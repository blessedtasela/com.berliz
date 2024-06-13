package com.berliz.serviceImplement;

import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.TodoList;
import com.berliz.models.User;
import com.berliz.repositories.TodoListRepo;
import com.berliz.repositories.UserRepo;
import com.berliz.services.TodoListService;
import com.berliz.utils.BerlizUtilities;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class TodoListServiceImplement implements TodoListService {

    @Autowired
    JWTFilter jwtFilter;

    @Autowired
    TodoListRepo todoListRepo;

    @Autowired
    UserRepo userRepo;

    @Override
    public ResponseEntity<String> addTodo(Map<String, String> requestMap) throws JsonProcessingException {
        log.info("Inside addTodo {}", requestMap);
        try {
            User user;
            if (jwtFilter.isAdmin() && requestMap.containsKey("email")) {
                user = userRepo.findByEmail(requestMap.get("email"));
            } else {
                String email = jwtFilter.getCurrentUserEmail();
                user = userRepo.findByEmail(email);
            }

            if (!jwtFilter.isBerlizUser()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!validateTodoListMap(requestMap, false)) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            if (jwtFilter.isAccountIncomplete(user)) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.COMPLETE_ACCOUNT_INFORMATION);
            }

            String task = requestMap.get("task");
            Boolean isTodo = todoListRepo.existsByUserAndTask(user, task);
            if (isTodo) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "You have this todo registered already");
            }

            getTodoListFromMap(requestMap, user);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Todo successfully created");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<TodoList>> getAllTodos() {
        try {
            log.info("inside getAllTodos");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity<>(todoListRepo.findAll(), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<TodoList>> getMyTodo() {
        try {
            log.info("inside getAllTodos");
            User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
            List<TodoList> todoList = todoListRepo.findByUser(user);
            if (todoList.isEmpty()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>(todoList, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateTodo(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("inside updateTodo {}", requestMap);
            if (!jwtFilter.isBerlizUser()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            boolean isValid = validateTodoListMap(requestMap, true);
            log.info("Is request valid? {}", isValid);

            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            Optional<TodoList> optional = todoListRepo.findById(Integer.parseInt(requestMap.get("id")));
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Todo id not found");
            }

            log.info("inside optional {}", requestMap);
            TodoList todoList = optional.get();
            User user = todoList.getUser();
            Boolean isTodo;
            if (jwtFilter.isAdmin()) {
                isTodo = true;
            } else {
                isTodo = todoListRepo.existsByUserAndTask(user, todoList.getTask());
            }

            if (!isTodo) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            todoList.setTask(requestMap.get("task"));
            todoList.setLastUpdate(new Date());
            TodoList savedTodoList = todoListRepo.save(todoList);
            String adminNotificationMessage = "Todo with id: " + todoList.getId() + " and content: "
                    + todoList.getTask() + ", has been updated for " + user.getEmail();
            String notificationMessage = "You have added a new todo: " + todoList.getTask();
            jwtFilter.sendNotifications("/topic/updateTodoList", adminNotificationMessage,
                    user, notificationMessage, savedTodoList);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Todo updated successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> bulkAction(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("inside bulkAction {}", requestMap);
            if (!jwtFilter.isBerlizUser()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            boolean isValid = !requestMap.isEmpty();
            log.info("Is request valid? {}", isValid);

            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            String idString = requestMap.get("ids");
            String[] idArray = idString.split(",");
            if (idString.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "No todo selected");
            }

            List<Integer> idList = Arrays.stream(idArray)
                    .map(Integer::valueOf)
                    .toList();
            List<TodoList> todoLists = todoListRepo.findAllById(idList);
            if (todoLists.isEmpty() || todoLists.size() != idList.size()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Todos id not found");
            }

            log.info("inside optional {}", requestMap);
            boolean isDelete = requestMap.get("action").equalsIgnoreCase("delete");
            boolean isComplete = requestMap.get("action").equalsIgnoreCase("complete");
            boolean isInProgress = requestMap.get("action").equalsIgnoreCase("in-progress");
            boolean isPending = requestMap.get("action").equalsIgnoreCase("pending");
            User user = null;
            for (TodoList todoList : todoLists) {
                user = todoList.getUser();
                break;
            }

            List<String> todoTasks = new ArrayList<>();
            boolean isTodo = false;
            if (jwtFilter.isAdmin()) {
                isTodo = true;
            } else {
                for (Integer todoId : idList) {
                    Optional<TodoList> todoOptional = todoListRepo.findById(todoId);
                    if (todoOptional.isEmpty()) {
                        isTodo = false;
                        break;
                    }

                    TodoList todoList = todoOptional.get();
                    todoTasks.add(todoList.getTask());
                    isTodo = todoListRepo.existsByUserAndTask(user, todoList.getTask());
                }
            }

            if (!isTodo) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            String successMessage = "";
            if (!(isDelete ^ isComplete ^ isInProgress ^ isPending)) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Action not recognized");
            }

            if (isDelete) {
                int updatedCount = todoListRepo.bulkDeleteByIds(idList);
                if (updatedCount < 0) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.SOMETHING_WENT_WRONG);
                }
                successMessage = "All Todos deleted successfully";
            }

            if (isComplete) {
                int updatedCount = todoListRepo.bulkUpdateStatusByIds(idList, "completed");
                if (updatedCount < 0) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.SOMETHING_WENT_WRONG);
                }
                successMessage = "All Todos are now completed";
            }

            if (isInProgress) {
                int updatedCount = todoListRepo.bulkUpdateStatusByIds(idList, "in-progress");
                if (updatedCount < 0) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.SOMETHING_WENT_WRONG);
                }
                successMessage = "All Todos are now in progress";
            }

            if (isPending) {
                int updatedCount = todoListRepo.bulkUpdateStatusByIds(idList, "pending");
                if (updatedCount < 0) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.SOMETHING_WENT_WRONG);
                }
                successMessage = "All Todos are now pending";
            }

            assert user != null;
            String adminNotificationMessage = successMessage + " with ids: " + idList + "and tasks: "
                    + todoTasks + ", for " + user.getEmail() + " and bulk action done";
            String notificationMessage = "You have perform a bulk action and " + successMessage +
                    " with tasks: " + todoTasks;
            jwtFilter.sendNotifications("/topic/todoBulkAction", adminNotificationMessage,
                    user, notificationMessage, todoLists);
            return BerlizUtilities.buildResponse(HttpStatus.OK, successMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateStatus(Integer id, String status) throws JsonProcessingException {
        try {
            log.info("inside updateStatus {} {}", id, status);
            if (!jwtFilter.isBerlizUser()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            boolean isValid = status.equalsIgnoreCase("pending")
                    || status.equalsIgnoreCase("completed")
                    || status.equalsIgnoreCase("in-progress");
            log.info("Is request valid? {}", isValid);
            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            Optional<TodoList> optional = todoListRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Todo id not found");
            }

            log.info("inside optional {}", optional);
            TodoList todoList = optional.get();
            User user = todoList.getUser();
            Boolean isTodo = todoListRepo.existsByUserAndTask(user, todoList.getTask());
            if (!isTodo) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            todoList.setStatus(status);
            todoList.setLastUpdate(new Date());
            TodoList savedTodoList = todoListRepo.save(todoList);
            String adminNotificationMessage = "Todo with id: " + todoList.getId() + " and task" + todoList.getTask() +
                    ", status has been set to " + status + " for " + user.getEmail();
            String notificationMessage = "You have just set a todo: " + todoList.getTask() + ", to " + status;
            jwtFilter.sendNotifications("/topic/updateTodoStatus", adminNotificationMessage,
                    user, notificationMessage, savedTodoList);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Todo is now " + todoList.getStatus());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteTodo(Integer id) throws JsonProcessingException {
        try {
            log.info("inside deleteTodo {}", id);
            if (!jwtFilter.isBerlizUser()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            Optional<TodoList> optional = todoListRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "To-do id not found");
            }
            log.info("inside optional {}", id);
            TodoList todoList = optional.get();
            User user = todoList.getUser();
            todoListRepo.deleteById(id);
            String adminNotificationMessage = "Todo with id: " + todoList.getId() + " and task" + todoList.getTask()
                    + ", has been deleted for " + user.getEmail();
            String notificationMessage = "You have deleted a todo: " + todoList.getTask();
            jwtFilter.sendNotifications("/topic/deleteTodo", adminNotificationMessage,
                    user, notificationMessage, todoList);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Todo item deleted successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    private void getTodoListFromMap(Map<String, String> requestMap, User user) {
        TodoList todoList = new TodoList();
        Date currentDate = new Date();
        todoList.setDate(currentDate);
        todoList.setTask(requestMap.get("task"));
        todoList.setUser(user);
        todoList.setStatus("pending");
        todoList.setLastUpdate(currentDate);
        TodoList savedTodoList = todoListRepo.save(todoList);
        String adminNotificationMessage = "A new todo with id: " + savedTodoList.getId() + " and task: / "
                + savedTodoList.getTask() + " /, has been added for " + user.getEmail();
        String notificationMessage = "You have added a new todo: " + savedTodoList.getTask();
        jwtFilter.sendNotifications("/topic/getTodoFromMap", adminNotificationMessage,
                user, notificationMessage, savedTodoList);
    }

    private boolean validateTodoListMap(Map<String, String> requestMap, boolean validId) {
        if (validId) {
            return requestMap.containsKey("id")
                    && requestMap.containsKey("task");
        } else {
            return requestMap.containsKey("task");
        }
    }

}

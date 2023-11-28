package com.berliz.serviceImplement;

import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.TodoList;
import com.berliz.models.User;
import com.berliz.repository.TodoListRepo;
import com.berliz.repository.UserRepo;
import com.berliz.services.TodoListService;
import com.berliz.utils.BerlizUtilities;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public ResponseEntity<String> addTodo(Map<String, String> requestMap) throws JsonProcessingException {
        log.info("Inside addTodo {}", requestMap);
        try {
            User user;
            if(jwtFilter.isAdmin() && requestMap.containsKey("email")){
                user = userRepo.findByEmail(requestMap.get("email"));
            } else{
                String email = jwtFilter.getCurrentUser();
                user = userRepo.findByEmail(email);
            }

            if (!jwtFilter.isBerlizUser()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!validateTodoListMap(requestMap, false)) {
                BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            String task = requestMap.get("task");
            Boolean isTodo = todoListRepo.existsByUserAndTask(user, task);
            if (isTodo) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "You have this task registered already");
            }

            todoListRepo.save(getTodoListFromMap(requestMap, false, user));
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Task successfully created");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<TodoList>> getAllTodos() {
        try {
            log.info("inside getAllTodos {}");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity<List<TodoList>>(todoListRepo.findAll(), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<TodoList>> getMyTodo() {
        try {
            log.info("inside getAllTodos {}");
            User user = userRepo.findByEmail(jwtFilter.getCurrentUser());
            List<TodoList> todoList = todoListRepo.findByUser(user);
            if (todoList.isEmpty()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<List<TodoList>>(todoList, HttpStatus.OK);
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
            User user = userRepo.findByEmail(jwtFilter.getCurrentUser());
            Boolean isTodo;
            if(jwtFilter.isAdmin()){
                isTodo =  true;
            } else{
                isTodo = todoListRepo.existsByUserAndTask(user, todoList.getTask());
            }

            if (!isTodo) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            todoList.setTask(requestMap.get("task"));
            todoList.setLastUpdate(new Date());
            todoListRepo.save(todoList);
            simpMessagingTemplate.convertAndSend("/topic/updateTodoList", todoList);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Task updated successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateStatus(Integer id, String status) throws JsonProcessingException {
        try {
            log.info("inside updateStatus {}", id, status);
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
            User user = userRepo.findByEmail(jwtFilter.getCurrentUser());
            Boolean isTodo = todoListRepo.existsByUserAndTask(user, todoList.getTask());

            if (isTodo == null) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            todoList.setStatus(status);
            todoList.setLastUpdate(new Date());
            todoListRepo.save(todoList);
            simpMessagingTemplate.convertAndSend("/topic/updateTodoStatus", todoList);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Task is now " + todoList.getStatus());
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
            todoListRepo.deleteById(id);
            simpMessagingTemplate.convertAndSend("/topic/deleteTodo", todoList);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "To-do item deleted successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }


    private TodoList getTodoListFromMap(Map<String, String> requestMap, boolean isAdd, User user) {
        TodoList todoList = new TodoList();
        Date currentDate = new Date();
        if (isAdd) {
            todoList.setId(Integer.parseInt(requestMap.get("id")));
        }
        todoList.setDate(currentDate);
        todoList.setTask(requestMap.get("task"));
        todoList.setUser(user);
        todoList.setStatus("pending");
        todoList.setLastUpdate(currentDate);

        simpMessagingTemplate.convertAndSend("/topic/getTodoFromMap", todoList);
        return todoList;
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

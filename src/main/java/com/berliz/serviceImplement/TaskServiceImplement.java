package com.berliz.serviceImplement;

import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.*;
import com.berliz.repository.*;
import com.berliz.services.TaskService;
import com.berliz.utils.BerlizUtilities;
import com.berliz.utils.EmailUtilities;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class TaskServiceImplement implements TaskService {

    @Autowired
    TaskRepo taskRepo;

    @Autowired
    CategoryRepo categoryRepo;

    @Autowired
    SubTaskRepo subTaskRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    JWTFilter jwtFilter;

    @Autowired
    EmailUtilities emailUtilities;

    @Autowired
    ExerciseRepo exerciseRepo;

    @Autowired
    TrainerRepo trainerRepo;

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public ResponseEntity<String> addTask(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside addTask {}", requestMap);
            boolean isValid = validateRequestFromMap(requestMap, false);
            log.info("Is request valid? {}", isValid);

            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            if (!jwtFilter.isAdmin() || jwtFilter.isTrainer()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            User user = userRepo.findByEmail(requestMap.get("email"));
            if (user == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "User email not found in db");
            }

            if (jwtFilter.isAdmin()) {
                if (requestMap.get("trainerId").isEmpty()) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Admin must provide trainerId");
                }

                Task task = taskRepo.findActiveTaskByUser(user);
                if (task != null) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Tasks exists already");
                }

                Trainer trainer = trainerRepo.findByTrainerId(Integer.valueOf(requestMap.get("trainerId")));
                if (trainer == null) {
                    return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Trainer not found");
                }

                getTaskFromMap(requestMap, user, trainer);
                return BerlizUtilities.buildResponse(HttpStatus.OK, "You have successfully created task for "
                        + user.getFirstname() + " with trainer: " + trainer.getName());
            } else {
                Trainer trainer = trainerRepo.findByUserId(jwtFilter.getCurrentUserId());
                if (trainer == null) {
                    return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Trainer not found");
                }
                getTaskFromMap(requestMap, user, trainer);
                return BerlizUtilities.buildResponse(HttpStatus.OK, "Hello "
                        + trainer.getName() + " you have successfully created task for " + user.getFirstname());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> addSubTask(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside addSubTask {}", requestMap);
            boolean isValid = validateSubTaskFromMap(requestMap, false);
            log.info("Is request valid? {}", isValid);

            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            if (!jwtFilter.isAdmin() || jwtFilter.isTrainer()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            Optional<Task> optional = taskRepo.findById(Integer.valueOf(requestMap.get("taskId")));
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Task not found in db");
            }

            Task task = optional.get();
            String userEmail = task.getTrainer().getPartner().getUser().getEmail();
            if (!(jwtFilter.isAdmin() || jwtFilter.getCurrentUser().equalsIgnoreCase(userEmail))) {

            }

            SubTask subTask = subTaskRepo.findByName(requestMap.get("name"));
            if (subTask != null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "SubTask exists already");
            }

            getSubTaskFromMap(requestMap, task);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Task successfully created task for "
                    + task.getUser().getFirstname() + " with trainer: " + task.getTrainer().getName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<Task>> getAllTasks() {
        try {
            log.info("Inside getAllTasks");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<Task> tasks = taskRepo.findAll();
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<SubTask>> getAllSubTasks() {
        try {
            log.info("Inside getAllSubTasks");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<SubTask> subTasks = subTaskRepo.findAll();
            return new ResponseEntity<>(subTasks, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Task>> getTrainerTasks() {
        try {
            log.info("Inside getTrainerTasks");
            Trainer trainer = trainerRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (!jwtFilter.isTrainer() && trainer == null) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            List<Task> tasks = taskRepo.findByTrainer(trainer);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Task>> getClientTasks() {
        try {
            log.info("Inside getTrainerTasks");
            User user = userRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (!jwtFilter.isUser() && user == null) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            List<Task> tasks = taskRepo.findByUser(user);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Task>> getActiveTasks() {
        try {
            log.info("Inside getActiveTasks");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<Task> tasks = taskRepo.findAll();
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Task> getTask(Integer id) {
        try {
            log.info("Inside getTask");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new Task(), HttpStatus.UNAUTHORIZED);
            }
            Optional<Task> optional = taskRepo.findById(id);
            if (optional.isEmpty()) {
                return new ResponseEntity<>(new Task(), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(optional.get(), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Task(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateTask(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside updateTask {}", requestMap);
            boolean isValid = validateRequestFromMap(requestMap, true);
            log.info("Is request valid? {}", isValid);
            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            Optional<Task> optional = taskRepo.findById(Integer.valueOf(requestMap.get("id")));
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Task ID not found");
            }

            Task task = optional.get();
            String currentUser = jwtFilter.getCurrentUser();
            if (!(jwtFilter.isAdmin() || task.getTrainer().getPartner().getUser().getEmail().equals(currentUser))) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (task.getStatus().equalsIgnoreCase("true")) {

                return BerlizUtilities.buildResponse(HttpStatus.OK, "Sorry " +
                        task.getUser().getFirstname() + ", task is active. you cannot make an update yet");
            }

            task.setDescription(requestMap.get("description"));
            task.setPriority(requestMap.get("priority"));
            String startDateString = requestMap.get("startDate");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = dateFormat.parse(startDateString);
            String endDateString = requestMap.get("endDate");
            task.setStartDate(startDate);
            Date endDate = dateFormat.parse(endDateString);
            task.setEndDate(endDate);
            task.setStatus("true");
            task.setDate(new Date());
            task.setLastUpdate(new Date());

            // Parse and save subtasks
            String subTasksString = requestMap.get("subTasks");
            List<SubTask> subTasks = new ArrayList<>();
            if (subTasksString != null) {
                JSONArray subTasksArray = new JSONArray(subTasksString);
                for (int i = 0; i < subTasksArray.length(); i++) {
                    JSONObject subTaskJson = subTasksArray.getJSONObject(i);
                    SubTask subTask = new SubTask();
                    subTask.setTask(task);
                    subTask.setDate(new Date());
                    subTask.setName(subTaskJson.getString("name"));
                    Optional<Exercise> exerciseOptional = exerciseRepo.findById(Integer.valueOf(subTaskJson.getString("exerciseId")));
                    if (optional.isPresent()) {
                        subTask.setExercise(exerciseOptional.get());
                    }
                    subTasks.add(subTask);
                }
                subTasks = subTaskRepo.saveAll(subTasks);
            }

            task.setSubTasks(subTasks);
            Task savedTask = taskRepo.save(task);
            simpMessagingTemplate.convertAndSend("/topic/getTaskFromMap", savedTask);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Task updated successfully for " + task.getUser().getFirstname());
        } catch (Exception ex) {
            ex.printStackTrace();
            return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
        }
    }

    @Override
    public ResponseEntity<String> updateSubTask(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside updateSubTask {}", requestMap);
            boolean isValid = validateSubTaskFromMap(requestMap, true);
            log.info("Is request valid? {}", isValid);
            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            Optional<SubTask> optional = subTaskRepo.findById(Integer.valueOf(requestMap.get("id")));
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "SubTask ID not found");
            }

            SubTask subTask = optional.get();
            SubTask trainerSubTask = null;
            Trainer trainer = trainerRepo.findByUserId(jwtFilter.getCurrentUserId());
            List<Task> tasks = taskRepo.findByTrainer(trainer);

            // Check if the current user is an admin or the subTask is associated with a task handled by the trainer
            if (!(jwtFilter.isAdmin() || tasks.stream().anyMatch(task ->
                    task.getSubTasks().stream().anyMatch(st -> st.getId().equals(subTask.getId()))))) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            subTask.setName(requestMap.get("name"));
            SubTask savedSubTask = subTaskRepo.save(subTask);
            simpMessagingTemplate.convertAndSend("/topic/updateSubTask", savedSubTask);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "subTask updated successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
            return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
        }
    }

    @Override
    public ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside updateStatus {}", id);
            String status;
            if (!jwtFilter.isAdmin()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            Optional<Task> optional = taskRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Task not found");
            }
            log.info("Inside optional {}", optional);
            status = optional.get().getStatus();
            Task task = optional.get();
            String responseMessage;
            if (status.equalsIgnoreCase("true")) {
                status = "false";
                responseMessage = "Task Status updated successfully. Now Deactivated";
            } else {
                status = "true";
                responseMessage = "Task Status updated successfully. Now Activated";
            }

            task.setStatus(status);
            taskRepo.save(task);
            simpMessagingTemplate.convertAndSend("/topic/updateTaskStatus", task);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (
                Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteTask(Integer id) throws JsonProcessingException {
        try {
            log.info("inside deleteTask {}", id);
            if (!jwtFilter.isAdmin()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            Optional<Task> optional = taskRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Task not found");
            }
            log.info("inside optional {}", optional);
            try {
                Task task = optional.get();
                taskRepo.deleteById(id);
                simpMessagingTemplate.convertAndSend("/topic/deleteTask", task);
                return BerlizUtilities.buildResponse(HttpStatus.OK, "Task deleted successfully");
            } catch (DataIntegrityViolationException ex) {
                // Handle foreign key constraint violation when deleting
                ex.printStackTrace();
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Cannot delete task due to a foreign key constraint violation.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteSubTask(Integer id) throws JsonProcessingException {
        try {
            log.info("inside deleteSubTask {}", id);
            if (!jwtFilter.isAdmin()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            Optional<SubTask> optional = subTaskRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "SubTask not found");
            }
            log.info("inside optional {}", optional);
            try {
                SubTask subTask = optional.get();
                subTaskRepo.deleteById(id);
                simpMessagingTemplate.convertAndSend("/topic/deleteSubTask", subTask);
                return BerlizUtilities.buildResponse(HttpStatus.OK, "subTask deleted successfully");
            } catch (DataIntegrityViolationException ex) {
                // Handle foreign key constraint violation when deleting
                ex.printStackTrace();
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Cannot delete subTask due to a foreign key constraint violation.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    private void getTaskFromMap(Map<String, String> requestMap, User user, Trainer trainer) throws ParseException, JSONException {
        Task task = new Task();
        task.setUser(user);
        task.setTrainer(trainer);

        task.setDescription(requestMap.get("description"));
        task.setPriority(requestMap.get("priority"));
        String startDateString = requestMap.get("startDate");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = dateFormat.parse(startDateString);
        String endDateString = requestMap.get("endDate");
        task.setStartDate(startDate);
        Date endDate = dateFormat.parse(endDateString);
        task.setEndDate(endDate);
        task.setStatus("true");
        task.setDate(new Date());
        task.setLastUpdate(new Date());

        // Parse and save subtasks
        String subTasksString = requestMap.get("subTasks");
        List<SubTask> subTasks = new ArrayList<>();
        if (subTasksString != null) {
            JSONArray subTasksArray = new JSONArray(subTasksString);
            for (int i = 0; i < subTasksArray.length(); i++) {
                JSONObject subTaskJson = subTasksArray.getJSONObject(i);
                SubTask subTask = new SubTask();
                subTask.setTask(task);
                subTask.setDate(new Date());
                subTask.setName(subTaskJson.getString("name"));
                Optional<Exercise> optional = exerciseRepo.findById(Integer.valueOf(subTaskJson.getString("exerciseId")));
                if (optional.isPresent()) {
                    subTask.setExercise(optional.get());
                }
                subTasks.add(subTask);
            }
            subTasks = subTaskRepo.saveAll(subTasks);
        }

        task.setSubTasks(subTasks);
        Task savedTask = taskRepo.save(task);
        simpMessagingTemplate.convertAndSend("/topic/getTaskFromMap", savedTask);
    }

    private boolean validateRequestFromMap(Map<String, String> requestMap, boolean validId) {
        if (validId) {
            return requestMap.containsKey("id")
                    && requestMap.containsKey("description")
                    && requestMap.containsKey("priority")
                    && requestMap.containsKey("startDate")
                    && requestMap.containsKey("endDate")
                    && requestMap.containsKey("subTasks");
        } else {
            return requestMap.containsKey("description")
                    && requestMap.containsKey("priority")
                    && requestMap.containsKey("startDate")
                    && requestMap.containsKey("endDate")
                    && requestMap.containsKey("subTasks");
        }
    }

    private void getSubTaskFromMap(Map<String, String> requestMap, Task task) {
        SubTask subTask = new SubTask();
        subTask.setTask(task);
        Optional<Exercise> optionalExercise = exerciseRepo.findById(Integer.valueOf(requestMap.get("exerciseId")));
        if (optionalExercise.isPresent()) {
            subTask.setExercise(optionalExercise.get());
        }

        subTask.setName(requestMap.get("name"));
        subTask.setDate(new Date());
        SubTask savedSubTask = subTaskRepo.save(subTask);
        simpMessagingTemplate.convertAndSend("/topic/getSubTaskFromMap", savedSubTask);
    }

    private boolean validateSubTaskFromMap(Map<String, String> requestMap, boolean validId) {
        if (validId) {
            return requestMap.containsKey("id")
                    && requestMap.containsKey("name")
                    && requestMap.containsKey("taskId")
                    && requestMap.containsKey("exerciseId");
        } else {
            return requestMap.containsKey("name")
                    && requestMap.containsKey("taskId")
                    && requestMap.containsKey("exerciseId");
        }
    }

}

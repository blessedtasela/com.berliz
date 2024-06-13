package com.berliz.serviceImplement;

import com.berliz.DTO.ExerciseRequest;
import com.berliz.DTO.FileRequest;
import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.Category;
import com.berliz.models.Exercise;
import com.berliz.models.MuscleGroup;
import com.berliz.repositories.CategoryRepo;
import com.berliz.repositories.ExerciseRepo;
import com.berliz.repositories.MuscleGroupRepo;
import com.berliz.services.ExerciseService;
import com.berliz.utils.BerlizUtilities;
import com.berliz.utils.FileUtilities;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class ExerciseServiceImplement implements ExerciseService {

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    ExerciseRepo exerciseRepo;

    @Autowired
    MuscleGroupRepo muscleGroupRepo;

    @Autowired
    FileUtilities fileUtilities;

    @Autowired
    CategoryRepo categoryRepo;

    @Autowired
    JWTFilter jwtFilter;

    @Override
    public ResponseEntity<String> addExercise(ExerciseRequest exerciseRequest) throws JsonProcessingException {
        log.info("Inside addExercise {}", exerciseRequest);
        try {
            if (!jwtFilter.isAdmin()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            boolean isValidRequest = exerciseRequest != null;
            log.info("is request valid? {}", isValidRequest);
            if (isValidRequest) {
                Exercise exercise = exerciseRepo.findByName(exerciseRequest.getName());

                if (Objects.isNull(exercise)) {
                    getExerciseFromMap(exerciseRequest);
                    return BerlizUtilities.buildResponse(HttpStatus.OK, "Exercise created successfully");
                } else {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Exercise Exists");
                }
            } else {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<Exercise>> getAllExercises() {
        try {
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            log.info("Inside getAllExercises 'ADMIN'");
            return new ResponseEntity<>(exerciseRepo.findAll(), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Exercise>> getActiveExercises() {
        try {
            log.info("Inside getActiveExercises 'USER'");
            return new ResponseEntity<>(exerciseRepo.getActiveExercises(), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Exercise> getExercise(Integer id) {
        try {
            log.info("Inside getExercise {}", id);
            Optional<Exercise> optional = exerciseRepo.findById(id);
            if (optional.isPresent()) {
                return new ResponseEntity<>(optional.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new Exercise(), HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Exercise(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> updateExerciseDemo(FileRequest fileRequest) throws JsonProcessingException {
        try {
            log.info("Inside updateExerciseDemo{}", fileRequest);
            MultipartFile file = fileRequest.getFile();
            if (file == null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "No file provided");
            }

            if (!fileUtilities.isValidImageType(file)) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Invalid file type");
            }

            if (!fileUtilities.isValidImageSize(file)) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Invalid file type");
            }

            if (!jwtFilter.isAdmin()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            log.info("Inside jwtFilter.isAdmin() {}", jwtFilter.isAdmin());
            Integer id = fileRequest.getId();
            Optional<Exercise> optional = exerciseRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Exercise id not found");
            }

            Exercise exercise = optional.get();
            exercise.setDemo(file.getBytes());
            exerciseRepo.save(exercise);
            String adminNotificationMessage = "Exercise with id: " + exercise.getId() + ", and info: "
                    + exercise.getName() + ", demo has been updated";
            String notificationMessage = "Your exercise demo has been updated : "
                    + exercise.getName();
            jwtFilter.sendNotifications("/topic/updateExerciseDemo", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, exercise);
            return BerlizUtilities.buildResponse(HttpStatus.OK, exercise.getName() + "'s demo updated successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateExercise(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside updateExercise {}", requestMap);
            if (!jwtFilter.isAdmin()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            boolean isValid = validateExerciseMap(requestMap);
            log.info("Is request valid? {}", isValid);
            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            // Extract Exercise ID from the request
            Integer id = Integer.parseInt(requestMap.get("id"));

            // Check if the Exercise with the given ID exists
            Optional<Exercise> optional = exerciseRepo.findById(id);
            log.info("Does exercise exist? {}", optional);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Exercise ID not found");
            }

            // Get the existing exercise
            Exercise exercise = optional.get();
            exercise.setName(requestMap.get("name"));
            exercise.setDescription(requestMap.get("description"));
            exercise.setLastUpdate(new Date());

            // Create a set to store the updated muscleGroups
            String muscleGroupIdsString = requestMap.get("muscleGroupIds");
            if (!muscleGroupIdsString.isEmpty()) {
                String[] muscleGroupIdsArray = muscleGroupIdsString.split(",");
                Set<MuscleGroup> muscleGroups = new HashSet<>();
                for (String muscleGroupIdString : muscleGroupIdsArray) {
                    int muscleGroupId = Integer.parseInt(muscleGroupIdString.trim());
                    MuscleGroup muscleGroup = muscleGroupRepo.findById(muscleGroupId)
                            .orElseThrow(() -> new EntityNotFoundException("MuscleGroup not found with ID: " + muscleGroupId));
                    muscleGroups.add(muscleGroup);
                }
                exercise.setMuscleGroups(muscleGroups);
            }

            // Create a set to store the updated categories
            String categoryIdsString = requestMap.get("categoryIds");
            if (!categoryIdsString.isEmpty()) {
                String[] categoryIdsArray = categoryIdsString.split(",");
                Set<Category> categories = new HashSet<>();
                for (String categoryIdString : categoryIdsArray) {
                    int categoryId = Integer.parseInt(categoryIdString.trim());
                    Category category = categoryRepo.findById(categoryId)
                            .orElseThrow(() -> new EntityNotFoundException("Exercise not found with ID: " + categoryId));
                    categories.add(category);
                }
                exercise.setCategories(categories);
            }

            exerciseRepo.save(exercise);
            String adminNotificationMessage = "Exercise with id: " + exercise.getId() + ", and info: "
                    + exercise.getName() + ", information has been updated";
            String notificationMessage = "Your exercise information has been updated : "
                    + exercise.getName();
            jwtFilter.sendNotifications("/topic/updateExercise", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, exercise);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Exercise updated successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside updateStatus {}", id);
            String status;
            if (!jwtFilter.isAdmin()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            Optional<Exercise> optional = exerciseRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Exercise ID not found");
            }
            log.info("Inside optional {}", optional);
            status = optional.get().getStatus();
            Exercise exercise = optional.get();
            String responseMessage;
            if (status.equalsIgnoreCase("true")) {
                status = "false";
                responseMessage = "Exercise Status updated successfully. Now Deactivated";
            } else {
                status = "true";
                responseMessage = "Exercise Status updated successfully. Now Activated";
            }

            exercise.setStatus(status);
            exerciseRepo.save(exercise);
            String adminNotificationMessage = "Exercise with id: " + exercise.getId() +
                    ", status has been set to " + status;
            String notificationMessage = "You have successfully set your exercise status to: " + status;
            jwtFilter.sendNotifications("/topic/updateExerciseStatus", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, exercise);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (
                Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteExercise(Integer id) throws JsonProcessingException {
        try {
            log.info("inside deleteExercise {}", id);
            if (!jwtFilter.isAdmin()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            Optional<Exercise> optional = exerciseRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Exercise id not found");
            }
            log.info("inside optional {}", id);
            Exercise exercise = optional.get();
            if (exercise.getStatus().equalsIgnoreCase("true")) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Exercise is active, cannot complete request");
            }

            exerciseRepo.deleteById(id);
            String adminNotificationMessage = "Exercise with id: " + exercise.getId() + ", and info: "
                    + exercise.getName() + ", has been deleted";
            String notificationMessage = "You have successfully deleted your exercise: " + exercise.getName();
            jwtFilter.sendNotifications("/topic/deleteExercise", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, exercise);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Exercise deleted successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    private void getExerciseFromMap(ExerciseRequest request) throws IOException {
        Exercise exercise = new Exercise();
        MultipartFile demoFile = request.getDemo();
        byte[] video = demoFile.getBytes();
        exercise.setDescription(request.getDescription());
        exercise.setDemo(video);
        exercise.setName(request.getName());
        exercise.setDate(new Date());
        exercise.setLastUpdate(new Date());
        exercise.setStatus("false");

        // Parse muscleGroupId as a comma-separated string
        String muscleGroupIdsString = request.getMuscleGroups();
        if (!muscleGroupIdsString.isEmpty()) {
            String[] muscleGroupIdsArray = muscleGroupIdsString.split(",");
            Set<MuscleGroup> muscleGroups = new HashSet<>();
            for (String muscleGroupIdString : muscleGroupIdsArray) {
                int muscleGroupId = Integer.parseInt(muscleGroupIdString.trim());
                MuscleGroup muscleGroup = muscleGroupRepo.findById(muscleGroupId)
                        .orElseThrow(() -> new EntityNotFoundException("MuscleGroup not found with ID: " + muscleGroupId));
                muscleGroups.add(muscleGroup);
            }
            exercise.setMuscleGroups(muscleGroups);
        }

        // Parse categoryId as a comma-separated string
        String categoryIdsString = request.getCategories();
        if (!categoryIdsString.isEmpty()) {
            String[] categoryIdsArray = categoryIdsString.split(",");
            Set<Category> categories = new HashSet<>();
            for (String categoryIdString : categoryIdsArray) {
                int categoryId = Integer.parseInt(categoryIdString.trim());
                Category category = categoryRepo.findById(categoryId)
                        .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + categoryId));
                categories.add(category);
            }
            exercise.setCategories(categories);
        }

        Exercise savedExercise = exerciseRepo.save(exercise);
        String adminNotificationMessage = "A new exercise with id: " + savedExercise.getId()
                + " and info" + savedExercise.getName() + ", has been added";
        String notificationMessage = "You have successfully added a new exercise: " + savedExercise.getName();
        jwtFilter.sendNotifications("/topic/getExerciseFromMap", adminNotificationMessage,
                jwtFilter.getCurrentUser(), notificationMessage, savedExercise);
    }

    private boolean validateExerciseMap(Map<String, String> requestMap) {
        if (true) {
            return requestMap.containsKey("id")
                    && requestMap.containsKey("name")
                    && requestMap.containsKey("description");
        } else {
            return requestMap.containsKey("name")
                    && requestMap.containsKey("description");
        }
    }

}

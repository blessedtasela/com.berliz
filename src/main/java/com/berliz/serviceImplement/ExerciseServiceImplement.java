package com.berliz.serviceImplement;

import com.berliz.DTO.ExerciseRequest;
import com.berliz.DTO.FileRequest;
import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.Category;
import com.berliz.models.Exercise;
import com.berliz.models.MuscleGroup;
import com.berliz.repository.CategoryRepo;
import com.berliz.repository.ExerciseRepo;
import com.berliz.repository.MuscleGroupRepo;
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
        log.info("Inside signUp {}", exerciseRequest);
        try {
            if (!jwtFilter.isAdmin()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            boolean isValidRequest = exerciseRequest != null;
            log.info("is request valid? {}", isValidRequest);
            if (isValidRequest) {
                Exercise exercise = exerciseRepo.findByName(exerciseRequest.getName());

                if (Objects.isNull(exercise)) {
                    exerciseRepo.save(getExerciseFromMap(exerciseRequest));
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
            simpMessagingTemplate.convertAndSend("/topic/updateExerciseDemo", exercise);
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

            boolean isValid = validateExerciseMap(requestMap, true);
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
            if (muscleGroupIdsString != null) {
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
            if (categoryIdsString != null) {
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
            simpMessagingTemplate.convertAndSend("/topic/updateExercise", exercise);
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
            simpMessagingTemplate.convertAndSend("/topic/updateExerciseStatus", exercise);
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
            simpMessagingTemplate.convertAndSend("/topic/deleteExercise", exercise);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Exercise deleted successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    private Exercise getExerciseFromMap(ExerciseRequest request) throws IOException {
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
        if (muscleGroupIdsString != null) {
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
        if (categoryIdsString != null) {
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

        simpMessagingTemplate.convertAndSend("/topic/getExerciseFromMap", exercise);
        return exercise;
    }

    private boolean validateExerciseMap(Map<String, String> requestMap, boolean validId) {
        if (validId) {
            return requestMap.containsKey("id")
                    && requestMap.containsKey("name")
                    && requestMap.containsKey("description");
        } else {
            return requestMap.containsKey("name")
                    && requestMap.containsKey("description");
        }
    }

}

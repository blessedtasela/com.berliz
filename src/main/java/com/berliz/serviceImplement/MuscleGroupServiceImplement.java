package com.berliz.serviceImplement;

import com.berliz.DTO.FileRequest;
import com.berliz.DTO.MuscleGroupRequest;
import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.Exercise;
import com.berliz.models.MuscleGroup;
import com.berliz.repository.ExerciseRepo;
import com.berliz.repository.MuscleGroupRepo;
import com.berliz.services.MuscleGroupService;
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
public class MuscleGroupServiceImplement implements MuscleGroupService {

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    MuscleGroupRepo muscleGroupRepo;

    @Autowired
    ExerciseRepo exerciseRepo;

    @Autowired
    FileUtilities fileUtilities;

    @Autowired
    JWTFilter jwtFilter;

    @Override
    public ResponseEntity<String> addMuscleGroup(MuscleGroupRequest muscleGroupRequest) throws JsonProcessingException {
        log.info("Inside signUp {}", muscleGroupRequest);
        try {
            if (!jwtFilter.isAdmin()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            boolean isValidRequest = muscleGroupRequest != null;
            log.info("is request valid? {}", isValidRequest);
            if (isValidRequest) {
                MuscleGroup muscleGroup = muscleGroupRepo.findByName(muscleGroupRequest.getName());

                if (Objects.isNull(muscleGroup)) {
                    muscleGroupRepo.save(getMuscleGroupFromMap(muscleGroupRequest));
                    return BerlizUtilities.buildResponse(HttpStatus.OK, "Muscle Group created successfully");
                } else {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Muscle Group Exists");
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
    public ResponseEntity<List<MuscleGroup>> getAllMuscleGroups() {
        try {
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            log.info("Inside getAllMuscleGroups 'ADMIN'");
            return new ResponseEntity<>(muscleGroupRepo.findAll(), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<MuscleGroup>> getActiveMuscleGroups() {
        try {
            log.info("Inside getActiveMuscleGroups 'USER'");
            return new ResponseEntity<>(muscleGroupRepo.getActiveMuscleGroups(), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<MuscleGroup> getMuscleGroup(Integer id) {
        try {
            log.info("Inside getMuscleGroup {}", id);
            Optional<MuscleGroup> optional = muscleGroupRepo.findById(id);
            if (optional.isPresent()) {
                return new ResponseEntity<>(optional.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new MuscleGroup(), HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new MuscleGroup(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> updateMuscleGroupImage(FileRequest imageRequest) throws JsonProcessingException {
        try {
            log.info("Inside updateMuscleGroupImage{}", imageRequest);
            MultipartFile file = imageRequest.getFile();
            if (file == null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "No photo provided");
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
            Integer id = imageRequest.getId();
            Optional<MuscleGroup> optional = muscleGroupRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "MuscleGroup id not found");
            }

            MuscleGroup muscleGroup = optional.get();
            muscleGroup.setImage(file.getBytes());
            muscleGroupRepo.save(muscleGroup);
            simpMessagingTemplate.convertAndSend("/topic/updateMuscleGroupImage", muscleGroup);
            return BerlizUtilities.buildResponse(HttpStatus.OK, muscleGroup.getName() + "'s photo updated successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateMuscleGroup(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside updateMuscleGroup {}", requestMap);
            if (!jwtFilter.isAdmin()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            boolean isValid = validateMuscleGroupMap(requestMap, true);
            log.info("Is request valid? {}", isValid);
            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            // Extract MuscleGroup ID from the request
            Integer id = Integer.parseInt(requestMap.get("id"));

            // Check if the category with the given ID exists
            Optional<MuscleGroup> optional = muscleGroupRepo.findById(id);
            log.info("Does muscleGroup exist? {}", optional);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "MuscleGroup ID not found");
            }

            // Get the existing category
            MuscleGroup muscleGroup = optional.get();
            muscleGroup.setName(requestMap.get("name"));
            muscleGroup.setBodyPart(requestMap.get("bodyPart"));
            muscleGroup.setDescription(requestMap.get("description"));
            muscleGroup.setLastUpdate(new Date());

            // Create a set to store the updated exercises
            String exerciseIdsString = requestMap.get("exerciseIds");
            if (exerciseIdsString != null) {
                String[] exerciseIdsArray = exerciseIdsString.split(",");
                Set<Exercise> exercises = new HashSet<>();
                for (String exerciseIdString : exerciseIdsArray) {
                    int exerciseId = Integer.parseInt(exerciseIdString.trim());
                    Exercise exercise = exerciseRepo.findById(exerciseId)
                            .orElseThrow(() -> new EntityNotFoundException("Exercise not found with ID: " + exerciseId));
                    exercises.add(exercise);
                }
                muscleGroup.setExercises(exercises);
            }

            muscleGroupRepo.save(muscleGroup);
            simpMessagingTemplate.convertAndSend("/topic/updateMuscleGroup", muscleGroup);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "MuscleGroup updated successfully");

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
            Optional<MuscleGroup> optional = muscleGroupRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "MuscleGroup ID not found");
            }
            log.info("Inside optional {}", optional);
            status = optional.get().getStatus();
            MuscleGroup muscleGroup = optional.get();
            String responseMessage;
            if (status.equalsIgnoreCase("true")) {
                status = "false";
                responseMessage = "MuscleGroup Status updated successfully. Now Deactivated";
            } else {
                status = "true";
                responseMessage = "MuscleGroup Status updated successfully. Now Activated";
            }

            muscleGroup.setStatus(status);
            muscleGroupRepo.save(muscleGroup);
            simpMessagingTemplate.convertAndSend("/topic/updateMuscleGroupStatus", muscleGroup);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (
                Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteMuscleGroup(Integer id) throws JsonProcessingException {
        try {
            log.info("inside deleteMuscleGroup {}", id);
            if (!jwtFilter.isAdmin()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            Optional<MuscleGroup> optional = muscleGroupRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "MuscleGroup id not found");
            }
            log.info("inside optional {}", id);
            MuscleGroup muscleGroup = optional.get();
            if (muscleGroup.getStatus().equalsIgnoreCase("true")) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "MuscleGroup is active, cannot complete request");
            }

            muscleGroupRepo.deleteById(id);
            simpMessagingTemplate.convertAndSend("/topic/deleteMuscleGroup", muscleGroup);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "MuscleGroup deleted successfully");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    private MuscleGroup getMuscleGroupFromMap(MuscleGroupRequest request) throws IOException {
        MuscleGroup muscleGroup = new MuscleGroup();
        byte[] image = request.getImage().getBytes();

        muscleGroup.setDescription(request.getDescription());
        muscleGroup.setImage(image);
        muscleGroup.setName(request.getName());
        muscleGroup.setBodyPart(request.getBodyPart());
        muscleGroup.setDate(new Date());
        muscleGroup.setLastUpdate(new Date());
        muscleGroup.setStatus("false");

        // Parse exercisesId as a comma-separated string
        String exerciseIdsString = request.getExercises();
        if (exerciseIdsString != null) {
            String[] exerciseIdsArray = exerciseIdsString.split(",");
            Set<Exercise> exercises = new HashSet<>();
            for (String exerciseIdString : exerciseIdsArray) {
                int exerciseId = Integer.parseInt(exerciseIdString.trim());
                Exercise exercise = exerciseRepo.findById(exerciseId)
                        .orElseThrow(() -> new EntityNotFoundException("Exercise not found with ID: " + exerciseId));
                exercises.add(exercise);
            }
            muscleGroup.setExercises(exercises);
        }
        simpMessagingTemplate.convertAndSend("/topic/getMuscleGroupFromMap", muscleGroup);
        return muscleGroup;
    }

    private boolean validateMuscleGroupMap(Map<String, String> requestMap, boolean validId) {
        if (validId) {
            return requestMap.containsKey("id")
                    && requestMap.containsKey("name")
                    && requestMap.containsKey("description")
                    && requestMap.containsKey("bodyPart");
        } else {
            return requestMap.containsKey("name")
                    && requestMap.containsKey("description")
                    && requestMap.containsKey("bodyPart");
        }
    }

}

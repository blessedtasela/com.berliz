package com.berliz.restImplement;

import com.berliz.DTO.TrainerRequest;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.Trainer;
import com.berliz.models.TrainerLike;
import com.berliz.models.TrainerPricing;
import com.berliz.rest.TrainerRest;
import com.berliz.services.TrainerService;
import com.berliz.utils.BerlizUtilities;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * REST API endpoints implementation for Trainer-related operations.
 */
@RestController
public class TrainerRestImplement implements TrainerRest {

    @Autowired
    TrainerService trainerService;

    /**
     * Add a new trainer.
     *
     * @param trainerRequest The trainer information to add.
     * @return ResponseEntity containing a status message.
     */
    @Override
    public ResponseEntity<String> addTrainer(TrainerRequest trainerRequest) throws JsonProcessingException {
        try {
            return trainerService.addTrainer(trainerRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Get a list of all trainers.
     *
     * @return ResponseEntity containing a list of trainers.
     */
    @Override
    public ResponseEntity<List<Trainer>> getAllTrainers() {
        try {
            return trainerService.getAllTrainers();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Get a list of active trainers.
     *
     * @return ResponseEntity containing a list of trainers.
     */
    @Override
    public ResponseEntity<List<Trainer>> getActiveTrainers() {
        try {
            return trainerService.getActiveTrainers();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Update trainer information.
     *
     * @param requestMap The map containing updated trainer information.
     * @return ResponseEntity containing a status message.
     */
    @Override
    public ResponseEntity<String> updateTrainer(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            return trainerService.updateTrainer(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }


    /**
     * Update trainer's photo.
     *
     * @param trainerRequest The trainer information including the new photo.
     * @return ResponseEntity containing a status message.
     */
    @Override
    public ResponseEntity<String> updatePhoto(TrainerRequest trainerRequest) throws JsonProcessingException {
        try {
            return trainerService.updatePhoto(trainerRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Delete a trainer by ID.
     *
     * @param id The ID of the trainer to delete.
     * @return ResponseEntity containing a status message.
     */
    @Override
    public ResponseEntity<String> deleteTrainer(Integer id) throws JsonProcessingException {
        try {
            return trainerService.deleteTrainer(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Update trainer's status.
     *
     * @param id The ID of the trainer to update status.
     * @return ResponseEntity containing a status message.
     */
    @Override
    public ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException {
        try {
            return trainerService.updateStatus(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Get a trainer.
     *
     * @return ResponseEntity containing trainer information.
     */
    @Override
    public ResponseEntity<Trainer> getTrainer() {
        try {
            return trainerService.getTrainer();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Trainer(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Likes a trainer based on the provided trainer ID.
     *
     * @param id The ID of the trainer to be liked.
     * @return A ResponseEntity containing a message about the like operation.
     * @throws JsonProcessingException If there is an issue processing JSON.
     */
    @Override
    public ResponseEntity<String> likeTrainer(Integer id) throws JsonProcessingException {
        try {
            return trainerService.likeTrainer(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Retrieves a list of TrainerLikes.
     *
     * @return A ResponseEntity containing the list of TrainerLikes.
     * @throws JsonProcessingException If there is an issue processing JSON.
     */
    @Override
    public ResponseEntity<List<TrainerLike>> getTrainerLikes() throws JsonProcessingException {
        try {
            return trainerService.getTrainerLikes();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Adds trainer pricing based on the provided request map.
     *
     * @param requestMap A map containing the necessary parameters for adding trainer pricing.
     * @return A ResponseEntity containing a message about the add operation.
     * @throws JsonProcessingException If there is an issue processing JSON.
     */
    @Override
    public ResponseEntity<String> addTrainerPricing(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            return trainerService.addTrainerPricing(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Updates trainer pricing based on the provided request map.
     *
     * @param requestMap A map containing the necessary parameters for updating trainer pricing.
     * @return A ResponseEntity containing a message about the update operation.
     * @throws JsonProcessingException If there is an issue processing JSON.
     */
    @Override
    public ResponseEntity<String> updateTrainerPricing(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            return trainerService.updateTrainerPricing(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Retrieves a list of TrainerPricing.
     *
     * @return A ResponseEntity containing the list of TrainerPricing.
     */
    @Override
    public ResponseEntity<List<TrainerPricing>> getTrainerPricing() {
        try {
            return trainerService.getTrainerPricing();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Deletes trainer pricing based on the provided trainer pricing ID.
     *
     * @param id The ID of the trainer pricing to be deleted.
     * @return A ResponseEntity containing a message about the delete operation.
     * @throws JsonProcessingException If there is an issue processing JSON.
     */
    @Override
    public ResponseEntity<String> deleteTrainerPricing(Integer id) throws JsonProcessingException {
        try {
            return trainerService.deleteTrainerPricing(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

}

package com.berliz.restImplement;

import com.berliz.DTO.TrainerRequest;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.Trainer;
import com.berliz.rest.TrainerRest;
import com.berliz.services.TrainerService;
import com.berliz.utils.BerlizUtilities;
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
    public ResponseEntity<String> addTrainer(TrainerRequest trainerRequest) {
        try {
            // Delegate the Trainer addTrainer operation to the service
            return trainerService.addTrainer(trainerRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
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
    public ResponseEntity<String> updateTrainer(Map<String, String> requestMap) {
        try {
            return trainerService.updateTrainer(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    /**
     * Update trainer's photo.
     *
     * @param trainerRequest The trainer information including the new photo.
     * @return ResponseEntity containing a status message.
     */
    @Override
    public ResponseEntity<String> updatePhoto(TrainerRequest trainerRequest) {
        try {
            return trainerService.updatePhoto(trainerRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Delete a trainer by ID.
     *
     * @param id The ID of the trainer to delete.
     * @return ResponseEntity containing a status message.
     */
    @Override
    public ResponseEntity<String> deleteTrainer(Integer id) {
        try {
            return trainerService.deleteTrainer(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Update trainer's status.
     *
     * @param id The ID of the trainer to update status.
     * @return ResponseEntity containing a status message.
     */
    @Override
    public ResponseEntity<String> updateStatus(Integer id) {
        try {
            // Delegate the Trainer updateStatus operation to the service
            return trainerService.updateStatus(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Get a trainer.
     *
     * @return ResponseEntity containing trainer information.
     */
    @Override
    public ResponseEntity<Trainer> getTrainer() {
        try {
            // Delegate the Trainer getTrainer operation to the service
            return trainerService.getTrainer();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Trainer(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

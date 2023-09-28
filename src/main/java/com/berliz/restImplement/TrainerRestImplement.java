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

    @Override
    public ResponseEntity<List<Trainer>> getAllTrainers() {
        try {
            // Delegate the Trainer getAllTrainers operation to the service
            return trainerService.getAllTrainers();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateTrainer(Map<String, String> requestMap) {
        try {
            // Delegate the Trainer updateTrainer operation to the service
            return trainerService.updateTrainer(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updatePartnerId(Integer id, Integer newId) {
        try {
            // Delegate the Trainer updatePartnerId operation to the service
            return trainerService.updatePartnerId(id, newId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updatePhoto(TrainerRequest trainerRequest) {
        try {
            return trainerService.updatePhoto(trainerRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteTrainer(Integer id) {
        try {
            return trainerService.deleteTrainer(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

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

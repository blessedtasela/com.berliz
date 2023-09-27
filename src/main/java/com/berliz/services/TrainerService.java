package com.berliz.services;

import com.berliz.DTO.TrainerRequest;
import com.berliz.models.Trainer;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * Service interface for managing trainer-related operations.
 */
public interface TrainerService {

    /**
     * Adds a new trainer based on the provided request map.
     *
     * @param trainerRequest Request body containing trainer details.
     * @return ResponseEntity indicating the result of the trainer addition operation.
     */
    ResponseEntity<String> addTrainer(TrainerRequest trainerRequest) throws JsonProcessingException;

    /**
     * Retrieves a list of all trainers.
     *
     * @return ResponseEntity containing the list of all trainers.
     */
    ResponseEntity<List<Trainer>> getAllTrainers();

    /**
     * Updates the details of a trainer based on the provided request map.
     *
     * @param requestMap Request body containing updated trainer details.
     * @return ResponseEntity indicating the result of the trainer update operation.
     */
    ResponseEntity<String> updateTrainer(Map<String, String> requestMap);

    /**
     * Updates the partner ID of a trainer.
     *
     * @param id    The ID of the trainer to update.
     * @param newId The new partner ID to associate with the trainer.
     * @return ResponseEntity indicating the result of the partner ID update operation.
     */
    ResponseEntity<String> updatePartnerId(Integer id, Integer newId);

    /**
     * Deletes a trainer with the specified ID.
     *
     * @param id The ID of the trainer to delete.
     * @return ResponseEntity indicating the result of the trainer deletion operation.
     */
    ResponseEntity<String> deleteTrainer(Integer id);

    /**
     * Updates the status of a trainer.
     *
     * @param id The ID of the trainer to update.
     * @return ResponseEntity indicating the result of the trainer status update operation.
     */
    ResponseEntity<String> updateStatus(Integer id);

    /**
     * Retrieves a trainer by its ID.
     *
     * @return ResponseEntity containing the trainer with the specified ID.
     */
    ResponseEntity<Trainer> getTrainer();

}

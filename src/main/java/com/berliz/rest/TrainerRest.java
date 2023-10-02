package com.berliz.rest;

import com.berliz.DTO.TrainerRequest;
import com.berliz.models.Trainer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API endpoints for managing trainer-related operations.
 */
@RequestMapping(path = "/trainer")
public interface TrainerRest {

    /**
     * Add a new trainer.
     *
     * @param trainerRequest Request body containing trainer details.
     * @return ResponseEntity indicating the result of the trainer addition operation.
     */
    @PostMapping(path = "/add")
    ResponseEntity<String> addTrainer(@ModelAttribute TrainerRequest trainerRequest);

    /**
     * Get a list of all trainers.
     *
     * @return ResponseEntity containing the list of all trainers.
     */
    @GetMapping(path = "/get")
    ResponseEntity<List<Trainer>> getAllTrainers();

    /**
     * Get a list of active trainers.
     *
     * @return ResponseEntity containing the list of all trainers.
     */
    @GetMapping(path = "/getActiveTrainers")
    ResponseEntity<List<Trainer>> getActiveTrainers();

    /**
     * Update an existing trainer's details.
     *
     * @param requestMap Request body containing updated trainer details.
     * @return ResponseEntity indicating the result of the trainer update operation.
     */
    @PutMapping(path = "/update")
    ResponseEntity<String> updateTrainer(@RequestBody Map<String, String> requestMap);

    /**
     * Update the partner ID associated with a trainer.
     *
     * @return ResponseEntity indicating the result of the partner ID update operation.
     */
    @PutMapping(path = "/updatePhoto")
    ResponseEntity<String> updatePhoto(@ModelAttribute TrainerRequest trainerRequest);


    /**
     * Delete a trainer.
     *
     * @param id The ID of the trainer to delete.
     * @return ResponseEntity indicating the result of the trainer deletion operation.
     */
    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteTrainer(@PathVariable Integer id);

    /**
     * Update the status of a trainer.
     *
     * @param id The ID of the trainer to update.
     * @return ResponseEntity indicating the result of the trainer status update operation.
     */
    @PutMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@PathVariable Integer id);

    /**
     * Get a trainer by its ID.
     *
     * @return ResponseEntity containing the trainer with the specified ID.
     */
    @GetMapping(path = "/getTrainer")
    ResponseEntity<Trainer> getTrainer();

}

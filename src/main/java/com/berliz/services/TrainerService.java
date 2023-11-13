package com.berliz.services;

import com.berliz.DTO.TrainerRequest;
import com.berliz.models.Trainer;
import com.berliz.models.TrainerLike;
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
    ResponseEntity<String> updateTrainer(Map<String, String> requestMap) throws JsonProcessingException;

    /**
     * Updates the photo of a trainer.
     *
     * @param trainerRequest The trainer information including the new photo.
     * @return ResponseEntity indicating the result of the photo update operation.
     * @throws JsonProcessingException if there is an issue processing JSON data.
     */
    ResponseEntity<String> updatePhoto(TrainerRequest trainerRequest) throws JsonProcessingException;

    /**
     * Deletes a trainer with the specified ID.
     *
     * @param id The ID of the trainer to delete.
     * @return ResponseEntity indicating the result of the trainer deletion operation.
     */
    ResponseEntity<String> deleteTrainer(Integer id) throws JsonProcessingException;

    /**
     * Updates the status of a trainer.
     *
     * @param id The ID of the trainer to update.
     * @return ResponseEntity indicating the result of the trainer status update operation.
     */
    ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException;

    /**
     * Retrieves a trainer by its ID.
     *
     * @return ResponseEntity containing the trainer with the specified ID.
     */
    ResponseEntity<Trainer> getTrainer();

    /**
     * Retrieves a list of active trainers.
     *
     * @return ResponseEntity containing the list of all trainers.
     */
    ResponseEntity<List<Trainer>> getActiveTrainers();

    /**
     * Likes a trainer with the specified ID.
     *
     * @param id The ID of the trainer to like.
     * @return ResponseEntity indicating the result of the like operation.
     */
    ResponseEntity<String> likeTrainer(Integer id) throws JsonProcessingException;

    /**
     * Retrieves the list of users who have liked a trainer.
     *
     * @return ResponseEntity containing the list of trainer likes.
     */
    ResponseEntity<List<TrainerLike>> getTrainerLikes();
}

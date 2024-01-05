package com.berliz.services;

import com.berliz.DTO.TrainerRequest;
import com.berliz.models.Trainer;
import com.berliz.models.TrainerLike;
import com.berliz.models.TrainerPricing;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * Service interface for managing trainer-related operations.
 */
public interface TrainerService {

    ResponseEntity<String> addTrainer(TrainerRequest trainerRequest) throws JsonProcessingException;

    ResponseEntity<List<Trainer>> getAllTrainers();

    ResponseEntity<String> updateTrainer(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> updatePhoto(TrainerRequest trainerRequest) throws JsonProcessingException;

    ResponseEntity<String> deleteTrainer(Integer id) throws JsonProcessingException;

    ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException;

    ResponseEntity<Trainer> getTrainer();

    ResponseEntity<List<Trainer>> getActiveTrainers();

    ResponseEntity<String> likeTrainer(Integer id) throws JsonProcessingException;

    ResponseEntity<List<TrainerLike>> getTrainerLikes();

    ResponseEntity<String> addTrainerPricing(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> updateTrainerPricing(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<List<TrainerPricing>> getTrainerPricing();

    ResponseEntity<String> deleteTrainerPricing(Integer id) throws JsonProcessingException;
}

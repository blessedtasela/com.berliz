package com.berliz.rest;

import com.berliz.DTO.TrainerRequest;
import com.berliz.models.Trainer;
import com.berliz.models.TrainerLike;
import com.berliz.models.TrainerPricing;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API endpoints for managing trainer-related operations.
 */
@RequestMapping(path = "/trainer")
public interface TrainerRest {

    @PostMapping(path = "/add")
    ResponseEntity<String> addTrainer(@ModelAttribute TrainerRequest trainerRequest) throws JsonProcessingException;

    @GetMapping(path = "/get")
    ResponseEntity<List<Trainer>> getAllTrainers();

    @GetMapping(path = "/getActiveTrainers")
    ResponseEntity<List<Trainer>> getActiveTrainers();

    @PutMapping(path = "/update")
    ResponseEntity<String> updateTrainer(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    @PutMapping(path = "/updatePhoto")
    ResponseEntity<String> updatePhoto(@ModelAttribute TrainerRequest trainerRequest) throws JsonProcessingException;

    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteTrainer(@PathVariable Integer id) throws JsonProcessingException;

    @PutMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@PathVariable Integer id) throws JsonProcessingException;

    @GetMapping(path = "/getTrainer")
    ResponseEntity<Trainer> getTrainer();

    @PutMapping(path = "/like/{id}")
    ResponseEntity<String> likeTrainer(@PathVariable Integer id) throws JsonProcessingException;

    @GetMapping(path = "/getTrainerLikes")
    ResponseEntity<List<TrainerLike>> getTrainerLikes() throws JsonProcessingException;

    @PostMapping(path = "/addTrainerPricing")
    ResponseEntity<String> addTrainerPricing(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    @PutMapping(path = "/updateTrainerPricing")
    ResponseEntity<String> updateTrainerPricing(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    @GetMapping(path = "/getTrainerPricing")
    ResponseEntity<List<TrainerPricing>> getTrainerPricing();

    @DeleteMapping(path = "/deleteTrainerPricing/{id}")
    ResponseEntity<String> deleteTrainerPricing(@PathVariable Integer id) throws JsonProcessingException;


}

package com.berliz.rest;

import com.berliz.DTO.CenterRequest;
import com.berliz.DTO.TrainerRequest;
import com.berliz.models.Center;
import com.berliz.models.CenterLike;
import com.berliz.models.Trainer;
import com.berliz.models.TrainerLike;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API endpoints for managing center-related operations.
 */
@RequestMapping(path = "/center")
public interface CenterRest {

    /**
     * Add a new center.
     *
     * @param centerRequest Request body containing center details.
     * @return ResponseEntity indicating the result of the center addition operation.
     */
    @PostMapping(path = "/add")
    ResponseEntity<String> addCenter(@ModelAttribute CenterRequest centerRequest) throws JsonProcessingException;

    /**
     * Get a list of all centers.
     *
     * @return ResponseEntity containing the list of all centers.
     */
    @GetMapping(path = "/get")
    ResponseEntity<List<Center>> getAllCenters();

    /**
     * Get a list of active centers.
     *
     * @return ResponseEntity containing the list of all centers whose status are true.
     */
    @GetMapping(path = "/getActiveCenters")
    ResponseEntity<List<Center>> getActiveCenters();

    /**
     * Update an existing center's details.
     *
     * @param requestMap Request body containing updated center details.
     * @return ResponseEntity indicating the result of the center update operation.
     */
    @PutMapping(path = "/update")
    ResponseEntity<String> updateCenter(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    /**
     * Delete a center.
     *
     * @param id The ID of the center to delete.
     * @return ResponseEntity indicating the result of the center deletion operation.
     */
    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteCenter(@PathVariable Integer id) throws JsonProcessingException;

    /**
     * Update the status of a center.
     *
     * @param id The ID of the center to update.
     * @return ResponseEntity indicating the result of the center status update operation.
     */
    @PutMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@PathVariable Integer id) throws JsonProcessingException;

    /**
     * Get a center by its user ID.
     *
     * @param id The user ID associated with the center.
     * @return ResponseEntity containing the center with the specified user ID.
     */
    @GetMapping(path = "/getByUserId")
    ResponseEntity<Center> getByUserId(@PathVariable Integer id);

    /**
     * Get a center.
     *
     * @return ResponseEntity containing the center with the specified ID.
     */
    @GetMapping(path = "/getCenter")
    ResponseEntity<Center> getCenter();

    /**
     * like a center by its ID.
     *
     * @return ResponseEntity containing the center with the specified ID.
     */
    /**
     * like a trainer by its ID.
     *
     * @return ResponseEntity containing the trainer with the specified ID.
     */
    @PutMapping(path = "/like/{id}")
    ResponseEntity<String> likeCenter(@PathVariable Integer id) throws JsonProcessingException;

    /**
     * Get list of trainers that are liked
     *
     * @return ResponseEntity containing the status of the operation
     */
    @GetMapping(path = "/getCenterLikes")
    ResponseEntity<List<CenterLike>> getCenterLikes() throws JsonProcessingException;

    /**
     * Update the center photo.
     *
     * @return ResponseEntity indicating the result of the operation.
     */
    @PutMapping(path = "/updatePhoto")
    ResponseEntity<String> updatePhoto(@ModelAttribute CenterRequest centerRequest) throws JsonProcessingException;

}

package com.berliz.rest;

import com.berliz.models.Center;
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
     * @param requestMap Request body containing center details.
     * @return ResponseEntity indicating the result of the center addition operation.
     */
    @PostMapping(path = "/add")
    ResponseEntity<String> addCenter(@RequestBody Map<String, String> requestMap);

    /**
     * Get a list of all centers.
     *
     * @return ResponseEntity containing the list of all centers.
     */
    @GetMapping(path = "/get")
    ResponseEntity<List<Center>> getAllCenters();

    /**
     * Update an existing center's details.
     *
     * @param requestMap Request body containing updated center details.
     * @return ResponseEntity indicating the result of the center update operation.
     */
    @PutMapping(path = "/update")
    ResponseEntity<String> updateCenter(@RequestBody Map<String, String> requestMap);

    /**
     * Update the partner ID associated with a center.
     *
     * @param id    The ID of the center to update.
     * @param newId The new partner ID to associate with the center.
     * @return ResponseEntity indicating the result of the partner ID update operation.
     */
    @PutMapping(path = "/updatePartnerId/{id}/{newId}")
    ResponseEntity<String> updatePartnerId(@PathVariable Integer id, @PathVariable Integer newId);

    /**
     * Delete a center.
     *
     * @param id The ID of the center to delete.
     * @return ResponseEntity indicating the result of the center deletion operation.
     */
    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteCenter(@PathVariable Integer id);

    /**
     * Update the status of a center.
     *
     * @param id The ID of the center to update.
     * @return ResponseEntity indicating the result of the center status update operation.
     */
    @PutMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@PathVariable Integer id);

    /**
     * Get a center by its partner ID.
     *
     * @param id The partner ID associated with the center.
     * @return ResponseEntity containing the center with the specified partner ID.
     */
    @GetMapping(path = "/getByPartnerId/{id}")
    ResponseEntity<Center> getByPartnerId(@PathVariable Integer id);

    /**
     * Get a center by its user ID.
     *
     * @param id The user ID associated with the center.
     * @return ResponseEntity containing the center with the specified user ID.
     */
    @GetMapping(path = "/getByUserId/{id}")
    ResponseEntity<Center> getByUserId(@PathVariable Integer id);

    /**
     * Get a center by its ID.
     *
     * @param id The ID of the center.
     * @return ResponseEntity containing the center with the specified ID.
     */
    @GetMapping(path = "/getCenter/{id}")
    ResponseEntity<Center> getCenter(@PathVariable Integer id);

    /**
     * Get a list of centers by category ID.
     *
     * @param id The ID of the category associated with the centers.
     * @return ResponseEntity containing the list of centers with the specified category ID.
     */
    @GetMapping(path = "/getByCategoryId/{id}")
    ResponseEntity<List<Center>> getByCategoryId(@PathVariable Integer id);

    /**
     * Get a list of centers by status.
     *
     * @param status The status of the centers to retrieve.
     * @return ResponseEntity containing the list of centers with the specified status.
     */
    @GetMapping(path = "/getByStatus/{status}")
    ResponseEntity<List<Center>> getByStatus(@PathVariable String status);

}

package com.berliz.services;

import com.berliz.DTO.CenterRequest;
import com.berliz.models.Center;
import com.berliz.models.CenterLike;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * Service interface for managing center-related operations.
 */
public interface CenterService {

    /**
     * Adds a new center based on the provided request map.
     *
     * @param centerRequest The map containing the center's details
     * @return The response entity indicating the outcome of the operation
     */
    ResponseEntity<String> addCenter(CenterRequest centerRequest) throws JsonProcessingException;

    /**
     * Retrieves a list of all centers.
     *
     * @return The response entity containing the list of centers
     */
    ResponseEntity<List<Center>> getAllCenters();

    /**
     * Updates the details of a center based on the provided request map.
     *
     * @param requestMap The map containing the updated center details
     * @return The response entity indicating the outcome of the operation
     */
    ResponseEntity<String> updateCenter(Map<String, String> requestMap) throws JsonProcessingException;

    /**
     * Deletes a center with the specified ID.
     *
     * @param id The ID of the center to delete
     * @return The response entity indicating the outcome of the operation
     */
    ResponseEntity<String> deleteCenter(Integer id) throws JsonProcessingException;

    /**
     * Updates the status of a center.
     *
     * @param id The ID of the center to update
     * @return The response entity indicating the outcome of the operation
     */
    ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException;


    /**
     * Retrieves a center by its ID.
     *
     * @return The response entity containing the retrieved center
     */
    ResponseEntity<Center> getCenter();

    /**
     * Retrieves a center by the user ID.
     *
     * @param id The user ID
     * @return The response entity containing the retrieved center
     */
    ResponseEntity<Center> getByUserId(Integer id);

    /**
     * Like a center by its ID.
     *
     * @param id The center ID
     * @return The response entity indicating the outcome of the operation
     */
    ResponseEntity<String> likeCenter(Integer id) throws JsonProcessingException;

    /**
     * Get list of Centers that are liked
     *
     * @return The response entity indicating the outcome of the operation
     */
    ResponseEntity<List<CenterLike>> getCenterLikes();

    /**
     * Update a center photo
     *
     * @return The response entity indicating the outcome of the operation
     */
    ResponseEntity<String> updatePhoto(CenterRequest centerRequest) throws JsonProcessingException;

    /**
     * Get the list of active centers
     *
     * @return The response entity indicating the outcome of the operation
     */
    ResponseEntity<List<Center>> getActiveCenters();
}

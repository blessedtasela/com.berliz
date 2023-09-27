package com.berliz.services;

import com.berliz.models.Center;
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
     * @param requestMap The map containing the center's details
     * @return The response entity indicating the outcome of the operation
     */
    ResponseEntity<String> addCenter(Map<String, String> requestMap);

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
    ResponseEntity<String> updateCenter(Map<String, String> requestMap);

    /**
     * Updates the partner ID of a center.
     *
     * @param id    The ID of the center to update
     * @param newId The new partner ID to set
     * @return The response entity indicating the outcome of the operation
     */
    ResponseEntity<String> updatePartnerId(Integer id, Integer newId);

    /**
     * Deletes a center with the specified ID.
     *
     * @param id The ID of the center to delete
     * @return The response entity indicating the outcome of the operation
     */
    ResponseEntity<String> deleteCenter(Integer id);

    /**
     * Updates the status of a center.
     *
     * @param id The ID of the center to update
     * @return The response entity indicating the outcome of the operation
     */
    ResponseEntity<String> updateStatus(Integer id);

    /**
     * Retrieves a center by its partner ID.
     *
     * @param id The partner ID of the center to retrieve
     * @return The response entity containing the retrieved center
     */
    ResponseEntity<Center> getByPartnerId(Integer id);

    /**
     * Retrieves a center by its ID.
     *
     * @param id The ID of the center to retrieve
     * @return The response entity containing the retrieved center
     */
    ResponseEntity<Center> getCenter(Integer id);

    /**
     * Retrieves a list of centers that belong to a specific category.
     *
     * @param id The ID of the category
     * @return The response entity containing the list of centers
     */
    ResponseEntity<List<Center>> getByCategoryId(Integer id);

    /**
     * Retrieves a list of centers based on their status.
     *
     * @param status The status of the centers ("true" or "false")
     * @return The response entity containing the list of centers
     */
    ResponseEntity<List<Center>> getByStatus(String status);

    /**
     * Retrieves a center by the user ID.
     *
     * @param id The user ID
     * @return The response entity containing the retrieved center
     */
    ResponseEntity<Center> getByUserId(Integer id);
}

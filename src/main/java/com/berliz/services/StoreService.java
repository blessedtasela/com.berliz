package com.berliz.services;

import com.berliz.models.Store;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * Service interface for managing Store-related operations.
 */
public interface StoreService {

    /**
     * Get a list of stores based on the provided status.
     *
     * @param status The status of the stores to be retrieved.
     * @return ResponseEntity containing the list of stores with the specified status.
     */
    ResponseEntity<List<Store>> getByStatus(String status);

    /**
     * Get a store based on the provided partner ID.
     *
     * @param id The partner ID associated with the store.
     * @return ResponseEntity containing the store with the specified partner ID.
     */
    ResponseEntity<Store> getByPartnerId(Integer id);

    /**
     * Get a store based on the provided store ID.
     *
     * @param id The ID of the store to be fetched.
     * @return ResponseEntity containing the store with the specified store ID.
     */
    ResponseEntity<Store> getStore(Integer id);

    /**
     * Update the status of a store.
     *
     * @param id The ID of the store whose status is to be updated.
     * @return ResponseEntity indicating the result of the store status update operation.
     */
    ResponseEntity<String> updateStatus(Integer id);

    /**
     * Delete a store based on the provided store ID.
     *
     * @param id The ID of the store to be deleted.
     * @return ResponseEntity indicating the result of the store deletion operation.
     */
    ResponseEntity<String> deleteStore(Integer id);

    /**
     * Update the details of a store based on the provided request map.
     *
     * @param requestMap The map containing the updated store details.
     * @return ResponseEntity indicating the result of the store update operation.
     */
    ResponseEntity<String> updateStore(Map<String, String> requestMap);

    /**
     * Get a list of all stores.
     *
     * @return ResponseEntity containing the list of all stores.
     */
    ResponseEntity<List<Store>> getAllStores();

    /**
     * Add a new store based on the provided request map.
     *
     * @param requestMap The map containing the store's details.
     * @return ResponseEntity indicating the result of the store addition operation.
     */
    ResponseEntity<String> addStore(Map<String, String> requestMap);

    /**
     * Update the partner ID associated with a store.
     *
     * @param id    The ID of the store to update.
     * @param newId The new partner ID to associate with the store.
     * @return ResponseEntity indicating the result of the partner ID update operation.
     */
    ResponseEntity<String> updatePartnerId(Integer id, Integer newId);

    /**
     * Get a list of stores based on the provided category ID.
     *
     * @param id The ID of the category associated with the stores.
     * @return ResponseEntity containing the list of stores with the specified category ID.
     */
    ResponseEntity<List<Store>> getByCategoryId(Integer id);

    /**
     * Get a store based on the provided user ID.
     *
     * @param id The ID of the user associated with the store.
     * @return ResponseEntity containing the store with the specified user ID.
     */
    ResponseEntity<Store> getByUserId(Integer id);
}

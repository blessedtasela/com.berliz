package com.berliz.rest;

import com.berliz.models.Store;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API endpoints for store-related operations.
 */
@RequestMapping(path = "/store")
public interface StoreRest {

    /**
     * Add a new store.
     *
     * @param requestMap Request body containing store details.
     * @return ResponseEntity indicating the result of the store addition operation.
     */
    @PostMapping(path = "/add")
    ResponseEntity<String> addStore(@RequestBody Map<String, String> requestMap);

    /**
     * Get a list of all stores.
     *
     * @return ResponseEntity containing the list of all stores.
     */
    @GetMapping(path = "/get")
    ResponseEntity<List<Store>> getAllStores();

    /**
     * Update an existing store's details.
     *
     * @param requestMap Request body containing updated store details.
     * @return ResponseEntity indicating the result of the store update operation.
     */
    @PutMapping(path = "/update")
    ResponseEntity<String> updateStore(@RequestBody Map<String, String> requestMap);

    /**
     * Update the partner ID associated with a store.
     *
     * @param id    The ID of the store to update.
     * @param newId The new partner ID to associate with the store.
     * @return ResponseEntity indicating the result of the partner ID update operation.
     */
    @PutMapping(path = "/updatePartnerId/{id}/{newId}")
    ResponseEntity<String> updatePartnerId(@PathVariable Integer id, @PathVariable Integer newId);

    /**
     * Delete a store.
     *
     * @param id The ID of the store to delete.
     * @return ResponseEntity indicating the result of the store deletion operation.
     */
    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteStore(@PathVariable Integer id);

    /**
     * Update the status of a store.
     *
     * @param id The ID of the store to update.
     * @return ResponseEntity indicating the result of the store status update operation.
     */
    @PutMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@PathVariable Integer id);

    /**
     * Get a store by its partner ID.
     *
     * @param id The partner ID associated with the store.
     * @return ResponseEntity containing the store with the specified partner ID.
     */
    @GetMapping(path = "/getByPartnerId/{id}")
    ResponseEntity<Store> getByPartnerId(@PathVariable Integer id);

    /**
     * Get a store by its ID.
     *
     * @param id The ID of the store.
     * @return ResponseEntity containing the store with the specified ID.
     */
    @GetMapping(path = "/getStore/{id}")
    ResponseEntity<Store> getStore(@PathVariable Integer id);

    /**
     * Get stores by their category ID.
     *
     * @param id The ID of the category associated with the stores.
     * @return ResponseEntity containing the list of stores with the specified category ID.
     */
    @GetMapping(path = "/getByCategoryId/{id}")
    ResponseEntity<List<Store>> getByCategoryId(@PathVariable Integer id);

    /**
     * Get stores by their status.
     *
     * @param status The status of the stores to retrieve.
     * @return ResponseEntity containing the list of stores with the specified status.
     */
    @GetMapping(path = "/getByStatus/{status}")
    ResponseEntity<List<Store>> getByStatus(@PathVariable String status);

    /**
     * Get a store by its user ID.
     *
     * @param id The user ID associated with the store.
     * @return ResponseEntity containing the center with the specified user ID.
     */
    @GetMapping(path = "/getByUserId/{id}")
    ResponseEntity<Store> getByUserId(@PathVariable Integer id);

}

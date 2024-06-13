
package com.berliz.rest;

import com.berliz.models.Subscription;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API endpoints for managing subscription-related operations.
 */
@RequestMapping(path = "/subscription")
public interface SubscriptionRest {

    /**
     * Add a new subscription.
     *
     * @param requestMap Request body containing subscription details.
     * @return ResponseEntity indicating the result of the subscription addition operation.
     */
    @PostMapping(path = "/add")
    ResponseEntity<String> addSubscription(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    /**
     * Get a list of all subscriptions.
     *
     * @return ResponseEntity containing the list of all subscriptions.
     */
    @GetMapping(path = "/get")
    ResponseEntity<List<Subscription>> getAllSubscriptions();

    /**
     * Get a list of active subscriptions.
     *
     * @return ResponseEntity containing the list of all subscriptions whose status is true.
     */
    @GetMapping(path = "/getActiveSubscriptions")
    ResponseEntity<List<Subscription>> getActiveSubscriptions();

    /**
     * Update an existing subscription's details.
     *
     * @param requestMap Request body containing updated subscription details.
     * @return ResponseEntity indicating the result of the subscription update operation.
     */
    @PutMapping(path = "/update")
    ResponseEntity<String> updateSubscription(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    /**
     * Delete a subscription.
     *
     * @param id The ID of the subscription to delete.
     * @return ResponseEntity indicating the result of the subscription deletion operation.
     */
    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteSubscription(@PathVariable Integer id) throws JsonProcessingException;

    /**
     * Update the status of a subscription.
     *
     * @param id The ID of the subscription to update.
     * @return ResponseEntity indicating the result of the subscription status update operation.
     */
    @PutMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@PathVariable Integer id) throws JsonProcessingException;

    /**
     * Get a subscription.
     *
     * @return ResponseEntity containing the subscription with the specified ID.
     */
    @GetMapping(path = "/getSubscription")
    ResponseEntity<Subscription> getSubscription(Integer id);

    /**
     * Get subscriptions related to a user.
     *
     * @return ResponseEntity containing the subscription with the specified ID.
     */
    @GetMapping(path = "/getMySubscriptions")
    ResponseEntity<List<Subscription>> getMySubscriptions();

    @PutMapping(path = "/bulkAction")
    ResponseEntity<String> bulkAction(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;


}
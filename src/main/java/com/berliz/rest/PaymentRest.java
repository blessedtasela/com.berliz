
package com.berliz.rest;

import com.berliz.models.Client;
import com.berliz.models.Payment;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API endpoints for managing payment-related operations.
 */
@RequestMapping(path = "/payment")
public interface PaymentRest {

    /**
     * Add a new payment.
     *
     * @param requestMap Request body containing payment details.
     * @return ResponseEntity indicating the result of the payment addition operation.
     */
    @PostMapping(path = "/add")
    ResponseEntity<String> addPayment(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    /**
     * Get a list of all payments.
     *
     * @return ResponseEntity containing the list of all payments.
     */
    @GetMapping(path = "/get")
    ResponseEntity<List<Payment>> getAllPayments();

    /**
     * Get a list of active payments.
     *
     * @return ResponseEntity containing the list of all payments whose status is true.
     */
    @GetMapping(path = "/getActivePayments")
    ResponseEntity<List<Payment>> getActivePayments();

    /**
     * Update an existing payment's details.
     *
     * @param requestMap Request body containing updated payment details.
     * @return ResponseEntity indicating the result of the payment update operation.
     */
    @PutMapping(path = "/update")
    ResponseEntity<String> updatePayment(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    /**
     * Delete a payment.
     *
     * @param id The ID of the payment to delete.
     * @return ResponseEntity indicating the result of the payment deletion operation.
     */
    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deletePayment(@PathVariable Integer id) throws JsonProcessingException;

    /**
     * Update the status of a payment.
     *
     * @param id The ID of the payment to update.
     * @return ResponseEntity indicating the result of the payment status update operation.
     */
    @PutMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@PathVariable Integer id) throws JsonProcessingException;

    /**
     * Get a payment.
     *
     * @return ResponseEntity containing the payment with the specified ID.
     */
    @GetMapping(path = "/getPayment")
    ResponseEntity<Payment> getPayment(Integer id);

}

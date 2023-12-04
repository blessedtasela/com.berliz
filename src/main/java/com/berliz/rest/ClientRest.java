
package com.berliz.rest;

import com.berliz.models.Client;
import com.berliz.models.Member;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API endpoints for managing client-related operations.
 */
@RequestMapping(path = "/client")
public interface ClientRest {

    /**
     * Add a new client.
     *
     * @param requestMap Request body containing client details.
     * @return ResponseEntity indicating the result of the client addition operation.
     */
    @PostMapping(path = "/add")
    ResponseEntity<String> addClient(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    /**
     * Get a list of all clients.
     *
     * @return ResponseEntity containing the list of all clients.
     */
    @GetMapping(path = "/get")
    ResponseEntity<List<Client>> getAllClients();

    /**
     * Get a list of active clients.
     *
     * @return ResponseEntity containing the list of all clients whose status is true.
     */
    @GetMapping(path = "/getActiveClients")
    ResponseEntity<List<Client>> getActiveClients();

    /**
     * Update an existing client's details.
     *
     * @param requestMap Request body containing updated client details.
     * @return ResponseEntity indicating the result of the client update operation.
     */
    @PutMapping(path = "/update")
    ResponseEntity<String> updateClient(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    /**
     * Delete a client.
     *
     * @param id The ID of the client to delete.
     * @return ResponseEntity indicating the result of the client deletion operation.
     */
    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteClient(@PathVariable Integer id) throws JsonProcessingException;

    /**
     * Update the status of a client.
     *
     * @param id The ID of the client to update.
     * @return ResponseEntity indicating the result of the client status update operation.
     */
    @PutMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@PathVariable Integer id) throws JsonProcessingException;

    /**
     * Get a client.
     *
     * @return ResponseEntity containing the client with the specified ID.
     */
    @GetMapping(path = "/getClient")
    ResponseEntity<Client> getClient(Integer id);

}
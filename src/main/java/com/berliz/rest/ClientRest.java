
package com.berliz.rest;

import com.berliz.models.Client;
import com.berliz.models.TrainerReview;
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

    @PostMapping(path = "/add")
    ResponseEntity<String> addClient(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    @GetMapping(path = "/get")
    ResponseEntity<List<Client>> getAllClients();

    @GetMapping(path = "/getActiveClients")
    ResponseEntity<List<Client>> getActiveClients();

    @PutMapping(path = "/update")
    ResponseEntity<String> updateClient(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteClient(@PathVariable Integer id) throws JsonProcessingException;

    @PutMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@PathVariable Integer id) throws JsonProcessingException;

    @GetMapping(path = "/getClient")
    ResponseEntity<Client> getClient(Integer id);

    @GetMapping(path = "/getMyTrainerReviews")
    ResponseEntity<List<TrainerReview>> getMyTrainerReviews();

}
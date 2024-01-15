
package com.berliz.rest;

import com.berliz.DTO.ClientReviewRequest;
import com.berliz.models.Client;
import com.berliz.models.ClientReview;
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

    @PostMapping(path = "/addClientReview")
    ResponseEntity<String> addClientReview(@ModelAttribute ClientReviewRequest clientReviewRequest) throws JsonProcessingException;

    @PutMapping(path = "/updateClientReview")
    ResponseEntity<String> updateClientReview(@ModelAttribute ClientReviewRequest clientReviewRequest) throws JsonProcessingException;

    @PutMapping(path = "/updateClientReviewStatus/{id}")
    ResponseEntity<String> updateClientReviewStatus(@PathVariable Integer id) throws JsonProcessingException;

    @PutMapping(path = "/disableClientReview/{id}")
    ResponseEntity<String> disableClientReview(@PathVariable Integer id) throws JsonProcessingException;

    @DeleteMapping(path = "/deleteClientReview/{id}")
    ResponseEntity<String> deleteClientReview(@PathVariable Integer id) throws JsonProcessingException;

    @GetMapping(path = "/getMyClientReviews")
    ResponseEntity<List<ClientReview>> getMyClientReviews();


}
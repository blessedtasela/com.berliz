package com.berliz.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST API endpoints for managing dashboard-related operations.
 */

@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/dashboard")
public interface DashboardRest {

    /**
     * Get details in dashBoard.
     *
     * @return ResponseEntity indicating the result of the center addition operation.
     */
    @GetMapping(path = "/details")
    ResponseEntity<Map<String, Object>> getDetails();

    /**
     * Get data in Berliz.
     *
     * @return ResponseEntity indicating the result of the center addition operation.
     */
    @GetMapping(path = "/berliz")
    ResponseEntity<Map<String, Object>> getBerlizData();

    /**
     * Get data in Berliz.
     *
     * @return ResponseEntity indicating the result of the center addition operation.
     */
    @GetMapping(path = "/getPartnerDetails")
    ResponseEntity<String> getPartnerDetails() throws JsonProcessingException;

    @GetMapping(path = "/getProfileData")
    ResponseEntity<Map<String, Object>> getProfileData() throws JsonProcessingException;


}

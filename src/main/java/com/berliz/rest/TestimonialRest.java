
package com.berliz.rest;

import com.berliz.models.Testimonial;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API endpoints for managing testimonial-related operations.
 */
@RequestMapping(path = "/testimonial")
public interface TestimonialRest {

    /**
     * Add a new testimonial.
     *
     * @param requestMap Request body containing testimonial details.
     * @return ResponseEntity indicating the result of the testimonial addition operation.
     */
    @PostMapping(path = "/add")
    ResponseEntity<String> addTestimonial(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    /**
     * Get a list of all testimonials.
     *
     * @return ResponseEntity containing the list of all testimonials.
     */
    @GetMapping(path = "/get")
    ResponseEntity<List<Testimonial>> getAllTestimonials();

    /**
     * Get a list of active testimonials.
     *
     * @return ResponseEntity containing the list of all testimonials whose status is true.
     */
    @GetMapping(path = "/getActiveTestimonials")
    ResponseEntity<List<Testimonial>> getActiveTestimonials();

    /**
     * Update an existing testimonial's details.
     *
     * @param requestMap Request body containing updated testimonial details.
     * @return ResponseEntity indicating the result of the testimonial update operation.
     */
    @PutMapping(path = "/update")
    ResponseEntity<String> updateTestimonial(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    /**
     * Delete a testimonial.
     *
     * @param id The ID of the testimonial to delete.
     * @return ResponseEntity indicating the result of the testimonial deletion operation.
     */
    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteTestimonial(@PathVariable Integer id) throws JsonProcessingException;

    /**
     * Update the status of a testimonial.
     *
     * @param id The ID of the testimonial to update.
     * @return ResponseEntity indicating the result of the testimonial status update operation.
     */
    @PutMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@PathVariable Integer id) throws JsonProcessingException;

    /**
     * Get a testimonial.
     *
     * @return ResponseEntity containing the testimonial with the specified ID.
     */
    @GetMapping(path = "/getTestimonial")
    ResponseEntity<Testimonial> getTestimonial(Integer id);

}
package com.berliz.services;

import com.berliz.models.Testimonial;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface TestimonialService {
    ResponseEntity<String> addTestimonial(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<List<Testimonial>> getAllTestimonials();

    ResponseEntity<List<Testimonial>> getActiveTestimonials();

    ResponseEntity<String> updateTestimonial(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> deleteTestimonial(Integer id) throws JsonProcessingException;

    ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException;

    ResponseEntity<Testimonial> getTestimonial(Integer id);
}

package com.berliz.services;

import com.berliz.DTO.PartnerRequest;
import com.berliz.models.Partner;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface PartnerService {

    // Add a new partner using the provided requestMap
    ResponseEntity<String> addPartner(PartnerRequest requestMap) throws JsonProcessingException;

    ResponseEntity<String> updatePartner(Map<String, String> requestMap) throws JsonProcessingException;

    // Get a list of all partners
    ResponseEntity<List<Partner>> getAllPartners();

    // Update an existing partner's details using the provided requestMap
    ResponseEntity<String> updateFile(PartnerRequest request) throws JsonProcessingException;

    // Delete a partner with the given ID
    ResponseEntity<String> deletePartner(Integer id) throws JsonProcessingException;

    // Update the status of a partner using the provided ID
    ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException;

    // Get partner details by their ID
    ResponseEntity<Partner> getPartner();

    // Reject a partner's application using the provided ID
    ResponseEntity<String> rejectApplication(Integer id) throws JsonProcessingException;
}

package com.berliz.rest;

import com.berliz.DTO.PartnerRequest;
import com.berliz.models.Partner;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping(path = "/partner")
public interface PartnerRest {

    /**
     * Adds a new partner.
     *
     * @param request The PartnerRequest containing partner information to add.
     * @return ResponseEntity indicating the result of the partner addition operation.
     */
    @PostMapping(path = "/add")
    ResponseEntity<String> addPartner(@ModelAttribute PartnerRequest request);

    /**
     * Retrieves a list of all partners.
     *
     * @return ResponseEntity containing a list of all partners.
     */
    @GetMapping(path = "/get")
    ResponseEntity<List<Partner>> getAllPartners();

    /**
     * Retrieves a list of active partners.
     *
     * @return ResponseEntity containing a list of active partners.
     */
    @GetMapping(path = "/getActivePartners")
    ResponseEntity<List<Partner>> getActivePartners();

    /**
     * Updates partner information.
     *
     * @param requestMap A map containing partner information to update.
     * @return ResponseEntity indicating the result of the partner update operation.
     */
    @PutMapping(path = "/update")
    ResponseEntity<String> updatePartner(@RequestBody Map<String, String> requestMap);

    /**
     * Updates a partner's file.
     *
     * @param request The PartnerRequest containing file information to update.
     * @return ResponseEntity indicating the result of the file update operation.
     */
    @PutMapping(path = "/updateFile")
    ResponseEntity<String> updateFile(@ModelAttribute PartnerRequest request);

    /**
     * Deletes a partner by ID.
     *
     * @param id The ID of the partner to delete.
     * @return ResponseEntity indicating the result of the partner deletion operation.
     */
    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deletePartner(@PathVariable Integer id);

    /**
     * Updates the status of a partner by ID.
     *
     * @param id The ID of the partner to update.
     * @return ResponseEntity indicating the result of the status update operation.
     */
    @PutMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@PathVariable Integer id);

    /**
     * Rejects a partner application by ID.
     *
     * @param id The ID of the partner application to reject.
     * @return ResponseEntity indicating the result of the rejection operation.
     */
    @PutMapping(path = "/reject/{id}")
    ResponseEntity<String> rejectApplication(@PathVariable Integer id);

    /**
     * Retrieves partner information.
     *
     * @return ResponseEntity containing partner information.
     */
    @GetMapping(path = "/getPartner")
    ResponseEntity<Partner> getPartner();
}

package com.berliz.restImplement;

import com.berliz.constants.BerlizConstants;
import com.berliz.models.ContactUs;
import com.berliz.models.ContactUsMessage;
import com.berliz.rest.ContactUsRest;
import com.berliz.services.ContactUsService;
import com.berliz.utils.BerlizUtilities;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class ContactUsRestImplement implements ContactUsRest {

    @Autowired
    ContactUsService contactUsService;

    /**
     * Add a new Contact Us entry.
     *
     * @param requestMap A Map containing the request parameters.
     * @return ResponseEntity containing a message indicating the success or failure of the operation.
     * @throws JsonProcessingException If there is an error in processing JSON.
     */
    @Override
    public ResponseEntity<String> addContactUs(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            return contactUsService.addContactUs(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Get a list of all Contact Us entries.
     *
     * @return ResponseEntity containing a list of ContactUs objects.
     */
    @Override
    public ResponseEntity<List<ContactUs>> getAllContactUs() {
        try {
            return contactUsService.getAllContactUs();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<ContactUsMessage>> getContactUsMessages() {
        try {
            return contactUsService.getContactUsMessages();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Get a specific Contact Us entry by ID.
     *
     * @param id The ID of the Contact Us entry to retrieve.
     * @return ResponseEntity containing the ContactUs object.
     * @throws JsonProcessingException If there is an error in processing JSON.
     */
    @Override
    public ResponseEntity<?> getContactUs(Integer id) throws JsonProcessingException {
        try {
            return contactUsService.getContactUs(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Update a Contact Us entry.
     *
     * @param requestMap A Map containing the request parameters.
     * @return ResponseEntity containing a message indicating the success or failure of the operation.
     * @throws JsonProcessingException If there is an error in processing JSON.
     */
    @Override
    public ResponseEntity<String> updateContactUs(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            return contactUsService.updateContactUs(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Update the status of a Contact Us entry.
     *
     * @param id The ID of the Contact Us entry to update.
     * @return ResponseEntity containing a message indicating the success or failure of the operation.
     * @throws JsonProcessingException If there is an error in processing JSON.
     */
    @Override
    public ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException {
        try {
            return contactUsService.updateStatus(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Review a Contact Us entry.
     *
     * @param requestMap A Map containing the request parameters.
     * @return ResponseEntity containing a message indicating the success or failure of the review.
     * @throws JsonProcessingException If there is an error in processing JSON.
     */
    @Override
    public ResponseEntity<String> reviewContactUs(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            return contactUsService.reviewContactUs(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Delete a Contact Us entry by ID.
     *
     * @param id The ID of the Contact Us entry to delete.
     * @return ResponseEntity containing a message indicating the success or failure of the operation.
     * @throws JsonProcessingException If there is an error in processing JSON.
     */
    @Override
    public ResponseEntity<String> deleteContactUs(Integer id) throws JsonProcessingException {
        try {
            return contactUsService.deleteContactUs(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }
}

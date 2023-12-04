package com.berliz.serviceImplement;

import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.ContactUs;
import com.berliz.models.ContactUsMessage;
import com.berliz.repository.ContactUsMessageRepo;
import com.berliz.repository.ContactUsRepo;
import com.berliz.repository.UserRepo;
import com.berliz.services.ContactUsService;
import com.berliz.utils.BerlizUtilities;
import com.berliz.utils.EmailUtilities;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class ContactUsServiceImplement implements ContactUsService {

    @Autowired
    UserRepo userRepo;

    @Autowired
    JWTFilter jwtFilter;

    @Autowired
    EmailUtilities emailUtilities;

    @Autowired
    ContactUsRepo contactUsRepo;

    @Autowired
    ContactUsMessageRepo contactUsMessageRepo;

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    /**
     * Adds a contact us request to the system and sends notifications to the user and administrators.
     *
     * @param requestMap A map containing contact us request data (e.g., email, name, message).
     * @return A ResponseEntity with a status code and response message.
     * @throws JsonProcessingException If there's an issue with JSON processing.
     */
    @Override
    public ResponseEntity<String> addContactUs(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside addContactUs {}", requestMap);
            boolean isValid = validateContactUsMap(requestMap, false);
            ContactUs contactUs = contactUsRepo.findByEmail(requestMap.get("email"));

            if (contactUs != null) {
                if (contactUs.getStatus().equalsIgnoreCase("false")) {
                    return buildResponse(HttpStatus.BAD_REQUEST, "You have a pending message awaiting review");
                }
            }

            if (!isValid) {
                return buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            String subject = "Berliz Management Team";
            getContactUsFromMap(requestMap);
            emailUtilities.sendContactUsMailToUser(subject, requestMap.get("name"), "true", requestMap.get("email"), "");
            emailUtilities.sendContactUsMailToAdmins("true", requestMap.get("email"), userRepo.getAllAdminsMail());
            return buildResponse(HttpStatus.OK, "Contact us request added successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Retrieves a list of all contact us requests.
     *
     * @return A ResponseEntity containing a list of contact us requests and an HTTP status code.
     */
    @Override
    public ResponseEntity<List<ContactUs>> getAllContactUs() {
        try {
            log.info("Inside getAllContactUs");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            return new ResponseEntity<>(contactUsRepo.findAll(), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Retrieves a specific contact us request by its ID.
     *
     * @param id The ID of the contact us request to retrieve.
     * @return A ResponseEntity containing the contact us request if found, or an error message and HTTP status code if not.
     */
    @Override
    public ResponseEntity<?> getContactUs(Integer id) {
        try {
            log.info("Inside getContactUs {}", id);
            Optional<ContactUs> optional = contactUsRepo.findById(id);
            if (optional.isPresent()) {
                // Return the contact us request if it exists
                return ResponseEntity.ok(optional);
            } else {
                // Return a bad request status with an error message if the contact us request is not found
                return ResponseEntity.badRequest().body("ContactUs id not found");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // Return INTERNAL_SERVER_ERROR status in case of an exception
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
    }

    /**
     * Updates an existing contact us request based on the provided request map.
     *
     * @param requestMap A map containing the updated contact us request data, including ID, name, email, and message.
     * @return A ResponseEntity with a success message if the update is successful, or an error message and HTTP status code if not.
     * @throws JsonProcessingException If there is an issue processing the request data.
     */
    @Override
    public ResponseEntity<String> updateContactUs(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("inside updateContactUs {}", requestMap);

            // Check if the user is an admin; if not so, return UNAUTHORIZED
            if (!jwtFilter.isAdmin()) {
                return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            // Validate the request map data
            boolean isValid = validateContactUsMap(requestMap, true);
            if (!isValid) {
                return buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            // Find the contact us request by ID
            Optional<ContactUs> optional = contactUsRepo.findById(Integer.parseInt(requestMap.get("id")));

            // Check if the contact us request exists
            if (optional.isEmpty()) {
                return buildResponse(HttpStatus.NOT_FOUND, "ContactUs id not found");
            }

            // Update the contact us request with the provided data
            ContactUs contactUs = optional.get();
            contactUs.setLastUpdate(new Date());
            contactUs.setName(requestMap.get("name"));
            contactUs.setEmail(requestMap.get("email"));
            contactUs.setMessage(requestMap.get("message"));
            contactUsRepo.save(contactUs);

            simpMessagingTemplate.convertAndSend("/topic/updateContactUs", contactUs);
            return buildResponse(HttpStatus.OK, "ContactUs updated successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }


    /**
     * Updates the status of a contact us request by its ID.
     *
     * @param id The ID of the contact us request to update.
     * @return A ResponseEntity with a success message if the status update is successful, or an error message and HTTP status code if not.
     * @throws JsonProcessingException If there is an issue processing the status update.
     */
    @Override
    public ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside updateStatus {}", id);
            String status;
            if (!jwtFilter.isAdmin()) {
                return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            Optional<ContactUs> optional = contactUsRepo.findById(id);

            if (optional.isEmpty()) {
                return buildResponse(HttpStatus.NOT_FOUND, "ContactUs id not found");
            }

            log.info("Inside optional {}", optional);

            // Toggle the status value
            status = optional.get().getStatus();
            if (status.equalsIgnoreCase("true")) {
                status = "false";
            } else {
                status = "true";
            }

            ContactUs contactUs = optional.get();
            contactUs.setStatus(status);
            contactUsRepo.save(contactUs);
            String subject = "Berliz Management Team";
            String name = optional.get().getName();
            String email = optional.get().getEmail();

            // Send notification emails based on the updated status
            emailUtilities.sendContactUsMailToUser(subject, name, status, email, "");
            emailUtilities.sendContactUsMailToAdmins(status, optional.get().getEmail(), userRepo.getAllAdminsMail());

            // Return a success message
            String responseMessage;
            if (status.equalsIgnoreCase("true")) {
                responseMessage = "ContactUs has been reviewed successfully";
            } else {
                responseMessage = "ContactUs is now pending";
            }
            simpMessagingTemplate.convertAndSend("/topic/updateContactUsStatus", contactUs);
            return buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Deletes a contact us request by its ID.
     *
     * @param id The ID of the contact us request to delete.
     * @return A ResponseEntity with a success message if the deletion is successful, or an error message and HTTP status code if not.
     * @throws JsonProcessingException If there is an issue processing the deletion.
     */
    @Override
    public ResponseEntity<String> deleteContactUs(Integer id) throws JsonProcessingException {
        try {
            log.info("inside deleteNewsletter {}", id);
            Optional<ContactUs> optional = contactUsRepo.findById(id);

            if (!jwtFilter.isAdmin()) {
                return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (optional.isEmpty()) {
                return buildResponse(HttpStatus.NOT_FOUND, "ContactUs id not found");
            }

            log.info("inside optional {}", id);
            ContactUs contactUs = optional.get();
            contactUsRepo.deleteById(id);
            simpMessagingTemplate.convertAndSend("/topic/deleteContactUs", contactUs);
            return buildResponse(HttpStatus.OK, "ContactUs deleted successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> reviewContactUs(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside reviewContactUs {}", requestMap);
            if (!jwtFilter.isAdmin()) {
                return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            Integer id = Integer.valueOf(requestMap.get("id"));
            String message = requestMap.get("body");
            String subject = requestMap.get("subject");
            Optional<ContactUs> optional = contactUsRepo.findById(id);

            if (optional.isEmpty()) {
                return buildResponse(HttpStatus.NOT_FOUND, "ContactUs id not found");
            }

            log.info("Inside optional {}", optional);
            String name = optional.get().getName();
            String email = optional.get().getEmail();
            String status = "true";

            if (optional.get().getStatus().equalsIgnoreCase("true")) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Status has been reviewed");
            }
            ContactUs contactUs = optional.get();
            contactUs.setStatus(status);
            contactUsRepo.save(contactUs);

            ContactUsMessage contactUsMessage = contactUsMessageRepo.findByMessage(message);
            if (contactUsMessage == null) {
                contactUsMessage = new ContactUsMessage();
                contactUsMessage.setDate(new Date());
                contactUsMessage.setMessage(message);
                contactUsMessage.setSubject(subject);
                contactUsMessageRepo.save(contactUsMessage);
            }

            emailUtilities.sendContactUsMailToUser(subject, name, status, email, message);
            emailUtilities.sendContactUsMailToAdmins(status, optional.get().getEmail(), userRepo.getAllAdminsMail());
            simpMessagingTemplate.convertAndSend("/topic/reviewContactUs", contactUs);
            return buildResponse(HttpStatus.OK, "ContactUs has been reviewed successfully");
        } catch (Exception ex) {
            log.error("SSomething went wrong", ex);
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Retrieves a list of all contact us requests.
     *
     * @return A ResponseEntity containing a list of contact us requests and an HTTP status code.
     */
    @Override
    public ResponseEntity<List<ContactUsMessage>> getContactUsMessages() {
        try {
            log.info("Inside getContactUsMessages");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            return new ResponseEntity<>(contactUsMessageRepo.findAll(), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Validates a newsletter request map based on the provided conditions.
     *
     * @param requestMap A map containing newsletter request data.
     * @param requireId  Set to true if 'id' is required; false otherwise.
     * @return True if the map is valid; false otherwise.
     */
    private boolean validateContactUsMap(Map<String, String> requestMap, boolean requireId) {
        if (requireId) {
            return requestMap.containsKey("id")
                    && requestMap.containsKey("email")
                    && requestMap.containsKey("name")
                    && requestMap.containsKey("message");
        } else {
            return requestMap.containsKey("email")
                    && requestMap.containsKey("name")
                    && requestMap.containsKey("message");
        }
    }

    /**
     * Converts a map of request data to a ContactUs object.
     *
     * @param requestMap A map containing contact information.
     * @return A ContactUs object with data from the request map.
     */
    private ContactUs getContactUsFromMap(Map<String, String> requestMap) {
        ContactUs contactUs = new ContactUs();
        Date currentDate = new Date();

        contactUs.setDate(currentDate);
        contactUs.setEmail(requestMap.get("email"));
        contactUs.setName(requestMap.get("name")); // Fixed typo here
        contactUs.setMessage(requestMap.get("message"));
        contactUs.setStatus("false");
        contactUs.setLastUpdate(currentDate);
        ContactUs savedContactUs = contactUsRepo.save(contactUs);
        simpMessagingTemplate.convertAndSend("/topic/getContactUsFromMap", savedContactUs);
        return contactUs;
    }

    /**
     * Build a ResponseEntity with the given status code and message.
     *
     * @param status  The HTTP status code.
     * @param message The response message.
     * @return A ResponseEntity with the specified status and message.
     * @throws JsonProcessingException If there is an issue processing the JSON response.
     */
    private ResponseEntity<String> buildResponse(HttpStatus status, String message) throws JsonProcessingException {
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", message);

        ObjectMapper objectMapper = new ObjectMapper();
        String responseBodyJson = objectMapper.writeValueAsString(responseBody);

        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseBodyJson);
    }

}

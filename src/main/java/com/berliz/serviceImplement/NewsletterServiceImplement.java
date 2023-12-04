package com.berliz.serviceImplement;

import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.Newsletter;
import com.berliz.models.NewsletterMessage;
import com.berliz.repository.NewsletterMessageRepo;
import com.berliz.repository.NewsletterRepo;
import com.berliz.repository.UserRepo;
import com.berliz.services.NewsletterService;
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
public class NewsletterServiceImplement implements NewsletterService {

    @Autowired
    UserRepo userRepo;

    @Autowired
    JWTFilter jwtFilter;

    @Autowired
    EmailUtilities emailUtilities;

    @Autowired
    NewsletterRepo newsletterRepo;

    @Autowired
    NewsletterMessageRepo newsletterMessageRepo;

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    /**
     * Adds a new newsletter subscription based on the provided data.
     *
     * @param requestMap A map containing the newsletter subscription data, including "email"
     * @return A ResponseEntity with a status message indicating the result of the operation
     * @throws JsonProcessingException If there is an issue with JSON processing
     */
    @Override
    public ResponseEntity<String> addNewsletter(Map<String, String> requestMap) throws JsonProcessingException {
        log.info("Inside addNewsletter {}", requestMap);
        try {
            boolean isValid = validateNewsletterMap(requestMap, false);
            if (!isValid) {
                return buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            Newsletter newsletter = newsletterRepo.findByEmail(requestMap.get("email"));
            if (newsletter != null) {
                return buildResponse(HttpStatus.BAD_REQUEST, "Email exists already");
            }

            getNewsletterFromMap(requestMap);
            emailUtilities.sendNewsletterStatusMailToUser("true", requestMap.get("email"));
            emailUtilities.sendStatusMailToAdmins("true", requestMap.get("email"),
                    userRepo.getAllAdminsMail(), "Newsletter");

            return buildResponse(HttpStatus.OK, "Newsletter email added successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Updates a newsletter subscription based on the provided data.
     *
     * @param requestMap A map containing the newsletter subscription data, including "id" and "email"
     * @return A ResponseEntity with a status message indicating the result of the operation
     * @throws JsonProcessingException If there is an issue with JSON processing
     */
    @Override
    public ResponseEntity<String> updateNewsletter(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("inside updateNewsletter {}", requestMap);
            if (!jwtFilter.isAdmin()) {
                return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            if (!validateNewsletterMap(requestMap, true)) {
                return buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }
            Optional<Newsletter> optional = newsletterRepo.findById(Integer.parseInt(requestMap.get("id")));
            if (optional.isEmpty()) {
                return buildResponse(HttpStatus.NOT_FOUND, "Newsletter id not found");
            }

            Newsletter newsletter = optional.get();
            Newsletter compareNewsletter = newsletterRepo.findByEmail(requestMap.get("email"));
            String compareEmail = compareNewsletter.getEmail();

            if (requestMap.get("email").equalsIgnoreCase(compareEmail)) {
                return buildResponse(HttpStatus.BAD_REQUEST, "Email exists already, cannot update");
            }

            newsletter.setEmail(requestMap.get("email"));
            newsletterRepo.save(newsletter);
            simpMessagingTemplate.convertAndSend("/topic/updateNewsletter", newsletter);
            return buildResponse(HttpStatus.OK, "Newsletter updated successfully");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Retrieves a list of newsletters based on the provided filter value.
     *
     * @param filterValue The filter value to apply when fetching newsletters
     * @return A ResponseEntity containing the list of newsletters and the HTTP status
     */
    @Override
    public ResponseEntity<List<Newsletter>> getAllNewsletters(String filterValue) {
        try {
            if (jwtFilter.isAdmin()) {
                return new ResponseEntity<>(newsletterRepo.findAll(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Retrieves a newsletter by its unique identifier.
     *
     * @param id The unique identifier of the newsletter to retrieve
     * @return A ResponseEntity containing the newsletter or an error message along with the HTTP status
     */
    @Override
    public ResponseEntity<?> getNewsletter(Integer id) {
        try {
            log.info("Inside getNewsletter {}", id);
            Optional<Newsletter> optional = newsletterRepo.findById(id);
            if (optional.isPresent()) {
                return ResponseEntity.ok(optional);
            } else {
                return ResponseEntity.badRequest().body("Newsletter id not found");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
    }

    /**
     * Deletes a newsletter entry with the specified ID.
     *
     * @param id The ID of the newsletter entry to be deleted
     * @return A ResponseEntity indicating the result of the delete operation
     * @throws JsonProcessingException if there is an issue with JSON processing
     */
    @Override
    public ResponseEntity<String> deleteNewsletter(Integer id) throws JsonProcessingException {
        try {
            log.info("inside deleteNewsletter {}", id);
            if (!jwtFilter.isAdmin()) {
                return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            Optional<Newsletter> optional = newsletterRepo.findById(id);
            if (optional.isEmpty()) {
                return buildResponse(HttpStatus.NOT_FOUND, "Newsletter id not found");
            }

            log.info("inside optional {}", id);
            Newsletter newsletter = optional.get();
            newsletterRepo.deleteById(id);
            simpMessagingTemplate.convertAndSend("/topic/deleteNewsletter", newsletter);
            return buildResponse(HttpStatus.OK, "Newsletter deleted successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside updateStatus {}", id);
            String status;
            if (!jwtFilter.isAdmin()) {
                return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            Optional<Newsletter> optional = newsletterRepo.findById(id);
            if (optional.isEmpty()) {
                return buildResponse(HttpStatus.NOT_FOUND, "Newsletter id not found");
            }

            log.info("Inside optional {}", optional);
            Newsletter newsletter = optional.get();
            String responseMessage;
            status = newsletter.getStatus();

            if (status.equalsIgnoreCase("true")) {
                status = "false";
                newsletter.setStatus(status);
                emailUtilities.sendNewsletterStatusMailToUser(status, optional.get().getEmail());
                emailUtilities.sendStatusMailToAdmins(status, optional.get().getEmail(),
                        userRepo.getAllAdminsMail(), "Newsletter");
                responseMessage = "Newsletter Status updated successfully. Now deactivated";
            } else {
                status = "true";
                newsletter.setStatus(status);
                emailUtilities.sendNewsletterStatusMailToUser(status, optional.get().getEmail());
                emailUtilities.sendStatusMailToAdmins(status, optional.get().getEmail(),
                        userRepo.getAllAdminsMail(), "Newsletter");
                responseMessage = "Newsletter Status updated successfully. Now activated";
            }

            newsletterRepo.save(newsletter);
            simpMessagingTemplate.convertAndSend("/topic/updateNewsletterStatus", newsletter);
            return buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> sendBulkMessage(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside sendBulkMessage {}", requestMap);
            String subject = requestMap.get("subject");
            String body = requestMap.get("body");
            List<String> emails = newsletterRepo.getAllActiveEMails();

            if (!jwtFilter.isAdmin()) {
                return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            NewsletterMessage newsletterMessage = newsletterMessageRepo.findByMessage(body);
            if (newsletterMessage == null) {
                newsletterMessage = new NewsletterMessage();
                newsletterMessage.setDate(new Date());
                newsletterMessage.setMessage(body);
                newsletterMessage.setSubject(subject);
                newsletterMessageRepo.save(newsletterMessage);
            }

            emailUtilities.sendBulkNewsletterMail(emails, body, subject);
            return buildResponse(HttpStatus.OK, "Newsletter has been sent to all active participants");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> sendMessage(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside sendMessage {}", requestMap);
            String subject = requestMap.get("subject");
            String body = requestMap.get("body");
            String email = requestMap.get("email");
            Newsletter newsletter = newsletterRepo.findByEmail(email);

            if (!jwtFilter.isAdmin()) {
                return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (newsletter == null) {
                return buildResponse(HttpStatus.NOT_FOUND, "Newsletter not found");
            }

            NewsletterMessage newsletterMessage = newsletterMessageRepo.findByMessage(body);
            if (newsletterMessage == null) {
                newsletterMessage = new NewsletterMessage();
                newsletterMessage.setDate(new Date());
                newsletterMessage.setMessage(body);
                newsletterMessage.setSubject(subject);
                newsletterMessageRepo.save(newsletterMessage);
            }

            emailUtilities.sendNewsletterMail(email, body, subject);
            return buildResponse(HttpStatus.OK, "Newsletter has been sent to participant");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Retrieves list of active newsletters.
     *
     * @return A ResponseEntity containing the newsletters or an error message along with the HTTP status
     */
    @Override
    public ResponseEntity<List<Newsletter>> getActiveNewsletters() {
        try {
            log.info("Inside getActiveNewsletters {}");
            if (jwtFilter.isAdmin()) {
                return new ResponseEntity<>(newsletterRepo.getActiveNewsletters(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Retrieves list of all newsletterMessages.
     *
     * @return A ResponseEntity containing the newsletterMessages or an error message along with the HTTP status
     */
    @Override
    public ResponseEntity<List<NewsletterMessage>> getNewsletterMessages() {
        try {
            log.info("Inside getNewsletterMessages {}");
            if (jwtFilter.isAdmin()) {
                return new ResponseEntity<>(newsletterMessageRepo.findAll(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    /**
     * Validates a map of request parameters for newsletter operations.
     *
     * @param requestMap The map of request parameters
     * @param validId    A boolean indicating whether a valid ID is required for validation
     * @return `true` if the request map is valid, otherwise `false`
     */
    private boolean validateNewsletterMap(Map<String, String> requestMap, boolean validId) {
        if (validId) {
            return requestMap.containsKey("id")
                    && requestMap.containsKey("email");
        } else {
            return requestMap.containsKey("email");
        }
    }

    /**
     * Creates a Newsletter object from the provided request map.
     *
     * @param requestMap A map containing key-value pairs from the request
     * @return A Newsletter object with data from the request map
     */
    private Newsletter getNewsletterFromMap(Map<String, String> requestMap) {
        Newsletter newsletter = new Newsletter();
        Date currentDate = new Date();
        newsletter.setDate(currentDate);
        newsletter.setEmail(requestMap.get("email"));
        newsletter.setStatus("true");
        newsletter.setLastUpdate(currentDate);
        Newsletter savedNewsletter = newsletterRepo.save(newsletter);
        simpMessagingTemplate.convertAndSend("/topic/getNewsletterFromMap", savedNewsletter);
        return newsletter;
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

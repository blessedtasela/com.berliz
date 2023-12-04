package com.berliz.serviceImplement;

import com.berliz.DTO.PartnerRequest;
import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.Center;
import com.berliz.models.Partner;
import com.berliz.models.Trainer;
import com.berliz.models.User;
import com.berliz.repository.CenterRepo;
import com.berliz.repository.PartnerRepo;
import com.berliz.repository.TrainerRepo;
import com.berliz.repository.UserRepo;
import com.berliz.services.PartnerService;
import com.berliz.utils.EmailUtilities;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class PartnerServiceImplement implements PartnerService {

    @Autowired
    PartnerRepo partnerRepo;

    @Autowired
    TrainerRepo trainerRepo;

    @Autowired
    CenterRepo centerRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    JWTFilter jwtFilter;

    @Autowired
    EmailUtilities emailUtilities;

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public ResponseEntity<String> addPartner(PartnerRequest request) throws JsonProcessingException {
        try {
            log.info("Inside addPartner {}", request);
            boolean isValid = request != null;
            log.info("Is request valid? {}", isValid);
            if (!isValid) {
                return buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            String role = request.getRole();
            if (!isValidRole(role)) {
                return buildResponse(HttpStatus.BAD_REQUEST, "Invalid role value. Allowed values are 'store', 'center', or 'trainer'");
            }

            if (jwtFilter.isAdmin()) {
                if (request.getEmail() == null) {
                    return buildResponse(HttpStatus.BAD_REQUEST, "Admin must provide user email");
                }

                User user = userRepo.findByEmail(request.getEmail());
                if (user == null) {
                    return buildResponse(HttpStatus.NOT_FOUND, "User id not found in db");
                }

                String userRole = user.getRole();
                boolean validateAdminRole = userRole.equalsIgnoreCase("admin");
                if (validateAdminRole) {
                    return buildResponse(HttpStatus.UNAUTHORIZED, "Admin cannot be a partner");
                }

                Partner partner = partnerRepo.findByUserId(user.getId());
                if (partner != null && partner.getStatus().equalsIgnoreCase("false")) {
                    return buildResponse(HttpStatus.BAD_REQUEST, "Partner application pending, please wait for approval");
                }

                getPartnerFromMap(request, user);
                return buildResponse(HttpStatus.OK, "You have successfully created a partner entry for " + user.getFirstname());

            } else {
                Integer userId = jwtFilter.getCurrentUserId();
                User user = userRepo.findByEmail(jwtFilter.getCurrentUser());
                if (user ==  null) {
                    return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid user");
                }

                Partner userPartner = partnerRepo.findByUserId(userId);
                if (userPartner != null && userPartner.getUser().getStatus().equalsIgnoreCase("false")) {
                    return buildResponse(HttpStatus.UNAUTHORIZED, "Your application is awaiting approval");
                }
                if (userPartner != null) {
                    return buildResponse(HttpStatus.UNAUTHORIZED, "Partner entry exists already");
                }
                getPartnerFromMap(request, user);
            }

            return buildResponse(HttpStatus.CREATED, "Partner entry added successfully");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Updates a partner's information based on the provided request data.
     *
     * @param requestMap A map containing partner update request data.
     *                   Expected keys: "id", "role", "motivation", "facebookUrl",
     *                   "instagramUrl", "youtubeUrl".
     * @return A ResponseEntity<String> containing the result of the update operation.
     * @throws JsonProcessingException If there is an issue processing JSON data.
     */
    @Override
    public ResponseEntity<String> updatePartner(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside updatePartner {}", requestMap);
            boolean isValid = validatePartnerMap(requestMap);
            log.info("Is request valid? {}", isValid);
            String role = requestMap.get("role");

            if (!isValid) {
                return buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            if (!isValidRole(role)) {
                return buildResponse(HttpStatus.BAD_REQUEST, "Invalid role value. Allowed values are 'store', 'center'," +
                        " 'trainer', or 'driver'");
            }

            Optional<Partner> optional = partnerRepo.findById(Integer.valueOf(requestMap.get("id")));
            if (optional.isEmpty()) {
                return buildResponse(HttpStatus.NOT_FOUND, "Partner ID not found");
            }

            Partner existingPartner = optional.get();
            String currentUser = jwtFilter.getCurrentUser();
            if (!(jwtFilter.isAdmin() || existingPartner.getUser().getEmail().equals(currentUser))) {
                return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (existingPartner.getStatus().equalsIgnoreCase("true")) {
                if (jwtFilter.isAdmin()) {
                    return buildResponse(HttpStatus.UNAUTHORIZED, "Cannot make an update. Partner is now active");
                } else {
                    return buildResponse(HttpStatus.OK, "Sorry " +
                            existingPartner.getUser().getFirstname() + ", you cannot make an update. " +
                            " Your application has been approved");
                }
            }

            existingPartner.setMotivation(requestMap.get("motivation"));
            existingPartner.setFacebookUrl(requestMap.get("facebookUrl"));
            existingPartner.setInstagramUrl(requestMap.get("instagramUrl"));
            existingPartner.setYoutubeUrl(requestMap.get("youtubeUrl"));
            existingPartner.setRole(requestMap.get("role"));
            existingPartner.setLastUpdate(new Date());
            partnerRepo.save(existingPartner);
            String responseMessage;

            if (jwtFilter.isAdmin()) {
                responseMessage = "Partner updated successfully";
            } else {
                responseMessage = "Hello " +
                        existingPartner.getUser().getFirstname() + " you have successfully " +
                        " updated your partnership application";
            }

            simpMessagingTemplate.convertAndSend("/topic/updatePartner", existingPartner);
            return buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
            return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
        }
    }

    @Override
    public ResponseEntity<List<Partner>> getAllPartners() {
        try {
            log.info("Inside getAllPartners");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<Partner> partners = partnerRepo.findAll();
            return new ResponseEntity<>(partners, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deletePartner(Integer id) throws JsonProcessingException {
        try {
            if (!jwtFilter.isAdmin()) {
                return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            Partner partner = partnerRepo.findByPartnerId(id);
            if (partner == null) {
                return buildResponse(HttpStatus.NOT_FOUND, "Partner id not found");
            }
            try {
                partnerRepo.delete(partner);
                simpMessagingTemplate.convertAndSend("/topic/deletePartner", partner);
                return buildResponse(HttpStatus.OK, "Partner deleted successfully");
            } catch (DataIntegrityViolationException ex) {
                // Handle foreign key constraint violation when deleting
                ex.printStackTrace();
                return buildResponse(HttpStatus.BAD_REQUEST, "Cannot delete partner due to a foreign key constraint violation.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside updateStatus {}", id);

            // Check if the user has admin privileges
            if (!jwtFilter.isAdmin()) {
                return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            String status;
            Optional<Partner> optional = partnerRepo.findById(id);
            Optional<User> user = userRepo.findById(optional.get().getUser().getId());

            if (optional.isEmpty()) {
                return buildResponse(HttpStatus.NOT_FOUND, "Partner id not found");
            }

            log.info("Inside optional {}", optional);
            Integer userId = user.get().getId();
            String role = optional.get().getRole();
            String responseMessage;
            Partner partner = optional.get();

            // Toggle partner status
            status = optional.get().getStatus();
            if (status.equalsIgnoreCase("true")) {
                status = "false";
                role = "user";
                Trainer trainer = trainerRepo.findByPartnerId(optional.get().getId());
                Center center = centerRepo.findByPartnerId(optional.get().getId());
                boolean isActivePartner = (trainer != null && trainer.getStatus().equalsIgnoreCase("true"))
                        || (center != null && center.getStatus().equalsIgnoreCase("true"));
                if (isActivePartner) {
                    return buildResponse(HttpStatus.BAD_REQUEST, "Cannot deactivate. Partner is already active");
                }

                responseMessage = "Partner account has be deactivated successfully";
            } else {
                status = "true";
                responseMessage = "Partner account has been successfully activated";
            }

            userRepo.updateUserRole(role, userId);
            partner.setStatus(status);
            partnerRepo.save(partner);
            emailUtilities.sendPartnerShipStatusMailToAdmins(status, optional.get().getUser().getEmail(), userRepo.getAllAdminsMail());
            emailUtilities.sendPartnerShipStatusMailToUser(status, optional.get().getUser().getEmail(), role);
            simpMessagingTemplate.convertAndSend("/topic/updatePartnerStatus", partner);
            return buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateFile(PartnerRequest request) throws JsonProcessingException {
        try {
            log.info("Inside updateFile", request);
            boolean isValid = request != null;

            if (!isValid) {
                return buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            // Attempt to find the partner by ID in the repository
            Optional<Partner> optional = partnerRepo.findById(request.getId());

            // Check if the partner with the given ID exists
            if (optional.isEmpty()) {
                return buildResponse(HttpStatus.NOT_FOUND, "Partner ID not found");
            }

            Partner existingPartner = optional.get();

            // Check if the logged-in user has permission to update the partner
            String currentUser = jwtFilter.getCurrentUser();
            if (!(jwtFilter.isAdmin() || existingPartner.getUser().getEmail().equals(currentUser))) {
                return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            // Check if the partner is already active
            if (existingPartner.getStatus().equalsIgnoreCase("true")) {
                return buildResponse(HttpStatus.UNAUTHORIZED, "Cannot make an update. Partner is now active");
            }

            // Update the partner's information based on the request data
            byte[] certificate = request.getCertificate().getBytes();
            existingPartner.setCertificate(certificate);
            byte[] cv = request.getCv().getBytes();
            existingPartner.setCv(cv);
            existingPartner.setCertificate(certificate);
            existingPartner.setLastUpdate(new Date());

            // Save the updated partner in the repository
            partnerRepo.save(existingPartner);

            simpMessagingTemplate.convertAndSend("/topic/updatePartnerFile", existingPartner);
            return buildResponse(HttpStatus.OK, "Partner files updated successfully");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<Partner> getPartner() {
        try {
            log.info("Inside getPartner", jwtFilter.getCurrentUser());

            // Get the current user's ID from the JWT token
            Integer userId = jwtFilter.getCurrentUserId();

            // Attempt to find the partner by ID in the repository by the user ID
            Partner existingPartner = partnerRepo.findByUserId(userId);

            // Check if the partner ID is valid before querying the repository
            if (existingPartner == null) {
                return new ResponseEntity<>(new Partner(), HttpStatus.NOT_FOUND);
            }

            // Check if the user is an admin or the owner of the partner data
            if (jwtFilter.isAdmin() || userId.equals(existingPartner.getUser().getId())) {
                return new ResponseEntity<>(existingPartner, HttpStatus.OK);
            } else {
                // Return a forbidden response if the user is not authorized to access the partner data
                return new ResponseEntity<>(new Partner(), HttpStatus.FORBIDDEN);
            }
        } catch (Exception ex) {
            // Handle exceptions and return an internal server error response
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> rejectApplication(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside rejectApplication {}", id);

            if (!jwtFilter.isAdmin()) {
                return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            Optional<Partner> optional = partnerRepo.findById(id);

            if (optional.isEmpty()) {
                return buildResponse(HttpStatus.NOT_FOUND, "Partner id not found");
            }
            // Get partner's email and role for sending notification
            String email = optional.get().getUser().getEmail();
            String role = optional.get().getRole();
            String response = email + " partnership application " + role + " has been rejected";

            // Send application rejection notification
            emailUtilities.sendPartnershipFailedMail(email, role);
            simpMessagingTemplate.convertAndSend("/topic/rejectPartnerApplication", response);
            return buildResponse(HttpStatus.OK, "Mail sent successfully. Application rejected");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<Partner>> getActivePartners() {
        try {
            log.info("Inside getActivePartners");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<Partner> partners = partnerRepo.getActivePartners();
            return new ResponseEntity<>(partners, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    /**
     * Creates and returns a new Partner object based on the provided requestMap and userId.
     *
     * @param request A map containing partner data from the request
     * @param user  The user associated with the partner
     * @return The created Partner object
     */
    private Partner getPartnerFromMap(PartnerRequest request, User user) throws IOException {
        // Initialize Partner and User objects
        Partner partner = new Partner();
        partner.setUser(user);

        // Set partner attributes from the requestMap
        byte[] certificate = request.getCertificate().getBytes();
        partner.setCertificate(certificate);
        partner.setMotivation(request.getMotivation());
        byte[] cv = request.getCv().getBytes();
        partner.setCv(cv);
        partner.setFacebookUrl(request.getFacebookUrl());
        partner.setInstagramUrl(request.getInstagramUrl());
        partner.setYoutubeUrl(request.getYoutubeUrl());
        partner.setRole(request.getRole());
        partner.setDate(new Date());
        partner.setLastUpdate(new Date());
        partner.setStatus("false");
        Partner savedPartner = partnerRepo.save(partner);
        simpMessagingTemplate.convertAndSend("/topic/getPartnerFromMap", savedPartner);
        return partner;
    }

    /**
     * Checks if the provided role value is valid.
     *
     * @param role The role value to be validated
     * @return True if the role is valid, false otherwise
     */
    private boolean isValidRole(String role) {
        List<String> validRoles = Arrays.asList("store", "driver", "trainer", "center");
        String lowercaseRole = role.toLowerCase();
        return validRoles.contains(lowercaseRole);
    }

    /**
     * Validates the provided request map for partner data against required fields.
     *
     * @param requestMap The request map containing partner data
     * @return True if the request map contains all required fields, false otherwise
     */
    private boolean validatePartnerMap(Map<String, String> requestMap) {
        return requestMap.containsKey("motivation")
                && requestMap.containsKey("role")
                && requestMap.containsKey("id")
                || requestMap.containsKey("facebookUrl")
                || requestMap.containsKey("instagramUrl")
                || requestMap.containsKey("youtubeUrl");
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

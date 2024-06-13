package com.berliz.serviceImplement;

import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.Center;
import com.berliz.models.Testimonial;
import com.berliz.models.User;
import com.berliz.repositories.CenterRepo;
import com.berliz.repositories.TestimonialRepo;
import com.berliz.repositories.UserRepo;
import com.berliz.services.TestimonialService;
import com.berliz.utils.BerlizUtilities;
import com.berliz.utils.EmailUtilities;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;

@Slf4j
@Service
public class TestimonialServiceImplement implements TestimonialService {

    @Autowired
    CenterRepo centerRepo;

    @Autowired
    TestimonialRepo testimonialRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    JWTFilter jwtFilter;

    @Autowired
    EmailUtilities emailUtilities;

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public ResponseEntity<String> addTestimonial(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside addTestimonial {}", requestMap);
            boolean isValid = validateRequestFromMap(requestMap, false);
            log.info("Is request valid? {}", isValid);

            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            if (jwtFilter.isAdmin()) {
                if (requestMap.get("email").isEmpty()) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Admin must provide user email");
                }

                User user = userRepo.findByEmail(requestMap.get("email"));
                if (user == null) {
                    return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "User email not found in db");
                }

                String userRole = user.getRole();
                boolean validateAdminRole = userRole.equalsIgnoreCase("admin");
                if (validateAdminRole) {
                    return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, "Admin not allowed");
                }

                Testimonial testimonial = testimonialRepo.findByUser(user);
                if (testimonial != null) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "User has a testimonial already");
                }
                getTestimonialFromMap(requestMap, user);
                return BerlizUtilities.buildResponse(HttpStatus.OK, "You have successfully added "
                        + user.getFirstname() + " testimonial");
            } else {
                User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
                if (user == null) {
                    return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, "Invalid user");
                }

                Testimonial testimonial = testimonialRepo.findByUser(user);
                if (testimonial != null) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Your have a testimonial already");
                }
                getTestimonialFromMap(requestMap, user);
                return BerlizUtilities.buildResponse(HttpStatus.OK, "Hello "
                        + user.getFirstname() + " your testimonial has been saved successfully");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }


    @Override
    public ResponseEntity<List<Testimonial>> getAllTestimonials() {
        try {
            log.info("Inside getAllTestimonials");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<Testimonial> testimonials = testimonialRepo.findAll();
            return new ResponseEntity<>(testimonials, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Testimonial>> getActiveTestimonials() {
        try {
            log.info("Inside getActiveTestimonials");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<Testimonial> testimonials = testimonialRepo.findAll();
            return new ResponseEntity<>(testimonials, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateTestimonial(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside updateTestimonial {}", requestMap);
            boolean isValid = validateRequestFromMap(requestMap, true);
            log.info("Is request valid? {}", isValid);
            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            Optional<Testimonial> optional = testimonialRepo.findById(Integer.valueOf(requestMap.get("id")));
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Payment ID not found");
            }

            Testimonial testimonial = optional.get();
            String currentUser = jwtFilter.getCurrentUserEmail();
            if (!(jwtFilter.isAdmin() || testimonial.getUser().getEmail().equals(currentUser))) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (testimonial.getStatus().equalsIgnoreCase("true")) {
                if (jwtFilter.isAdmin()) {
                    return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, "Cannot make an update. " +
                            "Testimonial is now active");
                } else {
                    return BerlizUtilities.buildResponse(HttpStatus.OK, "Sorry " +
                            testimonial.getUser().getFirstname() + ", you cannot make an update. " +
                            " Your testimonial is now active");
                }
            }

            Center center = centerRepo.findByCenterId(Integer.valueOf(requestMap.get("centerId")));
            testimonial.setCenter(center);
            testimonial.setTestimonial(requestMap.get("testimonial"));
            testimonial.setLikes(0);
            testimonial.setLastUpdate(new Date());
            Testimonial savedTestimonial = testimonialRepo.save(testimonial);
            String responseMessage;
            if (jwtFilter.isAdmin()) {
                responseMessage = "Payment updated successfully";
            } else {
                responseMessage = "Hello " +
                        testimonial.getUser().getFirstname() + " you have successfully " +
                        " updated your testimonial information";
            }

            String adminNotificationMessage = "Testimonial with id: " + savedTestimonial.getId() + ", and info: "
                    + savedTestimonial.getTestimonial() + ", information has been updated";
            String notificationMessage = "Your testimonial information has been updated : "
                    + savedTestimonial.getTestimonial();
            jwtFilter.sendNotifications("/topic/updateTestimonial", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, savedTestimonial);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
            return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
        }
    }

    @Override
    public ResponseEntity<String> deleteTestimonial(Integer id) throws JsonProcessingException {
        try {
            log.info("inside deleteTestimonial {}", id);
            if (!jwtFilter.isAdmin()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            Optional<Testimonial> optional = testimonialRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Testimonial not found");
            }
            log.info("inside optional {}", optional);
            try {
                Testimonial testimonial = optional.get();
                testimonialRepo.deleteById(id);
                String adminNotificationMessage = "Testimonial with id: " + testimonial.getId() + ", and info: "
                        + testimonial.getTestimonial() + ", has been deleted";
                String notificationMessage = "You have successfully deleted your testimonial: "
                        + testimonial.getTestimonial();
                jwtFilter.sendNotifications("/topic/deleteTestimonial", adminNotificationMessage,
                        jwtFilter.getCurrentUser(), notificationMessage, testimonial);
                return BerlizUtilities.buildResponse(HttpStatus.OK, "Testimonial deleted successfully");
            } catch (DataIntegrityViolationException ex) {
                // Handle foreign key constraint violation when deleting
                ex.printStackTrace();
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Cannot delete testimonial due to a foreign key constraint violation.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside updateStatus {}", id);
            String status;
            if (!jwtFilter.isAdmin()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            Optional<Testimonial> optional = testimonialRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Testimonial not found");
            }
            log.info("Inside optional {}", optional);
            status = optional.get().getStatus();
            Testimonial testimonial = optional.get();
            String responseMessage;
            if (status.equalsIgnoreCase("true")) {
                status = "false";
                responseMessage = "Testimonial Status updated successfully. Now Deactivated";
            } else {
                status = "true";
                responseMessage = "Testimonial Status updated successfully. Now Activated";
            }

            testimonial.setStatus(status);
            testimonialRepo.save(testimonial);
            String adminNotificationMessage = "Testimonial with id: " + testimonial.getId() +
                    ", status has been set to " + status;
            String notificationMessage = "You have successfully set your testimonial status to: " + status;
            jwtFilter.sendNotifications("/topic/updateTestimonialStatus", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, testimonial);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (
                Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<Testimonial> getTestimonial(Integer id) {
        try {
            log.info("Inside getPayment {}", id);
            Optional<Testimonial> optional = testimonialRepo.findById(id);
            if (optional.isPresent()) {
                return new ResponseEntity<>(optional.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new Testimonial(), HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Testimonial(), HttpStatus.NOT_FOUND);
    }

    private void getTestimonialFromMap(Map<String, String> requestMap, User user) throws ParseException {
        Testimonial testimonial = new Testimonial();
        Center center = centerRepo.findByCenterId(Integer.valueOf(requestMap.get("centerId")));
        testimonial.setUser(user);
        testimonial.setCenter(center);
        testimonial.setTestimonial(requestMap.get("testimonial"));
        testimonial.setLikes(0);
        testimonial.setDate(new Date());
        testimonial.setLastUpdate(new Date());
        testimonial.setStatus("false");
        Testimonial savedTestimonial = testimonialRepo.save(testimonial);
        String adminNotificationMessage = "A new testimonial with id: " + savedTestimonial.getId()
                + " and info" + savedTestimonial.getTestimonial() + ", has been added";
        String notificationMessage = "You have successfully added a new testimonial: "
                + savedTestimonial.getTestimonial();
        jwtFilter.sendNotifications("/topic/getTestimonialFromMap", adminNotificationMessage,
                jwtFilter.getCurrentUser(), notificationMessage, savedTestimonial);
    }

    private boolean validateRequestFromMap(Map<String, String> requestMap, boolean validId) {
        if (validId) {
            return requestMap.containsKey("id")
                    && requestMap.containsKey("centerId")
                    && requestMap.containsKey("testimonial");
        } else {
            return requestMap.containsKey("centerId")
                    && requestMap.containsKey("testimonial");
        }
    }
}

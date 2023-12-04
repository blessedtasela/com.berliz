package com.berliz.serviceImplement;

import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.*;
import com.berliz.repository.*;
import com.berliz.services.SubscriptionService;
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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
public class SubscriptionServiceImplement implements SubscriptionService {


    @Autowired
    MemberRepo memberRepo;

    @Autowired
    TrainerRepo trainerRepo;

    @Autowired
    CenterRepo centerRepo;

    @Autowired
    PaymentRepo paymentRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    JWTFilter jwtFilter;

    @Autowired
    EmailUtilities emailUtilities;

    @Autowired
    SubscriptionRepo subscriptionRepo;

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public ResponseEntity<String> addSubscription(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside addMember {}", requestMap);
            boolean isValid = validateRequestFromMap(requestMap, false);
            log.info("Is request valid? {}", isValid);

            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            if (jwtFilter.isAdmin()) {
                if (requestMap.get("id").isEmpty()) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Admin must provide userId");
                }

                User user = userRepo.findByEmail(requestMap.get("email"));
                if (user == null) {
                    return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "User email not found in db");
                }

                String userRole = user.getRole();
                boolean validateAdminRole = userRole.equalsIgnoreCase("admin");
                if (validateAdminRole) {
                    return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, "Admin cannot be a client");
                }

                Subscription subscription = subscriptionRepo.findActiveSubscriptionByUser(user);
                if (subscription != null) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "User has an active subscription already. " +
                            "Please cancel all subscriptions to continue");
                }

                getSubscriptionFromMap(requestMap, user);
                return BerlizUtilities.buildResponse(HttpStatus.OK, "You have successfully added "
                        + user.getFirstname() + " as a client");
            } else {
                Integer userId = jwtFilter.getCurrentUserId();
                User user = userRepo.findByEmail(jwtFilter.getCurrentUser());
                if (user == null) {
                    return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, "Invalid user");
                }

                Subscription subscription = subscriptionRepo.findActiveSubscriptionByUser(user);
                if (subscription != null) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Your subscription is active. " +
                            "Please cancel all subscriptions to add a new one");
                }
                getSubscriptionFromMap(requestMap, user);
                return BerlizUtilities.buildResponse(HttpStatus.OK, "Hello "
                        + user.getFirstname() + " your information has been saved successfully");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<Subscription>> getAllSubscriptions() {
        try {
            log.info("Inside getAllSubscriptions");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<Subscription> subscriptions = subscriptionRepo.findAll();
            return new ResponseEntity<>(subscriptions, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Subscription>> getActiveSubscriptions() {
        try {
            log.info("Inside getActiveSubscriptions");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<Subscription> subscriptions = subscriptionRepo.getActiveSubscriptions();
            return new ResponseEntity<>(subscriptions, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateSubscription(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside updateSubscription {}", requestMap);
            boolean isValid = validateRequestFromMap(requestMap, true);
            log.info("Is request valid? {}", isValid);
            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            Optional<Subscription> optional = subscriptionRepo.findById(Integer.valueOf(requestMap.get("id")));
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Subscription ID not found");
            }

            Subscription subscription = optional.get();
            String currentUser = jwtFilter.getCurrentUser();
            if (!(jwtFilter.isAdmin() || subscription.getUser().getEmail().equals(currentUser))) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (subscription.getStatus().equalsIgnoreCase("true")) {
                if (jwtFilter.isAdmin()) {
                    return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, "Cannot make an update. Subscription is now active");
                } else {
                    return BerlizUtilities.buildResponse(HttpStatus.OK, "Sorry " +
                            subscription.getUser().getFirstname() + ", you cannot make an update. " +
                            " Your subscription is now active");
                }
            }

            User user = subscription.getUser();
            Payment payment = paymentRepo.findActivePaymentByUser(user);
            if (payment != null) {
                subscription.setPayment(payment);
            }
            Trainer trainer = trainerRepo.findByTrainerId(Integer.valueOf(requestMap.get("trainerId")));
            if (trainer != null) {
                subscription.setTrainer(trainer);
            }

            Center center = centerRepo.findByCenterId(Integer.valueOf(requestMap.get("centerId")));
            if (center != null) {
                subscription.setCenter(center);
            }


            String startDateString = requestMap.get("startDate");
            Integer months = Integer.valueOf(requestMap.get("months"));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = dateFormat.parse(startDateString);
            LocalDate startDateLocaleDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate endDateLocalDate = startDateLocaleDate.plusMonths(months);
            Date endDate = Date.from(endDateLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            subscription.setStartDate(startDate);
            subscription.setMonths(months);
            subscription.setEndDate(endDate);
            subscription.setLastUpdate(new Date());
            Subscription savedSubscription = subscriptionRepo.save(subscription);
            String responseMessage;
            if (jwtFilter.isAdmin()) {
                responseMessage = "Subscription updated successfully";
            } else {
                responseMessage = "Hello " +
                        subscription.getUser().getFirstname() + " you have successfully " +
                        " updated your subscription information";
            }

            simpMessagingTemplate.convertAndSend("/topic/updateClient", savedSubscription);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
            return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
        }
    }


    @Override
    public ResponseEntity<String> deleteSubscription(Integer id) throws JsonProcessingException {
        try {
            log.info("inside deleteSubscription {}", id);
            if (!jwtFilter.isAdmin()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            Optional<Subscription> optional = subscriptionRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Subscription not found");
            }
            log.info("inside optional {}", optional);
            try {
                Subscription subscription = optional.get();
                subscriptionRepo.deleteById(id);
                simpMessagingTemplate.convertAndSend("/topic/deleteSubscription", subscription);
                return BerlizUtilities.buildResponse(HttpStatus.OK, "Subscription deleted successfully");
            } catch (DataIntegrityViolationException ex) {
                // Handle foreign key constraint violation when deleting
                ex.printStackTrace();
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Cannot delete subscription due to a foreign key constraint violation.");
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
            Optional<Subscription> optional = subscriptionRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Subscription not found");
            }
            log.info("Inside optional {}", optional);
            status = optional.get().getStatus();
            Subscription subscription = optional.get();
            String responseMessage;
            if (status.equalsIgnoreCase("true")) {
                status = "false";
                responseMessage = "Subscription Status updated successfully. Now Deactivated";
            } else {
                status = "true";
                responseMessage = "Subscription Status updated successfully. Now Activated";
            }

            subscription.setStatus(status);
            subscriptionRepo.save(subscription);
            simpMessagingTemplate.convertAndSend("/topic/updateSubscriptionStatus", subscription);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (
                Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<Subscription> getSubscription(Integer id) {
        try {
            log.info("Inside getSubscription {}", id);
            Optional<Subscription> optional = subscriptionRepo.findById(id);
            if (optional.isPresent()) {
                return new ResponseEntity<>(optional.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new Subscription(), HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Subscription(), HttpStatus.OK);
    }

    private void getSubscriptionFromMap(Map<String, String> requestMap, User user) throws ParseException {
        Subscription subscription = new Subscription();
        subscription.setUser(user);

        Payment payment = paymentRepo.findActivePaymentByUser(user);
        if (payment != null) {
            subscription.setPayment(payment);
        }
        Trainer trainer = trainerRepo.findByTrainerId(Integer.valueOf(requestMap.get("trainerId")));
        if (trainer != null) {
            subscription.setTrainer(trainer);
        }

        Center center = centerRepo.findByCenterId(Integer.valueOf(requestMap.get("centerId")));
        if (center != null) {
            subscription.setCenter(center);
        }


        String startDateString = requestMap.get("startDate");
        Integer months = Integer.valueOf(requestMap.get("months"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = dateFormat.parse(startDateString);
        LocalDate startDateLocaleDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDateLocalDate = startDateLocaleDate.plusMonths(months);
        Date endDate = Date.from(endDateLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        subscription.setStartDate(startDate);
        subscription.setMonths(months);
        subscription.setEndDate(endDate);
        subscription.setDate(new Date());
        subscription.setLastUpdate(new Date());
        subscription.setStatus("false");
        Subscription savedSubscription = subscriptionRepo.save(subscription);
        simpMessagingTemplate.convertAndSend("/topic/getSubscriptionFromMap", savedSubscription);
    }

    private boolean validateRequestFromMap(Map<String, String> requestMap, boolean validId) {
        if (validId) {
            return requestMap.containsKey("id")
                    && requestMap.containsKey("startDate")
                    && requestMap.containsKey("months");
        } else {
            return requestMap.containsKey("startDate")
                    && requestMap.containsKey("months");
        }
    }

}

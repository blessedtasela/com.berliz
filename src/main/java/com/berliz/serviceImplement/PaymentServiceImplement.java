package com.berliz.serviceImplement;

import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.Payment;
import com.berliz.models.Subscription;
import com.berliz.models.User;
import com.berliz.repositories.*;
import com.berliz.services.PaymentService;
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
public class PaymentServiceImplement implements PaymentService {

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
    public ResponseEntity<String> addPayment(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside addMember {}", requestMap);
            boolean isValid = validateRequestFromMap(requestMap, false);
            log.info("Is request valid? {}", isValid);

            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            if (jwtFilter.isAdmin()) {
                if (requestMap.get("userId").isEmpty()) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Admin must provide userId");
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

                Payment payment = paymentRepo.findActivePaymentByUser(user);
                if (payment != null) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "User has an active payment already. " +
                            "Please cancel all subscriptions to continue");
                }
                Subscription subscription = subscriptionRepo.findActiveSubscriptionByUser(user);
                if (subscription == null) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "User has no active subscription. " +
                            "Please add a subscription to continue");
                }
                getPaymentFromMap(requestMap);
                return BerlizUtilities.buildResponse(HttpStatus.OK, "You have successfully added "
                        + user.getFirstname() + " as a client");
            } else {
                User user = userRepo.findByEmail(jwtFilter.getCurrentUser());
                if (user == null) {
                    return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, "Invalid user");
                }

                Payment payment = paymentRepo.findActivePaymentByUser(user);
                if (payment != null) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Your payment is active. " +
                            "Please cancel all payments to add a new one");
                }
                getPaymentFromMap(requestMap);
                return BerlizUtilities.buildResponse(HttpStatus.OK, "Hello "
                        + user.getFirstname() + " your payment has been saved successfully");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<Payment>> getAllPayments() {
        try {
            log.info("Inside getAllPayments");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<Payment> payments = paymentRepo.findAll();
            return new ResponseEntity<>(payments, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Payment>> getActivePayments() {
        try {
            log.info("Inside getActivePayments");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<Payment> payments = paymentRepo.getActivePayments();
            return new ResponseEntity<>(payments, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updatePayment(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside updatePayment {}", requestMap);
            boolean isValid = validateRequestFromMap(requestMap, true);
            log.info("Is request valid? {}", isValid);
            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            Optional<Payment> optional = paymentRepo.findById(Integer.valueOf(requestMap.get("id")));
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Payment ID not found");
            }

            Payment payment = optional.get();
            String currentUser = jwtFilter.getCurrentUser();
            if (!(jwtFilter.isAdmin() || payment.getUser().getEmail().equals(currentUser))) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (payment.getStatus().equalsIgnoreCase("true")) {
                if (jwtFilter.isAdmin()) {
                    return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, "Cannot make an update. " +
                            "Payment is now active");
                } else {
                    return BerlizUtilities.buildResponse(HttpStatus.OK, "Sorry " +
                            payment.getUser().getFirstname() + ", you cannot make an update. " +
                            " Your payment is now active");
                }
            }

            Subscription subscription = subscriptionRepo.findActiveSubscriptionByUser(payment.getUser());
            if (subscription != null) {
                payment.setSubscription(subscription);
            }

            payment.setPaymentMethod(requestMap.get("paymentMethod"));
            payment.setAmount(Double.parseDouble(requestMap.get("amount")));
            payment.setLastUpdate(new Date());
            Payment savedPayment = paymentRepo.save(payment);
            String responseMessage;
            if (jwtFilter.isAdmin()) {
                responseMessage = "Payment updated successfully";
            } else {
                responseMessage = "Hello " +
                        payment.getUser().getFirstname() + " you have successfully " +
                        " updated your payment information";
            }

            simpMessagingTemplate.convertAndSend("/topic/updatePayment", savedPayment);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
            return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
        }
    }

    @Override
    public ResponseEntity<String> deletePayment(Integer id) throws JsonProcessingException {
        try {
            log.info("inside deletePayment {}", id);
            if (!jwtFilter.isAdmin()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            Optional<Payment> optional = paymentRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Payment not found");
            }
            log.info("inside optional {}", optional);
            try {
                Payment payment = optional.get();
                paymentRepo.deleteById(id);
                simpMessagingTemplate.convertAndSend("/topic/deletePayment", payment);
                return BerlizUtilities.buildResponse(HttpStatus.OK, "Payment deleted successfully");
            } catch (DataIntegrityViolationException ex) {
                // Handle foreign key constraint violation when deleting
                ex.printStackTrace();
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Cannot delete payment due to a foreign key constraint violation.");
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
            Optional<Payment> optional = paymentRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Payment not found");
            }
            log.info("Inside optional {}", optional);
            status = optional.get().getStatus();
            Payment payment = optional.get();
            String responseMessage;
            if (status.equalsIgnoreCase("true")) {
                status = "false";
                responseMessage = "Payment Status updated successfully. Now Deactivated";
            } else {
                status = "true";
                responseMessage = "Payment Status updated successfully. Now Activated";
            }

            payment.setStatus(status);
            paymentRepo.save(payment);
            simpMessagingTemplate.convertAndSend("/topic/updatePaymentStatus", payment);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (
                Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<Payment> getPayment(Integer id) {
        try {
            log.info("Inside getPayment {}", id);
            Optional<Payment> optional = paymentRepo.findById(id);
            if (optional.isPresent()) {
                return new ResponseEntity<>(optional.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new Payment(), HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Payment(), HttpStatus.NOT_FOUND);
    }

    private void getPaymentFromMap(Map<String, String> requestMap) throws ParseException {
        User payer = userRepo.findByEmail(jwtFilter.getCurrentUser());
        User user = userRepo.findByEmail(requestMap.get("email"));
        Payment payment = new Payment();

        Subscription subscription = subscriptionRepo.findActiveSubscriptionByUser(user);
        if (subscription != null) {
            payment.setSubscription(subscription);
        }

//        String startDateString = requestMap.get("startDate");
//        Integer months = Integer.valueOf(requestMap.get("months"));
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//        Date startDate = dateFormat.parse(startDateString);
//        LocalDate startDateLocaleDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//        LocalDate endDateLocalDate = startDateLocaleDate.plusMonths(months);
//        Date endDate = Date.from(endDateLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
//        subscription.setStartDate(startDate);
//        subscription.setMonths(months);
//        subscription.setEndDate(endDate);

        payment.setUser(user);
        payment.setPayer(payer);
        payment.setPaymentMethod(requestMap.get("paymentMethod"));
        payment.setAmount(Double.parseDouble(requestMap.get("amount")));
        payment.setDate(new Date());
        payment.setLastUpdate(new Date());
        payment.setStatus("false");
        Payment savedPayment = paymentRepo.save(payment);
        simpMessagingTemplate.convertAndSend("/topic/getSubscriptionFromMap", savedPayment);
    }

    private boolean validateRequestFromMap(Map<String, String> requestMap, boolean validId) {
        if (validId) {
            return requestMap.containsKey("id")
                    && requestMap.containsKey("paymentMethod")
                    && requestMap.containsKey("amount");
        } else {
            return requestMap.containsKey("paymentMethod")
                    && requestMap.containsKey("amount");
        }
    }
}

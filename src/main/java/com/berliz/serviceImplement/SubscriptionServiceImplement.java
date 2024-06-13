package com.berliz.serviceImplement;

import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.*;
import com.berliz.repositories.*;
import com.berliz.services.SubscriptionService;
import com.berliz.utils.BerlizUtilities;
import com.berliz.utils.EmailUtilities;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    CategoryRepo categoryRepo;

    @Autowired
    TrainerPricingRepo trainerPricingRepo;

    @Autowired
    CenterPricingRepo centerPricingRepo;

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
                if (requestMap.get("userId").isEmpty()) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Admin must provide userId");
                }

                User user = userRepo.findByUserId(Integer.valueOf(requestMap.get("userId")));
                if (user == null) {
                    return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "User email not found in db");
                }

                if (jwtFilter.isAccountIncomplete(user)) {
                    return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED,
                            "User account registration is incomplete. Please update user account information.");
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
                        + user.getFirstname() + " subscription");
            } else {
                User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
                if (user == null) {
                    return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, "Invalid user");
                }

                if (jwtFilter.isAccountIncomplete(user)) {
                    return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED,
                            "Account registration is incomplete. Please update account information to continue.");
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
            String currentUser = jwtFilter.getCurrentUserEmail();
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

            Trainer trainer = trainerRepo.findByTrainerId(Integer.valueOf(requestMap.get("trainerId")));
            if (trainer != null) {
                subscription.setTrainer(trainer);
            }

            Center center = centerRepo.findByCenterId(Integer.valueOf(requestMap.get("centerId")));
            if (center != null) {
                subscription.setCenter(center);
            }


            String startDateString = requestMap.get("startDate");
            int months = Integer.parseInt(requestMap.get("months"));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = dateFormat.parse(startDateString);
            LocalDate startDateLocaleDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate endDateLocalDate = startDateLocaleDate.plusMonths(months);
            Date endDate = Date.from(endDateLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            subscription.setStartDate(startDate);
            subscription.setMonths(months);
            subscription.setEndDate(endDate);
            subscription.setAmount(calculateSubscriptionAmount(requestMap));
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

            String adminNotificationMessage = "SSubscription with id: " + savedSubscription.getId() + ", and info: "
                    + savedSubscription.getEndDate() + ", information has been updated";
            String notificationMessage = "Your subscription information has been updated : "
                    + savedSubscription.getEndDate();
            jwtFilter.sendNotifications("/topic/updateSubscription", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, savedSubscription);
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
                String adminNotificationMessage = "Subscription with id: " + subscription.getId() + ", and info: "
                        + subscription.getEndDate() + ", has been deleted";
                String notificationMessage = "You have successfully deleted your subscription: "
                        + subscription.getEndDate();
                jwtFilter.sendNotifications("/topic/deleteSubscription", adminNotificationMessage,
                        jwtFilter.getCurrentUser(), notificationMessage, subscription);
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
            String adminNotificationMessage = "Subscription with id: " + subscription.getId() +
                    ", status has been set to " + status;
            String notificationMessage = "You have successfully set your subscription status to: " + status;
            jwtFilter.sendNotifications("/topic/updateSubscriptionStatus", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, subscription);
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
            return optional.map(subscription -> new ResponseEntity<>(subscription, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(new Subscription(), HttpStatus.NOT_FOUND));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Subscription(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<Subscription>> getMySubscriptions() {
        try {
            log.info("Inside getMySubscriptions");
            if (jwtFilter.getCurrentUser() == null) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);
            }

            List<Subscription> subscriptions = subscriptionRepo.findByUser(jwtFilter.getCurrentUser());
            if (subscriptions.isEmpty()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>(subscriptions, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> bulkAction(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("inside bulkAction {}", requestMap);
            if (!jwtFilter.isBerlizUser()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            boolean isValid = !requestMap.isEmpty();
            log.info("Is request valid? {}", isValid);

            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            String idString = requestMap.get("ids");
            String[] idArray = idString.split(",");
            if (idString.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "No subscription selected");
            }

            List<Integer> idList = Arrays.stream(idArray)
                    .map(Integer::valueOf)
                    .toList();
            List<Subscription> subscriptions = subscriptionRepo.findAllById(idList);
            if (subscriptions.isEmpty() || subscriptions.size() != idList.size()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Subscription id not found");
            }

            log.info("inside optional {}", requestMap);
            boolean isDelete = requestMap.get("action").equalsIgnoreCase("delete");
            boolean isTrue = requestMap.get("action").equalsIgnoreCase("true");
            boolean isFalse = requestMap.get("action").equalsIgnoreCase("false");
            User user = null;
            for (Subscription subscription : subscriptions) {
                user = subscription.getUser();
                break;
            }

            List<Subscription> subscriptionArrayList = new ArrayList<>();
            boolean isSubscription = false;
            if (jwtFilter.isAdmin()) {
                isSubscription = true;
            } else {
                for (Integer subscriptionId : idList) {
                    Optional<Subscription> subscriptionOptional = subscriptionRepo.findById(subscriptionId);
                    if (subscriptionOptional.isEmpty()) {
                        isSubscription = false;
                        break;
                    }

                    Subscription subscription = subscriptionOptional.get();
                    isSubscription = subscriptionRepo.existsByUser(subscription.getUser());
                }
            }

            if (!isSubscription) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            String successMessage = "";
            if (!(isDelete ^ isFalse ^ isTrue)) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Action not recognized");
            }

            if (isDelete) {
                int updatedCount = subscriptionRepo.bulkDeleteByIds(idList);
                if (updatedCount < 0) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.SOMETHING_WENT_WRONG);
                }
                successMessage = "All subscriptions deleted successfully";
            }

            if (isFalse) {
                int updatedCount = subscriptionRepo.bulkUpdateStatusByIds(idList, "false");
                if (updatedCount < 0) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.SOMETHING_WENT_WRONG);
                }
                successMessage = "All subscriptions are now inactive";
            }

            if (isTrue) {
                int updatedCount = subscriptionRepo.bulkUpdateStatusByIds(idList, "true");
                if (updatedCount < 0) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.SOMETHING_WENT_WRONG);
                }
                successMessage = "All subscriptions are now active";
            }

            assert user != null;
            String adminNotificationMessage = successMessage + " with ids: " + idList
                    + ", for " + user.getEmail() + " and bulk action done";
            String notificationMessage = "You have perform a bulk action and " + successMessage;
            jwtFilter.sendNotifications("/topic/subscriptionBulkAction", adminNotificationMessage,
                    user, notificationMessage, subscriptions);
            return BerlizUtilities.buildResponse(HttpStatus.OK, successMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    private void getSubscriptionFromMap(Map<String, String> requestMap, User user) {
        Subscription subscription = new Subscription();
        subscription.setUser(user);

        Trainer trainer = trainerRepo.findByTrainerId(Integer.valueOf(requestMap.get("trainerId")));
        if (trainer != null) {
            subscription.setTrainer(trainer);
        }

        Center center = centerRepo.findByCenterId(Integer.valueOf(requestMap.get("centerId")));
        if (center != null) {
            subscription.setCenter(center);
        }

        String categoryIdsString = requestMap.get("categoryIds");
        if (!categoryIdsString.isEmpty()) {
            String[] categoryIdsArray = categoryIdsString.split(",");
            Set<Category> categories = new HashSet<>();
            for (String categoryIdString : categoryIdsArray) {
                int categoryId = Integer.parseInt(categoryIdString.trim());
                Category category = categoryRepo.findById(categoryId)
                        .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + categoryId));
                categories.add(category);
            }
            subscription.setCategories(categories);
        }

        BigDecimal totalAmount = calculateSubscriptionAmount(requestMap);
        subscription.setAmount(totalAmount);
        subscription.setMode(requestMap.get("mode"));
        subscription.setMonths(Integer.valueOf(requestMap.get("months")));
        subscription.setDate(new Date());
        subscription.setLastUpdate(new Date());
        subscription.setStatus("false");
        Subscription savedSubscription = subscriptionRepo.save(subscription);
        String adminNotificationMessage = "A new subscription with id: " + savedSubscription.getId()
                + " and info" + savedSubscription.getEndDate() + ", has been added";
        String notificationMessage = "You have successfully added a new exercise: "
                + savedSubscription.getEndDate();
        jwtFilter.sendNotifications("/topic/getSubscriptionFromMap", adminNotificationMessage,
                jwtFilter.getCurrentUser(), notificationMessage, savedSubscription);
    }

    private boolean validateRequestFromMap(Map<String, String> requestMap, boolean validId) {
        if (validId) {
            return requestMap.containsKey("id")
                    && requestMap.containsKey("trainerId")
                    && requestMap.containsKey("centerId")
                    && requestMap.containsKey("categoryIds")
                    && requestMap.containsKey("mode")
                    && requestMap.containsKey("months");
        } else {
            return requestMap.containsKey("trainerId")
                    && requestMap.containsKey("centerId")
                    && requestMap.containsKey("categoryIds")
                    && requestMap.containsKey("mode")
                    && requestMap.containsKey("months");
        }
    }

    public BigDecimal calculateSubscriptionAmount(Map<String, String> requestMap) {
        // Retrieve necessary data from requestMap
        int trainerId = Integer.parseInt(requestMap.get("trainerId"));
        int centerId = Integer.parseInt(requestMap.get("centerId"));
        String mode = requestMap.get("mode");
        int months = Integer.parseInt(requestMap.get("months"));

        // Fetch Trainer and Center entities from repositories
        Trainer trainer = trainerRepo.findByTrainerId(trainerId);
        Center center = centerRepo.findByCenterId(centerId);

        // Initialize pricing information variables
        TrainerPricing trainerPricing = null;
        CenterPricing centerPricing = null;

        // Fetch pricing information only if the corresponding entity is not null
        if (trainer != null) {
            trainerPricing = trainerPricingRepo.findByTrainer(trainer);
        }

        if (center != null) {
            centerPricing = centerPricingRepo.findByCenter(center);
        }

        // Get the count of selected categories from the form control
        String categoryIdsString = requestMap.get("categoryIds");
        int selectedCategoriesCount = 0;

        if (categoryIdsString != null && !categoryIdsString.isEmpty()) {
            String[] categoryIdsArray = categoryIdsString.split(",");
            selectedCategoriesCount = categoryIdsArray.length;
        }

        // Calculate subscription amount based on pricing information
        BigDecimal trainerAmount = (trainer != null) ? calculateTrainerAmount(trainerPricing, mode, months, selectedCategoriesCount) : new BigDecimal("0");
        BigDecimal centerAmount = (center != null) ? calculateCenterAmount(centerPricing, months, selectedCategoriesCount) : new BigDecimal("0");

        // Total amount is the sum of trainer and center amounts
        return trainerAmount.add(centerAmount);
    }

    private BigDecimal calculateTrainerAmount(TrainerPricing trainerPricing, String mode, int months, int categoryCount) {
        BigDecimal basePrice = null;
        switch (mode) {
            case "online" -> basePrice = trainerPricing.getPriceOnline();
            case "hybrid" -> basePrice = trainerPricing.getPriceHybrid();
            case "personal" -> basePrice = trainerPricing.getPricePersonal();
            default -> {
            }
        }

        // Calculate discount based on months
        BigDecimal discount = new BigDecimal("0");
        if (months == 3) {
            discount = trainerPricing.getDiscount3Months();
        } else if (months == 6) {
            discount = trainerPricing.getDiscount6Months();
        } else if (months == 9) {
            discount = trainerPricing.getDiscount9Months();
        } else if (months == 12) {
            discount = trainerPricing.getDiscount12Months();
        }

        // Calculate the discount
        BigDecimal discountMultiplier = discount.divide(new BigDecimal("100"), RoundingMode.HALF_UP);
        assert basePrice != null;
        BigDecimal discountAmount = basePrice.multiply(discountMultiplier);
        BigDecimal discountedPrice = basePrice.subtract(discountAmount);

        // Calculate 2 categories discount
        BigDecimal twoCategoriesDiscount = calculate2CategoriesDiscount(basePrice, trainerPricing.getDiscount2Programs(), categoryCount);

        // Add 2 categories discount to the total amount
        return discountedPrice.multiply(new BigDecimal(months)).subtract(twoCategoriesDiscount);
    }

    private BigDecimal calculateCenterAmount(CenterPricing centerPricing, int months, int categoryCount) {
        BigDecimal basePrice = centerPricing.getPrice();

        // Calculate discount based on months
        BigDecimal discount = new BigDecimal("0");
        if (months == 3) {
            discount = centerPricing.getDiscount3Months();
        } else if (months == 6) {
            discount = centerPricing.getDiscount6Months();
        } else if (months == 9) {
            discount = centerPricing.getDiscount9Months();
        } else if (months == 12) {
            discount = centerPricing.getDiscount12Months();
        }

        // Calculate the discount
        BigDecimal discountMultiplier = discount.divide(new BigDecimal("100"), RoundingMode.HALF_UP);
        assert basePrice != null;
        BigDecimal discountAmount = basePrice.multiply(discountMultiplier);
        BigDecimal discountedPrice = basePrice.subtract(discountAmount);

        // Calculate 2 categories discount
        BigDecimal twoCategoriesDiscount = calculate2CategoriesDiscount(basePrice, centerPricing.getDiscount2Programs(), categoryCount);

        // Add 2 categories discount to the total amount
        return discountedPrice.multiply(new BigDecimal(months)).subtract(twoCategoriesDiscount);
    }

    private BigDecimal calculate2CategoriesDiscount(BigDecimal basePrice, BigDecimal discount, int selectedCategoriesCount) {
        if (selectedCategoriesCount == 2) {
            BigDecimal discountMultiplier = discount.divide(new BigDecimal("100"), RoundingMode.HALF_UP);
            return basePrice.multiply(discountMultiplier);
        } else {
            return BigDecimal.ZERO;
        }
    }
}


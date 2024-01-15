package com.berliz.serviceImplement;

import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.Category;
import com.berliz.models.Member;
import com.berliz.models.Subscription;
import com.berliz.models.User;
import com.berliz.repositories.*;
import com.berliz.services.MemberService;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
public class MemberServiceImplement implements MemberService {

    @Autowired
    MemberRepo memberRepo;

    @Autowired
    CategoryRepo categoryRepo;

    @Autowired
    CenterRepo centerRepo;

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
    public ResponseEntity<String> addMember(Map<String, String> requestMap) throws JsonProcessingException {
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

                Member member = memberRepo.findByUser(user);
                if (member != null) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Member exists already");
                }

                getMemberFromMap(requestMap, user);
                return BerlizUtilities.buildResponse(HttpStatus.OK, "You have successfully added "
                        + user.getFirstname() + " as a client");
            } else {
                Integer userId = jwtFilter.getCurrentUserId();
                User user = userRepo.findByEmail(jwtFilter.getCurrentUser());
                if (user == null) {
                    return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, "Invalid user");
                }

                Member member = memberRepo.findByUser(user);
                if (member != null) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Client exists already");
                }
                getMemberFromMap(requestMap, user);
                return BerlizUtilities.buildResponse(HttpStatus.OK, "Hello "
                        + user.getFirstname() + " your information has been saved successfully");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<Member>> getAllMembers() {
        try {
            log.info("Inside getAllMembers");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<Member> members = memberRepo.findAll();
            return new ResponseEntity<>(members, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Member>> getActiveMembers() {
        try {
            log.info("Inside getActiveMembers");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<Member> members = memberRepo.getActiveMembers();
            return new ResponseEntity<>(members, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateMember(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside updateMember {}", requestMap);
            boolean isValid = validateRequestFromMap(requestMap, true);
            log.info("Is request valid? {}", isValid);
            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            Optional<Member> optional = memberRepo.findById(Integer.valueOf(requestMap.get("id")));
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Member ID not found");
            }

            Member member = optional.get();
            String currentUser = jwtFilter.getCurrentUser();
            if (!(jwtFilter.isAdmin() || member.getUser().getEmail().equals(currentUser))) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (member.getStatus().equalsIgnoreCase("true")) {
                if (jwtFilter.isAdmin()) {
                    return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, "Cannot make an update. Member is now active");
                } else {
                    return BerlizUtilities.buildResponse(HttpStatus.OK, "Sorry " +
                            member.getUser().getFirstname() + ", you cannot make an update. " +
                            " Your membership is now active");
                }
            }

            User user = member.getUser();
            double height = Double.parseDouble(requestMap.get("height"));
            member.setHeight(height);
            double weight = Double.parseDouble(requestMap.get("weight"));
            member.setWeight(weight);
            String dobString = user.getDob();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date dob = dateFormat.parse(dobString);
            LocalDate dobLocalDate = dob.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate currentDate = LocalDate.now();
            Period period = Period.between(dobLocalDate, currentDate);
            Integer age = period.getYears();
            double BMI = weight / (height * height);
            double bodyFat = 1.2 * BMI + 0.23 * age - 5.4;
            member.setBodyFat(bodyFat);

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
                member.setCategories(categories);
            }

            List<Subscription> subscriptions = subscriptionRepo.findByUser(user);
            if (!subscriptions.isEmpty()) {
                Set<Subscription> subscriptionSet = new HashSet<>(subscriptions);
                member.setSubscriptions(subscriptionSet);
            }

            member.setMotivation(requestMap.get("motivation"));
            member.setTargetWeight(Double.parseDouble(requestMap.get("targetWeight")));
            member.setMedicalConditions(requestMap.get("medicalConditions"));
            member.setLastUpdate(new Date());
            Member savedMember = memberRepo.save(member);
            String responseMessage;
            if (jwtFilter.isAdmin()) {
                responseMessage = "Client updated successfully";
            } else {
                responseMessage = "Hello " +
                        member.getUser().getFirstname() + " you have successfully " +
                        " updated your client information";
            }

            simpMessagingTemplate.convertAndSend("/topic/updateClient", savedMember);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
            return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
        }
    }

    @Override
    public ResponseEntity<String> deleteMember(Integer id) throws JsonProcessingException {
        try {
            log.info("inside deleteMember {}", id);
            if (!jwtFilter.isAdmin()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            Optional<Member> optional = memberRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Member not found");
            }
            log.info("inside optional {}", optional);
            try {
                Member member = optional.get();
                memberRepo.deleteById(id);
                simpMessagingTemplate.convertAndSend("/topic/deleteMember", member);
                return BerlizUtilities.buildResponse(HttpStatus.OK, "Member deleted successfully");
            } catch (DataIntegrityViolationException ex) {
                // Handle foreign key constraint violation when deleting
                ex.printStackTrace();
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Cannot delete member due to a foreign key constraint violation.");
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
            Optional<Member> optional = memberRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Member not found");
            }
            log.info("Inside optional {}", optional);
            status = optional.get().getStatus();
            Member member = optional.get();
            String responseMessage;
            if (status.equalsIgnoreCase("true")) {
                status = "false";
                responseMessage = "Member Status updated successfully. Now Deactivated";
            } else {
                status = "true";
                responseMessage = "Member Status updated successfully. Now Activated";
            }

            member.setStatus(status);
            memberRepo.save(member);
            simpMessagingTemplate.convertAndSend("/topic/updateMemberStatus", member);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (
                Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<Member> getMember(Integer id) {
        try {
            log.info("Inside getMember {}", id);
            Optional<Member> optional = memberRepo.findById(id);
            if (optional.isPresent()) {
                return new ResponseEntity<>(optional.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new Member(), HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Member(), HttpStatus.OK);
    }

    private void getMemberFromMap(Map<String, String> requestMap, User user) throws ParseException {
        Member member = new Member();
        member.setUser(user);

        double height = Double.parseDouble(requestMap.get("height"));
        member.setHeight(height);
        double weight = Double.parseDouble(requestMap.get("weight"));
        member.setWeight(weight);
        String dobString = user.getDob();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dob = dateFormat.parse(dobString);
        LocalDate dobLocalDate = dob.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate currentDate = LocalDate.now();
        Period period = Period.between(dobLocalDate, currentDate);
        Integer age = period.getYears();
        double BMI = weight / (height * height);
        double bodyFat = 1.2 * BMI + 0.23 * age - 5.4;
        member.setBodyFat(bodyFat);

        String categoryIdsString = requestMap.get("categoryIds");
        if (!categoryIdsString.isEmpty()) {
            String[] categoryIdsArray = categoryIdsString.split(",");
            Set<Category> categories = new HashSet<>();
            for (String categoryIdString : categoryIdsArray) {
                int categoryId = Integer.parseInt(categoryIdString.trim());
                Category category = categoryRepo.findById(categoryId)
                        .orElseThrow(() -> new EntityNotFoundException("Exercise not found with ID: " + categoryId));
                categories.add(category);
            }
            member.setCategories(categories);
        }

        List<Subscription> subscriptions = subscriptionRepo.findByUser(user);
        if (!subscriptions.isEmpty()) {
            Set<Subscription> subscriptionSet = new HashSet<>(subscriptions);
            member.setSubscriptions(subscriptionSet);
        }

        member.setMotivation(requestMap.get("motivation"));
        member.setTargetWeight(Double.parseDouble(requestMap.get("targetWeight")));
        member.setMedicalConditions(requestMap.get("medicalConditions"));
        member.setDate(new Date());
        member.setLastUpdate(new Date());
        member.setStatus("false");
        Member savedMember = memberRepo.save(member);
        simpMessagingTemplate.convertAndSend("/topic/getMemberFromMap", savedMember);
    }

    private boolean validateRequestFromMap(Map<String, String> requestMap, boolean validId) {
        if (validId) {
            return requestMap.containsKey("id")
                    && requestMap.containsKey("height")
                    && requestMap.containsKey("weight")
                    && requestMap.containsKey("medicalConditions")
                    && requestMap.containsKey("dietaryPreference")
                    && requestMap.containsKey("dietaryRestrictions")
                    && requestMap.containsKey("calorieIntake")
                    && requestMap.containsKey("categoryIds")
                    && requestMap.containsKey("mode")
                    && requestMap.containsKey("motivation")
                    && requestMap.containsKey("targetWeight");
        } else {
            return requestMap.containsKey("height")
                    && requestMap.containsKey("weight")
                    && requestMap.containsKey("medicalConditions")
                    && requestMap.containsKey("dietaryPreference")
                    && requestMap.containsKey("dietaryRestrictions")
                    && requestMap.containsKey("calorieIntake")
                    && requestMap.containsKey("categoryIds")
                    && requestMap.containsKey("mode")
                    && requestMap.containsKey("motivation")
                    && requestMap.containsKey("targetWeight");
        }
    }

}

package com.berliz.serviceImplement;

import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.Client;
import com.berliz.models.Subscription;
import com.berliz.models.TrainerReview;
import com.berliz.models.User;
import com.berliz.repositories.*;
import com.berliz.services.ClientService;
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
import java.time.Period;
import java.time.ZoneId;
import java.util.*;

@Service
@Slf4j
public class ClientServiceImplement implements ClientService {

    @Autowired
    ClientRepo clientRepo;

    @Autowired
    TrainerRepo trainerRepo;

    @Autowired
    TrainerReviewRepo trainerReviewRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    JWTFilter jwtFilter;

    @Autowired
    EmailUtilities emailUtilities;

    @Autowired
    PaymentRepo paymentRepo;

    @Autowired
    SubscriptionRepo subscriptionRepo;

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public ResponseEntity<String> addClient(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside addClient {}", requestMap);
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
                    return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "User id not found in db");
                }

                String userRole = user.getRole();
                boolean validateAdminRole = userRole.equalsIgnoreCase("admin");
                if (validateAdminRole) {
                    return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, "Admin cannot be a client");
                }

                Client client = clientRepo.findByUser(user);
                if (client != null) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Client exists already");
                }

                getClientFromMap(requestMap, user);
                return BerlizUtilities.buildResponse(HttpStatus.OK, "You have successfully added "
                        + user.getFirstname() + " as a client");
            } else {
                User user = userRepo.findByEmail(jwtFilter.getCurrentUser());
                if (user == null) {
                    return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, "Invalid user");
                }

                Client client = clientRepo.findByUser(user);
                if (client != null) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Client exists already");
                }
                getClientFromMap(requestMap, user);
                return BerlizUtilities.buildResponse(HttpStatus.OK, "Hello "
                        + user.getFirstname() + " your information has been saved successfully");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<Client>> getAllClients() {
        try {
            log.info("Inside getAllClients");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<Client> clients = clientRepo.findAll();
            return new ResponseEntity<>(clients, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Client>> getActiveClients() {
        try {
            log.info("Inside getActiveClients");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<Client> clients = clientRepo.getActiveClients();
            return new ResponseEntity<>(clients, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateClient(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside updateClient {}", requestMap);
            boolean isValid = validateRequestFromMap(requestMap, true);
            log.info("Is request valid? {}", isValid);
            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            Optional<Client> optional = clientRepo.findById(Integer.valueOf(requestMap.get("id")));
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Partner ID not found");
            }

            Client client = optional.get();
            String currentUser = jwtFilter.getCurrentUser();
            if (!(jwtFilter.isAdmin() || client.getUser().getEmail().equals(currentUser))) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (client.getStatus().equalsIgnoreCase("true")) {
                if (jwtFilter.isAdmin()) {
                    return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, "Cannot make an update. Partner is now active");
                } else {
                    return BerlizUtilities.buildResponse(HttpStatus.OK, "Sorry " +
                            client.getUser().getFirstname() + ", you cannot make an update. " +
                            " Your goal is in progress");
                }
            }

            User user = client.getUser();
            double heightInCM = Double.parseDouble(requestMap.get("height"));
            client.setHeight(heightInCM);
            double weight = Double.parseDouble(requestMap.get("weight"));
            client.setWeight(weight);
            String dobString = user.getDob();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date dob = dateFormat.parse(dobString);
            LocalDate dobLocalDate = dob.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate currentDate = LocalDate.now();
            Period period = Period.between(dobLocalDate, currentDate);
            int age = period.getYears();
            double heightInM = heightInCM / 100.0;
            double BMI = weight / (heightInM * heightInM);
            String gender = user.getGender().toLowerCase();
            double genderCoefficient;
            if ("male".equalsIgnoreCase(gender)) {
                genderCoefficient = 0.29288;
            } else if ("female".equalsIgnoreCase(gender)) {
                genderCoefficient = 0.29669;
            } else {
                genderCoefficient = 0.29225;
            }

            int caloriesIntake = Integer.parseInt(requestMap.get("caloriesIntake"));
            double bodyFat = (1.2 * BMI + 0.23 * age - 5.4 - genderCoefficient) * (caloriesIntake / 1000.0);
            client.setBodyFat(bodyFat);

            List<Subscription> subscriptions = subscriptionRepo.findByUser(user);
            if (!subscriptions.isEmpty()) {
                Set<Subscription> subscriptionSet = new HashSet<>(subscriptions);
                client.setSubscriptions(subscriptionSet);
            }

            client.setMotivation(requestMap.get("motivation"));
            client.setMode(requestMap.get("mode"));
            client.setTargetWeight(Double.parseDouble(requestMap.get("targetWeight")));
            client.setDietaryRestrictions(requestMap.get("dietaryRestrictions"));
            client.setCaloriesIntake(caloriesIntake);
            client.setDietaryPreferences(requestMap.get("dietaryPreferences"));
            client.setMedicalConditions(requestMap.get("medicalConditions"));
            client.setLastUpdate(new Date());
            Client savedClient = clientRepo.save(client);
            String responseMessage;
            if (jwtFilter.isAdmin()) {
                responseMessage = "Client updated successfully";
            } else {
                responseMessage = "Hello " +
                        client.getUser().getFirstname() + " you have successfully " +
                        " updated your client information";
            }

            simpMessagingTemplate.convertAndSend("/topic/updateClient", savedClient);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
            return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
        }
    }

    @Override
    public ResponseEntity<String> deleteClient(Integer id) throws JsonProcessingException {
        try {
            log.info("inside deleteClient {}", id);
            if (!jwtFilter.isAdmin()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            Optional<Client> optional = clientRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Client not found");
            }
            log.info("inside optional {}", optional);
            try {
                Client client = optional.get();
                clientRepo.deleteById(id);
                simpMessagingTemplate.convertAndSend("/topic/deleteClient", client);
                return BerlizUtilities.buildResponse(HttpStatus.OK, "Client deleted successfully");
            } catch (DataIntegrityViolationException ex) {
                // Handle foreign key constraint violation when deleting
                ex.printStackTrace();
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Cannot delete client due to a foreign key constraint violation.");
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
            if (!jwtFilter.isAdmin()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            Optional<Client> optional = clientRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Client not found");
            }

            log.info("Inside optional {}", optional);
            Client client = optional.get();
            String status = client.getStatus();
            User user = userRepo.findByUserId(client.getUser().getId());

            if (user == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "User not found for client");
            }

            boolean hasActiveSubscription = false;
            for (Subscription subscription : client.getSubscriptions()) {
                if ("true".equalsIgnoreCase(subscription.getStatus())) {
                    hasActiveSubscription = true;
                    break;
                }
            }

            if (!hasActiveSubscription) {
                log.info("No active subscriptions found for client {}", id);
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND,
                        "Cannot update client's status. No active subscriptions found");
            }


            String responseMessage;
            if (status.equalsIgnoreCase("true")) {
                status = "false";
                responseMessage = "Client Status updated successfully. Now Deactivated";
                emailUtilities.sendStatusMailToUser("false", "Client", user.getEmail());
            } else {
                status = "true";
                responseMessage = "Client Status updated successfully. Now Activated";
                emailUtilities.sendStatusMailToUser("true", "Client", user.getEmail());
            }

            client.setStatus(status);
            clientRepo.save(client);
            simpMessagingTemplate.convertAndSend("/topic/updateClientStatus", client);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
            return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
        }
    }

    @Override
    public ResponseEntity<Client> getClient(Integer id) {
        try {
            log.info("Inside getClient {}", id);
            Optional<Client> optional = clientRepo.findById(id);
            return optional.map(client ->
                    new ResponseEntity<>(client, HttpStatus.OK)).orElseGet(() ->
                    new ResponseEntity<>(new Client(), HttpStatus.NOT_FOUND));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Client(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<TrainerReview>> getMyTrainerReviews() {
        try {
            log.info("Inside getMyTrainerReviews");
            if (!(jwtFilter.isAdmin() || jwtFilter.isClient() || jwtFilter.isTrainer())) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            Client client = clientRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (client == null) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            List<TrainerReview> trainerTrainerReviews = trainerReviewRepo.findByClient(client);
            return new ResponseEntity<>(trainerTrainerReviews, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void getClientFromMap(Map<String, String> requestMap, User user) throws ParseException {
        Client client = new Client();
        client.setUser(user);

        double heightInCM = Double.parseDouble(requestMap.get("height"));
        client.setHeight(heightInCM);
        double weight = Double.parseDouble(requestMap.get("weight"));
        client.setWeight(weight);
        String dobString = user.getDob();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dob = dateFormat.parse(dobString);
        LocalDate dobLocalDate = dob.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate currentDate = LocalDate.now();
        Period period = Period.between(dobLocalDate, currentDate);
        int age = period.getYears();
        double heightInM = heightInCM / 100.0;
        double BMI = weight / (heightInM * heightInM);
        String gender = user.getGender().toLowerCase();
        double genderCoefficient;
        if ("male".equalsIgnoreCase(gender)) {
            genderCoefficient = 0.29288;
        } else if ("female".equalsIgnoreCase(gender)) {
            genderCoefficient = 0.29669;
        } else {
            genderCoefficient = 0.29225;
        }

        int caloriesIntake = Integer.parseInt(requestMap.get("caloriesIntake"));
        double bodyFat = (1.2 * BMI + 0.23 * age - 5.4 - genderCoefficient) * (caloriesIntake / 1000.0);
        client.setBodyFat(bodyFat);

        List<Subscription> subscriptions = subscriptionRepo.findByUser(user);
        if (!subscriptions.isEmpty()) {
            Set<Subscription> subscriptionSet = new HashSet<>(subscriptions);
            client.setSubscriptions(subscriptionSet);
        }

        client.setMotivation(requestMap.get("motivation"));
        client.setMode(requestMap.get("mode"));
        client.setTargetWeight(Double.parseDouble(requestMap.get("targetWeight")));
        client.setDietaryRestrictions(requestMap.get("dietaryRestrictions"));
        client.setCaloriesIntake(caloriesIntake);
        client.setDietaryPreferences(requestMap.get("dietaryPreferences"));
        client.setMedicalConditions(requestMap.get("medicalConditions"));
        client.setDate(new Date());
        client.setLastUpdate(new Date());
        client.setStatus("false");
        Client savedClient = clientRepo.save(client);
        simpMessagingTemplate.convertAndSend("/topic/getClientFromMap", savedClient);
    }

    private boolean validateRequestFromMap(Map<String, String> requestMap, boolean validId) {
        if (validId) {
            return requestMap.containsKey("id")
                    && requestMap.containsKey("height")
                    && requestMap.containsKey("weight")
                    && requestMap.containsKey("medicalConditions")
                    && requestMap.containsKey("dietaryPreferences")
                    && requestMap.containsKey("dietaryRestrictions")
                    && requestMap.containsKey("caloriesIntake")
                    && requestMap.containsKey("motivation")
                    && requestMap.containsKey("targetWeight");
        } else {
            return requestMap.containsKey("height")
                    && requestMap.containsKey("weight")
                    && requestMap.containsKey("medicalConditions")
                    && requestMap.containsKey("dietaryPreferences")
                    && requestMap.containsKey("dietaryRestrictions")
                    && requestMap.containsKey("caloriesIntake")
                    && requestMap.containsKey("motivation")
                    && requestMap.containsKey("targetWeight");
        }
    }
}

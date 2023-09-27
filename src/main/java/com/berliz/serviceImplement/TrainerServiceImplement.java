package com.berliz.serviceImplement;

import com.berliz.DTO.TrainerRequest;
import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.Category;
import com.berliz.models.Partner;
import com.berliz.models.Trainer;
import com.berliz.models.User;
import com.berliz.repository.CategoryRepo;
import com.berliz.repository.PartnerRepo;
import com.berliz.repository.TrainerRepo;
import com.berliz.repository.UserRepo;
import com.berliz.services.TrainerService;
import com.berliz.utils.BerlizUtilities;
import com.berliz.utils.EmailUtilities;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class TrainerServiceImplement implements TrainerService {

    @Autowired
    TrainerRepo trainerRepo;

    @Autowired
    CategoryRepo categoryRepo;

    @Autowired
    UserServiceImplement userServiceImplement;

    @Autowired
    JWTFilter jwtFilter;

    @Autowired
    PartnerRepo partnerRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    EmailUtilities emailUtilities;

    /**
     * adds a Trainer based on data provided
     *
     * @param trainerRequest The request body containing data
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<String> addTrainer(TrainerRequest trainerRequest) throws JsonProcessingException {
        try {
            log.info("Inside addTrainer {}", trainerRequest);
            boolean validRequest = trainerRequest != null;
            log.info("Is request valid? {}", validRequest);

            if (!validRequest) {
                return BerlizUtilities.getResponseEntity(BerlizConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
            if (jwtFilter.isAdmin()) {
                return handleTrainerAdditionByAdmin(trainerRequest);
            } else {
                return handleTrainerAdditionByUser(trainerRequest);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }


    /**
     * Updates a Trainer based on data provided
     *
     * @param requestMap The request body containing data
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<String> updateTrainer(Map<String, String> requestMap) {
        try {
            log.info("Inside updateTrainer {}", requestMap);

            // Validate the incoming request
            boolean isValid = validateTrainerFromMap(requestMap, true);
            log.info("Is request valid? {}", isValid);

            if (!isValid) {
                return BerlizUtilities.getResponseEntity(BerlizConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }

            Trainer validTrainer = trainerRepo.findByTrainerId(Integer.valueOf(requestMap.get("id")));

            // Check if the Trainer exists
            if (validTrainer == null) {
                return BerlizUtilities.getResponseEntity("Trainer id not found", HttpStatus.BAD_REQUEST);
            }

            // Check user permissions and update Trainer
            if (jwtFilter.isAdmin() || (jwtFilter.isTrainer()
                    && jwtFilter.getCurrentUserId().equals(validTrainer.getPartner().getUser().getId())
                    && validTrainer.getStatus().equalsIgnoreCase("true"))) {
                updateTrainerFromMap(requestMap);
                return BerlizUtilities.getResponseEntity("Trainer updated successfully", HttpStatus.OK);
            } else {
                return BerlizUtilities.getResponseEntity(BerlizConstants.UNAUTHORIZED_REQUEST, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    /**
     * Returns a list of Trainers
     *
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<List<Trainer>> getAllTrainers() {
        try {
            log.info("Inside getAllTrainers");

            // Check if the user is an admin
            if (jwtFilter.isAdmin()) {
                // Retrieve all Trainers from the repository
                List<Trainer> Trainers = trainerRepo.findAll();
                return new ResponseEntity<>(Trainers, HttpStatus.OK);
            } else {
                // Return an unauthorized response for non-admin users
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Return an error response if an exception occurred
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    /**
     * Updates a Trainer partner ID based on the existing id and new id provided
     *
     * @param id    The existing partner id to be replaced.
     * @param newId The new partner id.
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<String> updatePartnerId(Integer id, Integer newId) {
        try {
            log.info("Inside updateTrainerPartnerId {}", id);

            // Check if the user is an admin
            if (jwtFilter.isAdmin()) {
                // Retrieve the Trainer with the given id
                Optional<Trainer> optional = trainerRepo.findById(id);
                if (optional.isPresent()) {
                    log.info("Inside optional {}", optional);
                    Trainer Trainer = optional.get();

                    // Check if the new partner id exists
                    Partner newPartner = partnerRepo.findById(newId).orElse(null);
                    if (newPartner == null) {
                        return BerlizUtilities.getResponseEntity("Invalid new partner id", HttpStatus.BAD_REQUEST);
                    }

                    // Check if the new partner id exists in the driver
                    Trainer partnerTrainer = trainerRepo.findById(newId).orElse(null);
                    if (partnerTrainer != null) {
                        return BerlizUtilities.getResponseEntity("Partner id exists in driver", HttpStatus.BAD_REQUEST);
                    }

                    //Check if the new partner id is a valid user - i.e. it is active
                    String newPartnerStatus = newPartner.getUser().getStatus();
                    if (!newPartnerStatus.equalsIgnoreCase("true")) {
                        return BerlizUtilities.getResponseEntity("new partnerId must be approved by admin", HttpStatus.BAD_REQUEST);
                    }

                    //Check if the new partner id has a valid user role
                    String newPartnerRole = newPartner.getUser().getRole();
                    if (!newPartnerRole.equalsIgnoreCase("user")) {
                        return BerlizUtilities.getResponseEntity("new partnerId must have user role", HttpStatus.BAD_REQUEST);
                    }

                    // Check if the Trainer status is false before updating partner id
                    if (Trainer.getStatus().equalsIgnoreCase("false")) {
                        // Update the Trainer's partner id
                        trainerRepo.updatePartnerId(id, newId);
                        return BerlizUtilities.getResponseEntity("Trainer - partner id updated successfully. New id: " + newId, HttpStatus.OK);
                    } else {
                        return BerlizUtilities.getResponseEntity("Trainer status must be false to update partner id", HttpStatus.BAD_REQUEST);
                    }
                } else {
                    return BerlizUtilities.getResponseEntity("Trainer with id " + id + " not found", HttpStatus.BAD_REQUEST);
                }
            } else {
                return BerlizUtilities.getResponseEntity(BerlizConstants.UNAUTHORIZED_REQUEST, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes a Trainer based on the provided Trainer ID.
     *
     * @param id The ID of the Trainer to be deleted
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<String> deleteTrainer(Integer id) {
        try {
            // Check if the current user is an admin
            if (jwtFilter.isAdmin()) {
                // Retrieve the Trainer by its ID
                Trainer Trainer = trainerRepo.findByTrainerId(id);
                if (Trainer != null) {
                    // Delete the retrieved Trainer from the repository
                    trainerRepo.delete(Trainer);
                    return BerlizUtilities.getResponseEntity("Trainer deleted successfully", HttpStatus.OK);
                } else {
                    // Trainer with the provided ID was not found
                    return BerlizUtilities.getResponseEntity("Trainer id not found", HttpStatus.BAD_REQUEST);
                }
            } else {
                // Unauthorized access, user is not an admin
                return BerlizUtilities.getResponseEntity(BerlizConstants.UNAUTHORIZED_REQUEST, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // Internal server error occurred
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Updates a Trainer status based on the provided Trainer ID.
     *
     * @param id The ID of the Trainer status to be updated
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<String> updateStatus(Integer id) {
        try {
            log.info("Inside updateStatus {}", id);

            // Retrieve the current user's ID
            Integer userId = jwtFilter.getCurrentUserId();

            // Retrieve the Trainer entity by ID
            Optional<Trainer> optional = trainerRepo.findById(id);

            // Check if the Trainer exists
            if (optional.isPresent()) {
                log.info("Inside optional {}", optional);

                // Retrieve the ID and status of the user associated with the Trainer
                Integer validUser = optional.get().getPartner().getUser().getId();
                String validUserStatus = optional.get().getStatus();

                // Check if the user is an admin or the associated partner
                if (jwtFilter.isAdmin() || (validUser.equals(userId) && validUserStatus.equalsIgnoreCase("true"))) {
                    log.info("Is valid user? Admin: {}, ValidUser: {}, CurrentUser: {}", jwtFilter.isAdmin(), validUser, userId);

                    // Get the current status of the Trainer
                    String status = optional.get().getStatus();
                    String userEmail = optional.get().getPartner().getUser().getEmail();

                    // Toggle the status
                    status = status.equalsIgnoreCase("true") ? "false" : "true";

                    // Update the status in the repository
                    trainerRepo.updateStatus(id, status);

                    // Update user role in user repository
                    if (optional.get().getPartner().getUser().getRole().equalsIgnoreCase("user") && status.equalsIgnoreCase("true")) {
                        userRepo.updateUserRole("Trainer", validUser);
                    } else {
                        userRepo.updateUserRole("user", validUser);
                    }

                    // Send status update emails
                    emailUtilities.sendStatusMailToAdmins(status, userEmail, userRepo.getAllAdminsMail(), "Trainer");
                    emailUtilities.sendStatusMailToUser(status, "Trainer", userEmail);

                    // Return a success response
                    String responseMessage = status.equalsIgnoreCase("true") ?
                            "Trainer Status updated successfully. NOW ACTIVE" :
                            "Trainer Status updated successfully. NOW DISABLED";
                    return BerlizUtilities.getResponseEntity(responseMessage, HttpStatus.OK);
                } else {
                    // Return an unauthorized response
                    return BerlizUtilities.getResponseEntity(BerlizConstants.UNAUTHORIZED_REQUEST, HttpStatus.UNAUTHORIZED);
                }
            } else {
                // Return a response when Trainer ID is not found
                return BerlizUtilities.getResponseEntity("Trainer id not found", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Return an error response if an exception occurred
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    /**
     * Gets a Trainer based on the provided Trainer ID.
     *
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<Trainer> getTrainer() {
        try {
            log.info("Inside getTrainer");

            // Get the current user's ID
            Integer user = jwtFilter.getCurrentUserId();

            // Retrieve the partner associated with the current user
            Partner partnerByUserId = partnerRepo.findByUserId(user);

            // Retrieve the Trainer by its ID
            Trainer Trainer = trainerRepo.findByPartnerId(partnerByUserId.getId());

            if (Trainer == null) {
                // Trainer with the provided ID was not found
                return new ResponseEntity<>(new Trainer(), HttpStatus.BAD_REQUEST);
            }

            if (jwtFilter.isAdmin()) {
                // User is an admin, return the retrieved Trainer
                return new ResponseEntity<>(Trainer, HttpStatus.OK);
            } else if (partnerByUserId != null) {
                // Check if the logged-in user has a partner and the Trainer matches
                Integer currentUser = partnerByUserId.getUser().getId();

                if (currentUser.equals(user) && Trainer.getPartner().getId().equals(partnerByUserId.getId())) {
                    // Return the retrieved Trainer
                    return new ResponseEntity<>(Trainer, HttpStatus.OK);
                }
            }

            // Unauthorized access, user is not admin and doesn't have a valid Trainer
            return new ResponseEntity<>(new Trainer(), HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Internal server error occurred
        return new ResponseEntity<>(new Trainer(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Validates the data in the request map for Trainer creation or update.
     *
     * @param requestMap The map containing the request data
     * @param validId    Flag indicating whether a valid ID is required for update
     * @return True if the request data is valid, otherwise false
     */
    private boolean validateTrainerFromMap(Map<String, String> requestMap, boolean validId) {
        if (validId) {
            // For Trainer update with valid ID, check the presence of all required fields
            return requestMap.containsKey("id")
                    && requestMap.containsKey("name")
                    && requestMap.containsKey("motto")
                    && requestMap.containsKey("address")
                    && requestMap.containsKey("introduction")
                    && requestMap.containsKey("experience")
                    && requestMap.containsKey("location")
                    && requestMap.containsKey("photo")
                    && requestMap.containsKey("likes")
                    && requestMap.containsKey("categoryIds");
        } else {
            // For Trainer creation, check the presence of all required fields except ID
            return requestMap.containsKey("name")
                    && requestMap.containsKey("motto")
                    && requestMap.containsKey("address")
                    && requestMap.containsKey("introduction")
                    && requestMap.containsKey("experience")
                    && requestMap.containsKey("location")
                    && requestMap.containsKey("photo")
                    && requestMap.containsKey("categoryIds");
        }
    }

    /**
     * Constructs a Trainer object from the provided request map and saves it to the repository.
     *
     * @param trainerRequest The map containing the request data
     * @return The constructed and saved Trainer object
     */
    private Trainer getTrainerFromMap(TrainerRequest trainerRequest) throws IOException {
        Trainer trainer = new Trainer();
        User user = userRepo.findByEmail(jwtFilter.getCurrentUser());
        user.setRole("trainer");
        userRepo.save(user);
        Partner partner = partnerRepo.findByPartnerId(trainerRequest.getId());
        byte[] photo = trainerRequest.getPhoto().getBytes();

        // Parse tagIds as a comma-separated string
        String categoryIdsString = trainerRequest.getCategoryIds();
        String[] categoryIdsArray = categoryIdsString.split(",");

        Set<Category> categorySet = new HashSet<>();
        for (String categoryIdString : categoryIdsArray) {
            // Remove leading and trailing spaces before parsing
            int categoryId = Integer.parseInt(categoryIdString.trim());

            Category category = new Category();
            category.setId(categoryId);
            categorySet.add(category);
        }

        trainer.setCategorySet(categorySet);
        trainer.setPartner(partner);
        trainer.setName(trainerRequest.getName());
        trainer.setMotto(trainerRequest.getMotto());
        trainer.setAddress(trainerRequest.getAddress());
        trainer.setExperience(trainerRequest.getExperience());
        trainer.setPhoto(photo);
        trainer.setLikes(0);
        trainer.setDate(new Date());
        trainer.setLastUpdate(new Date());
        trainer.setStatus("false"); // Initializing status

        return trainer;
    }

    /**
     * Updates a Trainer object from the provided request map and saves it to the repository.
     *
     * @param requestMap The map containing the request data
     * @return The constructed and saved Trainer object
     */
    private ResponseEntity<String> updateTrainerFromMap(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            Integer currentUser = jwtFilter.getCurrentUserId();
            Optional<Trainer> optional = trainerRepo.findById(Integer.valueOf(requestMap.get("id")));

            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Trainer id not found");
            }

            Trainer existingTrainer = optional.get();
            boolean validUser = jwtFilter.isAdmin() || currentUser.equals(existingTrainer.getPartner().getUser().getId());

            if (!validUser) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            // Parse categoryIds as a comma-separated string
            String categoryIdsString = requestMap.get("categoryIds");
            String[] categoryIdsArray = categoryIdsString.split(",");

            Set<Category> categorySet = new HashSet<>();
            for (String categoryIdString : categoryIdsArray) {
                // Remove leading and trailing spaces before parsing
                int categoryId = Integer.parseInt(categoryIdString.trim());

                // Check if the category with the given ID exists in the database
                Optional<Category> optionalCategory = categoryRepo.findById(categoryId);
                if (optionalCategory.isEmpty()) {
                    return BerlizUtilities.getResponseEntity("Category with ID " + categoryId + " not found", HttpStatus.BAD_REQUEST);
                }
                categorySet.add(optionalCategory.get());
            }

            // Update the Trainer properties
            existingTrainer.setCategorySet(categorySet);
            existingTrainer.setName(requestMap.get("name"));
            existingTrainer.setMotto(requestMap.get("motto"));
            existingTrainer.setAddress(requestMap.get("address"));
            existingTrainer.setExperience(requestMap.get("experience"));
            existingTrainer.setLikes(Integer.parseInt(requestMap.get("likes")));
            existingTrainer.setLastUpdate(new Date());
            trainerRepo.save(existingTrainer);

            if (jwtFilter.isAdmin())
                return BerlizUtilities.buildResponse(HttpStatus.OK, "Trainer information updated successfully");
            else
                return BerlizUtilities.buildResponse(HttpStatus.OK, "Hello, " + existingTrainer.getName() + " you have successfully updated your trainer's account information");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Handle use case of adding Trainer by an admin.
     *
     * @param trainerRequest The map containing the request data
     * @return The constructed and saved Trainer object
     */
    private ResponseEntity<String> handleTrainerAdditionByAdmin(TrainerRequest trainerRequest) throws JsonProcessingException {
        try {
            log.info("Handling Trainer addition by admin");
            Integer partnerId = trainerRequest.getId();
            Trainer existingTrainer = trainerRepo.findByPartnerId(partnerId);
            Partner partner = partnerRepo.findByPartnerId(partnerId);

            if (partnerId == null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Admin must provide partnerId");
            }

            if (partner == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Partner id not found");
            }

            if (existingTrainer != null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Trainer already exists");
            }

            if (!isValidRole(partnerId, "trainer")) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Invalid partner role. Partner must be a trainer");
            }

            if (!isApprovedPartner(partnerId)) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, "Partner application hasn't been approved yet");
            }

            if (isTrainerNameAlreadyExists(trainerRequest.getName())) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Trainer name is already taken. Please choose another name");
            }

            trainerRepo.save(getTrainerFromMap(trainerRequest));
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Trainer added successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Handle use case of adding Trainer by a valid user.
     *
     * @param trainerRequest The map containing the request data
     * @return The constructed and saved Trainer object
     */
    private ResponseEntity<String> handleTrainerAdditionByUser(TrainerRequest trainerRequest) throws JsonProcessingException {
        try {
            log.info("Handling Trainer addition by user");
            Integer userId = jwtFilter.getCurrentUserId();
            Partner partner = partnerRepo.findByUserId(userId);
            Integer partnerId = partner.getId();

            if (partner == null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Partner id not found");
            }

            Trainer existingTrainer = trainerRepo.findByPartnerId(partnerId);
            if (existingTrainer != null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Trainer already exists");
            }

            if (!isValidRole(partnerId, "trainer")) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Invalid partner role. Partner must be a trainer");
            }

            if (!isApprovedPartner(partnerId)) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Your partnership application is under review, please wait for admin approval");
            }

            if (isTrainerNameAlreadyExists(trainerRequest.getName())) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Trainer name is already taken. Please choose another name");
            }

            trainerRepo.save(getTrainerFromMap(trainerRequest));
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Trainer added successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Validates partner and role .
     *
     * @param partnerId    ID of the partner to be validated
     * @param requiredRole role required of the partner
     * @return The valid partner
     */
    private boolean isValidRole(Integer partnerId, String requiredRole) {
        Partner partner = partnerRepo.findByPartnerId(partnerId);
        return partner != null && partner.getRole().equalsIgnoreCase(requiredRole);
    }

    /**
     * Validates if Trainer is an approved partner.
     *
     * @param partnerId ID of the partner to be approved
     * @return The valid partner
     */
    private boolean isApprovedPartner(Integer partnerId) {
        Partner partner = partnerRepo.findByPartnerId(partnerId);
        return partner != null && partner.getStatus().equalsIgnoreCase("true");
    }

    /**
     * Checks if a Trainer name already exists.
     *
     * @param TrainerName name of Trainer to be checked
     * @return The valid partner
     */
    private boolean isTrainerNameAlreadyExists(String TrainerName) {
        Trainer TrainerByName = trainerRepo.findByName(TrainerName);
        return TrainerByName != null;
    }

}

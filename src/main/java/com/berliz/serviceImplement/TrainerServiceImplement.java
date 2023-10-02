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
import com.berliz.utils.FileUtilities;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    JWTFilter jwtFilter;

    @Autowired
    PartnerRepo partnerRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    EmailUtilities emailUtilities;

    @Autowired
    FileUtilities fileUtilities;

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
    public ResponseEntity<String> updateTrainer(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside updateTrainer {}", requestMap);
            Trainer trainer = trainerRepo.findByTrainerId(Integer.valueOf(requestMap.get("id")));
            Integer userId = jwtFilter.getCurrentUserId();
            boolean validUser = jwtFilter.isAdmin() ||
                    (jwtFilter.isTrainer() && userId.equals(trainer.getPartner().getUser().getId()));
            boolean isValidMap = validateTrainerFromMap(requestMap);
            log.info("Is request valid? {}", isValidMap);

            if (!isValidMap) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            if (!validUser) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (trainer == null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Trainer id not found");
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
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Category with ID " + categoryId + " not found");
                }
                categorySet.add(optionalCategory.get());
            }

            // Update the Trainer properties
            trainer.setCategorySet(categorySet);
            trainer.setName(requestMap.get("name"));
            trainer.setMotto(requestMap.get("motto"));
            trainer.setAddress(requestMap.get("address"));
            trainer.setExperience(requestMap.get("experience"));
            trainer.setLikes(Integer.parseInt(requestMap.get("likes")));
            trainer.setLastUpdate(new Date());
            trainerRepo.save(trainer);

            if (jwtFilter.isAdmin())
                return BerlizUtilities.buildResponse(HttpStatus.OK, "Trainer information updated successfully");
            else
                return BerlizUtilities.buildResponse(HttpStatus.OK, "Hello, " + trainer.getName() + " you have successfully updated your trainer's account information");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
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
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
                List<Trainer> Trainers = trainerRepo.findAll();
                return new ResponseEntity<>(Trainers, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Deletes a Trainer based on the provided Trainer ID.
     *
     * @param id The ID of the Trainer to be deleted
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<String> deleteTrainer(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside deleteTrainer {}", id);
            User user = userRepo.findByEmail(jwtFilter.getCurrentUser());
            boolean authorizedUser = user.getEmail().equalsIgnoreCase("berlizworld@gmail.com");
            if (!jwtFilter.isAdmin() && authorizedUser) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            Trainer Trainer = trainerRepo.findByTrainerId(id);
            if (Trainer == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Trainer id not found");
            }
            trainerRepo.delete(Trainer);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Trainer deleted successfully");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Updates a Trainer status based on the provided Trainer ID.
     *
     * @param id The ID of the Trainer status to be updated
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside updateStatus {}", id);
            Integer validUserId = jwtFilter.getCurrentUserId();
            Optional<Trainer> optional = trainerRepo.findById(id);

            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Trainer id not found");
            }
            log.info("Inside optional {}", optional);

            Integer userId = optional.get().getPartner().getUser().getId();
            String status = optional.get().getStatus();
            String userEmail = optional.get().getPartner().getUser().getEmail();
            boolean validUser = jwtFilter.isAdmin() || (validUserId.equals(userId));

            if (!validUser) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            // Toggle the status
            status = status.equalsIgnoreCase("true") ? "false" : "true";
            trainerRepo.updateStatus(id, status);

            // Update user role in user repository
            if (status.equalsIgnoreCase("true")) {
                userRepo.updateUserRole("trainer", userId);
            } else {
                userRepo.updateUserRole("user", userId);
            }

            // Send status update emails
            emailUtilities.sendStatusMailToAdmins(status, userEmail, userRepo.getAllAdminsMail(), "Trainer");
            emailUtilities.sendStatusMailToUser(status, "Trainer", userEmail);

            String responseMessage = status.equalsIgnoreCase("true") ?
                    "Trainer Status updated successfully. NOW activated" :
                    "Trainer Status updated successfully. NOW deactivated";
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
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
            Integer userId = jwtFilter.getCurrentUserId();
            Partner partner = partnerRepo.findByUserId(userId);
            Trainer trainer = trainerRepo.findByPartnerId(partner.getId());

            if (partner == null) {
                return new ResponseEntity<>(new Trainer(), HttpStatus.BAD_REQUEST);
            }

            if (trainer == null) {
                return new ResponseEntity<>(new Trainer(), HttpStatus.BAD_REQUEST);
            }
            // Check if the logged-in user has a partner and the Trainer matches
            Integer currentUser = partner.getUser().getId();

            if (currentUser.equals(userId) && trainer.getPartner().getId().equals(partner.getId())) {
                return new ResponseEntity<>(trainer, HttpStatus.OK);
            }
            return new ResponseEntity<>(new Trainer(), HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Trainer(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Trainer>> getActiveTrainers() {
        try {
            log.info("Inside getActiveTrainers");
            List<Trainer> Trainers = trainerRepo.getActiveTrainers();
            return new ResponseEntity<>(Trainers, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @Override
    public ResponseEntity<String> updatePhoto(TrainerRequest trainerRequest) throws JsonProcessingException {
        try {
            log.info("Inside updatePhoto{}", trainerRequest);
            Integer trainerId = trainerRequest.getId();
            Trainer trainer = trainerRepo.findByTrainerId(trainerId);
            User user = trainer.getPartner().getUser();
            boolean validUser = jwtFilter.isAdmin() || jwtFilter.getCurrentUserId().equals(user.getId());

            if (!validUser) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (trainer == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Trainer id not found");
            }
            MultipartFile file = trainerRequest.getPhoto();
            if (file == null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "No profile photo provided");
            }

            if (!fileUtilities.isValidImageType(file)) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Invalid file type");
            }

            if (!fileUtilities.isValidImageSize(file)) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Invalid file type");
            }

            trainer.setPhoto(file.getBytes());
            trainerRepo.save(trainer);
            if (jwtFilter.isAdmin())
                return BerlizUtilities.buildResponse(HttpStatus.OK, trainer.getName() + "'s cover photo updated successfully");
            else
                return BerlizUtilities.buildResponse(HttpStatus.OK, "Your cover photo has been updated successfully");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);

    }

    /**
     * Validates the data in the request map for Trainer creation or update.
     *
     * @param requestMap The map containing the request data
     * @return True if the request data is valid, otherwise false
     */
    private boolean validateTrainerFromMap(Map<String, String> requestMap) {
            return requestMap.containsKey("id")
                    && requestMap.containsKey("name")
                    && requestMap.containsKey("motto")
                    && requestMap.containsKey("address")
                    && requestMap.containsKey("experience")
                    && requestMap.containsKey("likes")
                    && requestMap.containsKey("categoryIds");
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
            Optional<Trainer> optional = trainerRepo.findById(Integer.valueOf(requestMap.get("id")));
            Trainer trainer = optional.get();
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Trainer id not found");
            }


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
            Trainer trainer = trainerRepo.findByPartnerId(partnerId);
            Partner partner = partnerRepo.findByPartnerId(partnerId);

            if (partnerId == null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Admin must provide partnerId");
            }

            if (partner == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Partner id not found");
            }

            if (trainer != null) {
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

            Trainer trainer = trainerRepo.findByPartnerId(partnerId);
            if (trainer != null) {
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

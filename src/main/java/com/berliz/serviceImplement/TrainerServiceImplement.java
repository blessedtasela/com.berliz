package com.berliz.serviceImplement;

import com.berliz.DTO.TrainerRequest;
import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.*;
import com.berliz.repository.*;
import com.berliz.services.TrainerService;
import com.berliz.utils.BerlizUtilities;
import com.berliz.utils.EmailUtilities;
import com.berliz.utils.FileUtilities;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
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
    CenterRepo centerRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    TrainerLikeRepo trainerLikeRepo;

    @Autowired
    EmailUtilities emailUtilities;

    @Autowired
    FileUtilities fileUtilities;

    @Autowired
    TrainerPricingRepo trainerPricingRepo;

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

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

            simpMessagingTemplate.convertAndSend("/topic/updateTrainer", trainer);
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
            boolean authorizedUser = user.getEmail().equalsIgnoreCase(BerlizConstants.BERLIZ_SUPER_ADMIN);
            if (!(jwtFilter.isAdmin() && authorizedUser)) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            Trainer trainer = trainerRepo.findByTrainerId(id);
            if (trainer == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Trainer id not found");
            }

            if (trainer.getStatus().equalsIgnoreCase("true")) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Trainer is active, Cannot complete request");
            }
            trainerRepo.delete(trainer);
            simpMessagingTemplate.convertAndSend("/topic/deleteTrainer", trainer);
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
            Trainer trainer = optional.get();
            Integer userId = optional.get().getPartner().getUser().getId();
            String status = optional.get().getStatus();
            String userEmail = optional.get().getPartner().getUser().getEmail();
            boolean validUser = jwtFilter.isAdmin() || (validUserId.equals(userId));

            if (!validUser) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            status = status.equalsIgnoreCase("true") ? "false" : "true";
            trainer.setStatus(status);
            trainerRepo.save(trainer);
            emailUtilities.sendStatusMailToAdmins(status, userEmail, userRepo.getAllAdminsMail(), "Trainer");
            emailUtilities.sendStatusMailToUser(status, "Trainer", userEmail);
            String responseMessage;
            if (jwtFilter.isAdmin()) {
                responseMessage = status.equalsIgnoreCase("true") ?
                        "Trainer has been successfully activated" :
                        "Trainer has been deactivated successfully";
            } else {
                responseMessage = status.equalsIgnoreCase("true") ?
                        "Hello " + userEmail + ", your Trainer account has successfully been activated" :
                        "Hello " + userEmail + ", your Trainer account has been deactivated";
            }
            simpMessagingTemplate.convertAndSend("/topic/updateTrainerStatus", trainer);
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

            if (trainer == null || trainer.getId() == null) {
                log.error("Trainer or its ID is null. Check database.");
                return new ResponseEntity("Center is null", HttpStatus.NOT_FOUND);
            }

            // Check if the partner and the Trainer matches
            Integer currentUser = partner.getUser().getId();
            boolean validUser = currentUser.equals(userId)
                    && trainer.getPartner().getId().equals(partner.getId());
            if (validUser || jwtFilter.isAdmin()) {
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
    public ResponseEntity<String> likeTrainer(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside likeTrainer {}", id);
            Trainer trainer = trainerRepo.findByTrainerId(id);
            User user = userRepo.findByEmail(jwtFilter.getCurrentUser());
            boolean validUser = jwtFilter.isBerlizUser();

            if (!validUser) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (trainer == null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Trainer id not found");
            }

            boolean hasLiked = trainerLikeRepo.existsByUserAndTrainer(user, trainer);
            String responseMessage;
            if (hasLiked) {
                // dislike trainer
                trainerLikeRepo.deleteByUserAndTrainer(user, trainer);
                trainer.setLikes(trainer.getLikes() - 1);
                responseMessage = "Hello, " + user.getFirstname() + " you have disliked " + trainer.getName() + " profile";

            } else {
                // like trainer
                TrainerLike trainerLike = new TrainerLike();
                trainerLike.setUser(user);
                trainerLike.setTrainer(trainer);
                trainerLike.setDate(new Date());
                trainerLikeRepo.save(trainerLike);
                trainer.setLikes(trainer.getLikes() + 1);
                responseMessage = "Hello, " + user.getFirstname() + " you just liked " + trainer.getName() + " profile";
            }

            trainerRepo.save(trainer);
            simpMessagingTemplate.convertAndSend("/topic/likeTrainer", trainer);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Retrieves a list of all trainer likes.
     *
     * @return ResponseEntity containing the list of trainer likes.
     */
    @Override
    public ResponseEntity<List<TrainerLike>> getTrainerLikes() {
        try {
            log.info("Inside getTrainerLikes {}");
            List<TrainerLike> trainerLikes = trainerLikeRepo.findAll();
            return new ResponseEntity<>(trainerLikes, HttpStatus.OK);
        } catch (Exception ex) {
            log.error("Something went wrong while performing operation", ex);
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Updates the photo of a trainer.
     *
     * @param trainerRequest The trainer information including the new photo.
     * @return ResponseEntity indicating the result of the photo update operation.
     * @throws JsonProcessingException if there is an issue processing JSON data.
     */
    @Override
    public ResponseEntity<String> updatePhoto(TrainerRequest trainerRequest) throws JsonProcessingException {
        try {
            log.info("Inside updatePhoto{}", trainerRequest);
            Integer trainerId = trainerRequest.getId();
            Trainer trainer = trainerRepo.findByTrainerId(trainerId);

            if (trainer == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Trainer id not found");
            }

            User user = trainer.getPartner().getUser();
            boolean validUser = jwtFilter.isAdmin() || jwtFilter.getCurrentUserId().equals(user.getId());

            if (!validUser) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, BerlizConstants.UNAUTHORIZED_REQUEST);
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
            String responseMessage;
            if (jwtFilter.isAdmin()) {
                responseMessage = trainer.getName() + "'s cover photo updated successfully";
            } else {
                responseMessage = "Your cover photo has been updated successfully";
            }
            simpMessagingTemplate.convertAndSend("/topic/updatePhoto", trainer);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> addTrainerPricing(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside addTrainerPricing {}", requestMap);
            boolean isValid = validateTrainerPricingRequestFromMap(requestMap, false);
            log.info("Is request valid? {}", isValid);

            if (!jwtFilter.isAdmin() || jwtFilter.isTrainer()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            if (jwtFilter.isAdmin()) {
                if (requestMap.get("trainerId").isEmpty()) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Admin must provide trainerId");
                }

                Trainer trainer = trainerRepo.findByTrainerId(Integer.valueOf(requestMap.get("trainerId")));
                if (trainer == null) {
                    return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Trainer not found in db");
                }

                TrainerPricing trainerPricing = trainerPricingRepo.findByTrainer(trainer);
                if (trainerPricing != null) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "TrainerPricing already exits for " +
                            trainer.getName());
                }

                getTrainerPricingFromMap(requestMap, trainer);
                return BerlizUtilities.buildResponse(HttpStatus.OK, "You have successfully added pricing for "
                        + trainer.getName());
            } else {
                User user = userRepo.findByEmail(jwtFilter.getCurrentUser());
                Trainer trainer = trainerRepo.findByUserId(user.getId());
                if (trainer == null) {
                    return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "You are not authorized to make this request, " +
                            "Please contact admin to check your trainer status");
                }

                TrainerPricing trainerPricing = trainerPricingRepo.findByTrainer(trainer);
                if (trainerPricing != null) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Hello " + trainer.getName() +
                            ", you already have an active pricing");
                }

                getTrainerPricingFromMap(requestMap, trainer);
                return BerlizUtilities.buildResponse(HttpStatus.OK, "Congratulations" + trainer.getName() +
                        "!, your pricing has been added successfully");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);

    }

    @Override
    public ResponseEntity<String> updateTrainerPricing(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside updateTrainerPricing {}", requestMap);
            boolean isValid = validateTrainerPricingRequestFromMap(requestMap, true);
            log.info("Is request valid? {}", isValid);

            if (!jwtFilter.isAdmin() || jwtFilter.isTrainer()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            Optional<TrainerPricing> optional = trainerPricingRepo.findById(Integer.valueOf(requestMap.get("id")));
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "TrainerPricing ID not found");
            }

            TrainerPricing trainerPricing = optional.get();
            String currentUser = jwtFilter.getCurrentUser();
            if (!(jwtFilter.isAdmin()
                    || trainerPricing.getTrainer().getPartner().getUser().getEmail().equals(currentUser))) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            BigDecimal priceOnline = new BigDecimal(requestMap.get("priceOnline"));
            trainerPricing.setPriceOnline(priceOnline);
            BigDecimal priceHybrid = new BigDecimal(requestMap.get("priceHybrid"));
            trainerPricing.setPriceHybrid(priceHybrid);
            BigDecimal pricePersonal = new BigDecimal(requestMap.get("pricePersonal"));
            trainerPricing.setPricePersonal(pricePersonal);
            BigDecimal discount2Programs = new BigDecimal(requestMap.get("discount2Programs"));
            trainerPricing.setDiscount2Programs(discount2Programs);
            BigDecimal discount3Months = new BigDecimal(requestMap.get("discount3Months"));
            trainerPricing.setDiscount3Months(discount3Months);
            BigDecimal discount6Months = new BigDecimal(requestMap.get("discount6Months"));
            trainerPricing.setDiscount6Months(discount6Months);
            BigDecimal discount9Months = new BigDecimal(requestMap.get("discount9Months"));
            trainerPricing.setDiscount9Months(discount9Months);
            BigDecimal discount12Months = new BigDecimal(requestMap.get("discount12Months"));
            trainerPricing.setDiscount12Months(discount12Months);
            trainerPricing.setLastUpdate(new Date());
            TrainerPricing savedTrainerPricing = trainerPricingRepo.save(trainerPricing);
            String responseMessage;
            if (jwtFilter.isAdmin()) {
                responseMessage = trainerPricing.getTrainer().getName()+ "'s TrainerPricing updated successfully";
            } else {
                responseMessage = "Hello " +
                        trainerPricing.getTrainer().getName() + " you have successfully " +
                        " updated your pricing information";
            }

            simpMessagingTemplate.convertAndSend("/topic/updateTrainerPricing", savedTrainerPricing);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<TrainerPricing>> getTrainerPricing() {
        try {
            log.info("Inside getTrainerPricing");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<TrainerPricing> trainerPricing = trainerPricingRepo.findAll();
            return new ResponseEntity<>(trainerPricing, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteTrainerPricing(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside deleteTrainerPricing {}", id);
            User user = userRepo.findByEmail(jwtFilter.getCurrentUser());
            boolean authorizedUser = user.getEmail().equalsIgnoreCase(BerlizConstants.BERLIZ_SUPER_ADMIN);
            if (!(jwtFilter.isAdmin() && authorizedUser)) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            Optional<TrainerPricing> optional = trainerPricingRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "TrainerPricing id not found");
            }

            TrainerPricing trainerPricing = optional.get();
            trainerPricingRepo.delete(trainerPricing);
            simpMessagingTemplate.convertAndSend("/topic/deleteTrainerPricing", trainerPricing);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "TrainerPricing deleted successfully");

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

    private boolean validateTrainerPricingRequestFromMap(Map<String, String> requestMap, boolean isValid) {
        if (isValid) {
            return requestMap.containsKey("id")
                    && requestMap.containsKey("priceOnline")
                    && requestMap.containsKey("priceHybrid")
                    && requestMap.containsKey("pricePersonal")
                    && requestMap.containsKey("discount3Months")
                    && requestMap.containsKey("discount6Months")
                    && requestMap.containsKey("discount9Months")
                    && requestMap.containsKey("discount12Months")
                    && requestMap.containsKey("discount2Programs");
        }
        return requestMap.containsKey("priceOnline")
                && requestMap.containsKey("priceHybrid")
                && requestMap.containsKey("pricePersonal")
                && requestMap.containsKey("discount3Months")
                && requestMap.containsKey("discount6Months")
                && requestMap.containsKey("discount9Months")
                && requestMap.containsKey("discount12Months")
                && requestMap.containsKey("discount2Programs");
    }

    /**
     * Constructs a Trainer object from the provided request map and saves it to the repository.
     *
     * @param trainerRequest The map containing the request data
     * @return The constructed and saved Trainer object
     */
    private Trainer getTrainerFromMap(TrainerRequest trainerRequest) throws IOException {
        Trainer trainer = new Trainer();
        User user;
        Partner partner = partnerRepo.findByPartnerId(trainerRequest.getPartnerId());
        if (jwtFilter.isAdmin()) {
            String userEmail = partner.getUser().getEmail();
            user = userRepo.findByEmail(userEmail);
        } else {
            user = userRepo.findByEmail(jwtFilter.getCurrentUser());
        }
        user.setRole("trainer");
        userRepo.save(user);
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
        trainer.setStatus("false");
        Trainer savedTrainer = trainerRepo.save(trainer);
        simpMessagingTemplate.convertAndSend("/topic/getTrainerFromMap", savedTrainer);
        return trainer;
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
            Integer partnerId = trainerRequest.getPartnerId();
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

            getTrainerFromMap(trainerRequest);
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

            getTrainerFromMap(trainerRequest);
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

    private Trainer getTrainerPricingFromMap(Map<String, String> requestMap, Trainer trainer) throws IOException {
        TrainerPricing trainerPricing = new TrainerPricing();
        trainerPricing.setTrainer(trainer);
        BigDecimal priceOnline = new BigDecimal(requestMap.get("priceOnline"));
        trainerPricing.setPriceOnline(priceOnline);
        BigDecimal priceHybrid = new BigDecimal(requestMap.get("priceHybrid"));
        trainerPricing.setPriceHybrid(priceHybrid);
        BigDecimal pricePersonal = new BigDecimal(requestMap.get("pricePersonal"));
        trainerPricing.setPricePersonal(pricePersonal);
        BigDecimal discount2Programs = new BigDecimal(requestMap.get("discount2Programs"));
        trainerPricing.setDiscount2Programs(discount2Programs);
        BigDecimal discount3Months = new BigDecimal(requestMap.get("discount3Months"));
        trainerPricing.setDiscount3Months(discount3Months);
        BigDecimal discount6Months = new BigDecimal(requestMap.get("discount6Months"));
        trainerPricing.setDiscount6Months(discount6Months);
        BigDecimal discount9Months = new BigDecimal(requestMap.get("discount9Months"));
        trainerPricing.setDiscount9Months(discount9Months);
        BigDecimal discount12Months = new BigDecimal(requestMap.get("discount12Months"));
        trainerPricing.setDiscount12Months(discount12Months);
        trainerPricing.setDate(new Date());
        trainerPricing.setLastUpdate(new Date());

        TrainerPricing savedTrainerPricing = trainerPricingRepo.save(trainerPricing);
        simpMessagingTemplate.convertAndSend("/topic/getTrainerPricingFromMap", savedTrainerPricing);
        return trainer;
    }

}

package com.berliz.serviceImplement;

import com.berliz.DTO.*;
import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.*;
import com.berliz.models.TrainerReview;
import com.berliz.repositories.*;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    TrainerVideoAlbumRepo trainerVideoAlbumRepo;

    @Autowired
    TrainerPhotoAlbumRepo trainerPhotoAlbumRepo;

    @Autowired
    TrainerIntroductionRepo trainerIntroductionRepo;

    @Autowired
    TrainerBenefitRepo trainerBenefitRepo;

    @Autowired
    TrainerFeatureVideoRepo trainerFeatureVideoRepo;

    @Autowired
    ClientRepo clientRepo;

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
    TrainerReviewRepo trainerReviewRepo;

    @Autowired
    CenterTrainerRepo centerTrainerRepo;

    @Autowired
    TrainerReviewLikeRepo trainerReviewLikeRepo;

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
            boolean isValidMap = validateTrainerFromMap(requestMap);
            log.info("Is request valid? {}", isValidMap);
            Integer userId = jwtFilter.getCurrentUserId();
            if (!isValidMap) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            Trainer trainer = trainerRepo.findByTrainerId(Integer.valueOf(requestMap.get("id")));
            if (trainer == null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Trainer id not found");
            }

            boolean validUser = jwtFilter.isAdmin() ||
                    (jwtFilter.isTrainer() && userId.equals(trainer.getPartner().getUser().getId()));
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
            Trainer savedTrainer = trainerRepo.save(trainer);
            String adminNotificationMessage = "Trainer with id: " + savedTrainer.getId()
                    + ", and info: " + savedTrainer.getName() + "/ account information has been updated";
            String notificationMessage = "You have update your trainer account: " +
                    savedTrainer.getName();
            jwtFilter.sendNotifications("/topic/updateTrainer", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, savedTrainer);

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
            User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
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
            String adminNotificationMessage = "Trainer with id: " + trainer.getId() + ", and name " + trainer.getName() +
                    ", account has been deleted";
            String notificationMessage = "You have successfully deleted your trainer your account : " + trainer.getName();
            jwtFilter.sendNotifications("/topic/deleteTrainer", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, trainer);
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

            Trainer existingTrainer = optional.get();
            Integer userId = existingTrainer.getPartner().getUser().getId();
            String existingStatus = existingTrainer.getStatus();
            String userEmail = existingTrainer.getPartner().getUser().getEmail();
            boolean validUser = jwtFilter.isAdmin() || (validUserId.equals(userId));

            if (!validUser) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            String newStatus = existingStatus.equalsIgnoreCase("true") ? "false" : "true";
            if (newStatus.equalsIgnoreCase("true")) {
                String resultMessage = canUpdateTrainerStatus(existingTrainer, newStatus);

                // Check the resultMessage and return the corresponding HTTP response
                if (resultMessage.startsWith("Cannot")) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, resultMessage);
                }
            }

            existingTrainer.setStatus(newStatus);
            Trainer savedTrainer = trainerRepo.save(existingTrainer);
            emailUtilities.sendStatusMailToAdmins(newStatus, userEmail, userRepo.getAllAdminsMail(), "Trainer");
            emailUtilities.sendStatusMailToUser(newStatus, "Trainer", userEmail);
            String responseMessage = jwtFilter.isAdmin() ?
                    (newStatus.equalsIgnoreCase("true") ? "Trainer has been successfully activated" : "Trainer has been deactivated successfully") :
                    (newStatus.equalsIgnoreCase("true") ? "Hello " + userEmail + ", your Trainer account has successfully been activated" :
                            "Hello " + userEmail + ", your Trainer account has been deactivated");
            String adminNotificationMessage = "Trainer with id: " + savedTrainer.getId() +
                    ", status has been set to " + savedTrainer.getStatus();
            String notificationMessage = "You have successfully set your trainer status to : " +
                    savedTrainer.getStatus();
            jwtFilter.sendNotifications("/topic/updateTrainerStatus", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, savedTrainer);
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
            if (partner == null) {
                return new ResponseEntity<>(new Trainer(), HttpStatus.BAD_REQUEST);
            }

            Trainer trainer = trainerRepo.findByPartnerId(partner.getId());
            if (trainer == null || trainer.getId() == null) {
                log.error("Trainer or its ID is null. Check database.");
                return new ResponseEntity<>(new Trainer(), HttpStatus.NOT_FOUND);
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
            List<Trainer> trainers = trainerRepo.getActiveTrainers();
            return new ResponseEntity<>(trainers, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Likes a trainer with the specified identifier.
     *
     * @param id The identifier of the trainer to be liked.
     * @return A ResponseEntity<String> indicating the success or failure of the liking operation.
     * @throws JsonProcessingException If there is an issue processing JSON data during the operation.
     */
    @Override
    public ResponseEntity<String> likeTrainer(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside likeTrainer {}", id);
            Trainer trainer = trainerRepo.findByTrainerId(id);
            User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
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
                String adminNotificationMessage = "Trainer with id: " + trainer.getId() +
                        ", and name: " + trainer.getName() + " has just been disliked by: " + user.getEmail();
                String notificationMessage = "You have successfully disliked trainer : " + trainer.getName();
                jwtFilter.sendNotifications("/topic/likeTrainer", adminNotificationMessage,
                        jwtFilter.getCurrentUser(), notificationMessage, trainer);
            } else {
                // like trainer
                TrainerLike trainerLike = new TrainerLike();
                trainerLike.setUser(user);
                trainerLike.setTrainer(trainer);
                trainerLike.setDate(new Date());
                trainerLikeRepo.save(trainerLike);
                trainer.setLikes(trainer.getLikes() + 1);
                responseMessage = "Hello, " + user.getFirstname() + " you just liked " + trainer.getName() + " profile";
                String adminNotificationMessage = "Trainer with id: " + trainer.getId() +
                        ", and name: " + trainer.getName() + " has just been liked by: " + user.getEmail();
                String notificationMessage = "You have successfully liked trainer : " + trainer.getName();
                jwtFilter.sendNotifications("/topic/likeTrainer", adminNotificationMessage,
                        jwtFilter.getCurrentUser(), notificationMessage, trainer);
            }

            trainerRepo.save(trainer);
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
            log.info("Inside getTrainerLikes");
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

            String adminNotificationMessage = "Trainer photo with id: " + trainer.getId()
                    + ", and info: " + trainer.getName() + "/ has been updated";
            String notificationMessage = "You have update your trainer photo: " + trainer.getName();
            jwtFilter.sendNotifications("/topic/updatePhoto", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, trainer);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Adds pricing information for a trainer based on the provided request map.
     *
     * @param requestMap A Map<String, String> containing the pricing details for the trainer.
     *                   Expected keys: "trainerId", "price", "currency".
     * @return A ResponseEntity<String> indicating the success or failure of the pricing addition.
     * @throws JsonProcessingException If there is an issue processing JSON data during the operation.
     */
    @Override
    public ResponseEntity<String> addTrainerPricing(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside addTrainerPricing {}", requestMap);
            boolean isValid = validateTrainerPricingRequestFromMap(requestMap, false);
            log.info("Is request valid? {}", isValid);

            if (!(jwtFilter.isAdmin() || jwtFilter.isTrainer())) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            Trainer trainer = trainerRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (jwtFilter.isAdmin()) {
                if (requestMap.get("trainerId").isEmpty()) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Admin must provide trainerId");
                }

                trainer = trainerRepo.findByTrainerId(Integer.valueOf(requestMap.get("trainerId")));
            }

            if (trainer == null) {
                String trainerNotFoundResponse = jwtFilter.isAdmin() ? "Trainer not found in db" : "You are not authorized to make this request, " +
                        "Please contact admin to check your trainer status";
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, trainerNotFoundResponse);
            }

            TrainerPricing trainerPricing = trainerPricingRepo.findByTrainer(trainer);
            if (trainerPricing != null) {
                String trainerPricingExitsResponse = jwtFilter.isAdmin() ? "TrainerPricing already exits for " +
                        trainer.getName() : "Hello " + trainer.getName() +
                        ", you already have an active pricing";
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, trainerPricingExitsResponse);
            }


            getTrainerPricingFromMap(requestMap, trainer);
            String responseMessage = jwtFilter.isAdmin() ?
                    trainer.getName() + ", pricing have successfully been added to their profile " :
                    "Congratulations! " + trainer.getName() +
                            ", your pricing has been added to your trainer profile successfully";

            // check if all trainer entities are added and set the trainer status to true
            String resultMessage = canUpdateTrainerStatus(trainer, "true");
            if (resultMessage.startsWith("Conditions")) {
                trainer.setStatus("true");
                trainerRepo.save(trainer);
                responseMessage = jwtFilter.isAdmin() ?
                        trainer.getName() + ", pricing have successfully been added to their profile " +
                                "and their trainer account has been activated successfully" :
                        "Congratulations! " + trainer.getName() +
                                ", your pricing has been added successfully " +
                                "and your trainer account has been successfully activated";
            }

            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Updates pricing information for a trainer based on the provided request map.
     *
     * @param requestMap A Map<String, String> containing the updated pricing details for the trainer.
     *                   Expected keys: "trainerId", "price", "currency".
     * @return A ResponseEntity<String> indicating the success or failure of the pricing update.
     * @throws JsonProcessingException If there is an issue processing JSON data during the operation.
     */
    @Override
    public ResponseEntity<String> updateTrainerPricing(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside updateTrainerPricing {}", requestMap);
            boolean isValid = validateTrainerPricingRequestFromMap(requestMap, true);
            log.info("Is request valid? {}", isValid);

            if (!(jwtFilter.isAdmin() || jwtFilter.isTrainer())) {
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
            String currentUser = jwtFilter.getCurrentUserEmail();
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
                responseMessage = trainerPricing.getTrainer().getName() + "'s TrainerPricing updated successfully";
            } else {
                responseMessage = "Hello " +
                        trainerPricing.getTrainer().getName() + " you have successfully " +
                        " updated your pricing information";
            }

            String adminNotificationMessage = "Trainer pricing with id: " + savedTrainerPricing.getId()
                    + ", and info: " + savedTrainerPricing.getPriceOnline() + "/ has been updated";
            String notificationMessage = "You have update your trainer pricing: " +
                    savedTrainerPricing.getPriceOnline();
            jwtFilter.sendNotifications("/topic/updateTrainerPricing", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, savedTrainerPricing);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Retrieves the pricing information for trainers.
     *
     * @return A ResponseEntity<List<TrainerPricing>> containing the list of TrainerPricing objects,
     * indicating the success or failure of the retrieval operation.
     */
    @Override
    public ResponseEntity<List<TrainerPricing>> getAllTrainerPricing() {
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

    /**
     * Retrieves the pricing information  for a specific trainer.
     *
     * @return A ResponseEntity<<TrainerPricing> containing the  TrainerPricing object,
     * indicating the success or failure of the retrieval operation.
     */
    @Override
    public ResponseEntity<TrainerPricing> getMyTrainerPricing() {
        try {
            log.info("Inside getMyCenterPricing");
            if (!(jwtFilter.isAdmin() || jwtFilter.isTrainer())) {
                return new ResponseEntity<>(new TrainerPricing(), HttpStatus.UNAUTHORIZED);
            }

            Trainer trainer = trainerRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (trainer == null) {
                return new ResponseEntity<>(new TrainerPricing(), HttpStatus.BAD_REQUEST);
            }

            TrainerPricing trainerPricing = trainerPricingRepo.findByTrainer(trainer);
            if (trainerPricing == null) {
                return new ResponseEntity<>(new TrainerPricing(), HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>(trainerPricing, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new TrainerPricing(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Deletes the pricing information for a trainer with the specified identifier.
     *
     * @param id The identifier of the trainer whose pricing information is to be deleted.
     * @return A ResponseEntity<String> indicating the success or failure of the deletion operation.
     * @throws JsonProcessingException If there is an issue processing JSON data during the operation.
     */
    @Override
    public ResponseEntity<String> deleteTrainerPricing(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside deleteTrainerPricing {}", id);
            User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
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
            String adminNotificationMessage = "Trainer pricing with id: " + trainerPricing.getId()
                    + ", and info " + trainerPricing.getPriceOnline() +
                    ", account has been deleted";
            String notificationMessage = "You have successfully deleted your trainer pricing: "
                    + trainerPricing.getPriceOnline();
            jwtFilter.sendNotifications("/topic/deleteTrainerPricing", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, trainerPricing);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "TrainerPricing deleted successfully");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> addTrainerPhotoAlbum(PhotoAlbumRequest photoAlbumRequest) throws JsonProcessingException {
        try {
            log.info("Inside addTrainerPhotoAlbum {}", photoAlbumRequest);

            boolean isValid = photoAlbumRequest != null;
            log.info("Is request valid? {}", isValid);

            if (!jwtFilter.isAdmin() || jwtFilter.isTrainer()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            Trainer trainer = trainerRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (jwtFilter.isAdmin()) {
                if (photoAlbumRequest.getTrainerId() == null) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Admin must provide trainerId");
                }

                trainer = trainerRepo.findByTrainerId(photoAlbumRequest.getTrainerId());
            }

            if (trainer == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Trainer not found in db");
            }

            getTrainerPhotoAlbumFromMap(photoAlbumRequest, trainer);
            String responseMessage = jwtFilter.isAdmin() ?
                    trainer.getName() + "!, photo have successfully been added to their album" :
                    "Hello " + trainer.getName() + "!, you have successfully added a photo to your album";

            // check if all trainer entities are added and set the trainer status to true
            String resultMessage = canUpdateTrainerStatus(trainer, "true");
            if (resultMessage.startsWith("Conditions")) {
                trainer.setStatus("true");
                trainerRepo.save(trainer);
                responseMessage = jwtFilter.isAdmin() ?
                        trainer.getName() + "!, photo have successfully been added to their album " +
                                "and their trainer account has been activated successfully" :
                        "Hello " + trainer.getName() + "!, you have successfully added a photo to your album" +
                                "and your trainer account has successfully been activated";
            }

            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateTrainerPhotoAlbum(PhotoAlbumRequest photoAlbumRequest) throws JsonProcessingException {
        try {
            log.info("Inside updateTrainerPhotoAlbum {}", photoAlbumRequest);
            Integer userId = jwtFilter.getCurrentUserId();
            boolean isValid = photoAlbumRequest != null;
            log.info("Is request valid? {}", isValid);

            if (!jwtFilter.isAdmin() || !jwtFilter.isTrainer()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid || photoAlbumRequest.getId() == null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            TrainerPhotoAlbum existingPhotoAlbum = trainerPhotoAlbumRepo.findById(photoAlbumRequest.getId()).orElse(null);
            if (existingPhotoAlbum == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Photo album not found in db");
            }

            boolean validUser = jwtFilter.isAdmin() ||
                    (jwtFilter.isTrainer() && userId.equals(existingPhotoAlbum
                            .getTrainer().getPartner().getUser().getId()));
            if (!validUser) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!jwtFilter.isAdmin() && !existingPhotoAlbum.getTrainer().getId().equals(jwtFilter.getCurrentUserId())) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            Trainer trainer = existingPhotoAlbum.getTrainer();
            String photoFolderPath = BerlizConstants.TRAINER_PHOTO_ALBUM_LOCATION;
            String photoFileName = existingPhotoAlbum.getPhoto();
            String photoFilePath = photoFolderPath + photoFileName;
            Path path = Paths.get(photoFilePath);

            // Update photo content
            Files.write(path, photoAlbumRequest.getPhoto().getBytes());

            // Update photo details in the database
            existingPhotoAlbum.setComment(photoAlbumRequest.getComment());
            existingPhotoAlbum.setLastUpdate(new Date());
            trainerPhotoAlbumRepo.save(existingPhotoAlbum);
            String responseMessage = jwtFilter.isAdmin() ?
                    trainer.getName() + "!, photo have successfully been updated in their album" :
                    "Hello " + trainer.getName() + "!, you have successfully updated the photo in your album";
            String adminNotificationMessage = "Trainer photo with id: " + existingPhotoAlbum.getId()
                    + ", and info: " + existingPhotoAlbum.getUuid() + "/ has been updated";
            String notificationMessage = "You have update your trainer video: " +
                    existingPhotoAlbum.getUuid();
            jwtFilter.sendNotifications("/topic/updateTrainerPhotoAlbum", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, existingPhotoAlbum);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteTrainerPhotoAlbum(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside deleteTrainerPhotoAlbum {}", id);
            Optional<TrainerPhotoAlbum> optional = trainerPhotoAlbumRepo.findById(id);
            TrainerPhotoAlbum trainerPhotoAlbum = optional.orElse(null);

            if (trainerPhotoAlbum == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Trainer photo id not found");
            }

            if (!isAuthorizedToDeleteTrainerPhotoAlbum(trainerPhotoAlbum)) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!trainerPhotoAlbum.getTrainer().getStatus().equalsIgnoreCase("false")) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Trainer is inactive, Cannot complete request");
            }

            trainerPhotoAlbumRepo.delete(trainerPhotoAlbum);
            String adminNotificationMessage = "Trainer photo with id: " + trainerPhotoAlbum.getId()
                    + ", and info " + trainerPhotoAlbum.getUuid() + ", has been deleted";
            String notificationMessage = "You have successfully deleted your trainer video: "
                    + trainerPhotoAlbum.getUuid();
            jwtFilter.sendNotifications("/topic/deleteTrainerPhotoAlbum", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, trainerPhotoAlbum);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Trainer photo deleted from album successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<TrainerPhotoAlbum>> getAllTrainerPhotoAlbums() {
        try {
            log.info("Inside getAllTrainerPhotoAlbums");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<TrainerPhotoAlbum> trainerPhotoAlbum = trainerPhotoAlbumRepo.findAll();
            return new ResponseEntity<>(trainerPhotoAlbum, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<TrainerPhotoAlbum>> getMyTrainerPhotoAlbums() {
        try {
            log.info("Inside getMyTrainerPhotoAlbums");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            Trainer trainer = trainerRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (trainer == null) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            List<TrainerPhotoAlbum> trainerPhotoAlbum = trainerPhotoAlbumRepo.findByTrainer(trainer);
            return new ResponseEntity<>(trainerPhotoAlbum, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> addTrainerBenefit(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside addTrainerPricing {}", requestMap);
            boolean isValid = validateTrainerBenefitFromMap(requestMap, false);
            log.info("Is request valid? {}", isValid);

            if (!(jwtFilter.isAdmin() || jwtFilter.isTrainer())) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            Trainer trainer = trainerRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (jwtFilter.isAdmin()) {
                if (requestMap.get("trainerId").isEmpty()) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Admin must provide trainerId");
                }

                trainer = trainerRepo.findByTrainerId(Integer.valueOf(requestMap.get("trainerId")));
            }

            if (trainer == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Trainer not found in db");
            }

            getTrainerBenefitFromMap(requestMap, trainer);
            String responseMessage = jwtFilter.isAdmin() ?
                    trainer.getName() + "'s, trainer benefit have successfully been added to their list" :
                    "Hello " + trainer.getName() + "!, you have successfully added a new benefit to your list";

            // check if all trainer entities are added and set the trainer status to true
            String resultMessage = canUpdateTrainerStatus(trainer, "true");
            if (resultMessage.startsWith("Conditions")) {
                trainer.setStatus("true");
                trainerRepo.save(trainer);
                responseMessage = jwtFilter.isAdmin() ?
                        trainer.getName() + "'s, trainer benefit have successfully been added to their list" +
                                " and their trainer account has been activated successfully" :
                        "Hello " + trainer.getName() + "!, you have successfully added a new benefit to your list" +
                                " and your trainer account has been successfully activated";
            }

            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateTrainerBenefit(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside updateTrainerBenefit {}", requestMap);
            boolean isValid = validateTrainerBenefitFromMap(requestMap, true);
            log.info("Is request valid? {}", isValid);

            if (!(jwtFilter.isAdmin() || jwtFilter.isTrainer())) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            Optional<TrainerBenefit> optional = trainerBenefitRepo.findById(Integer.valueOf(requestMap.get("id")));
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Trainer benefit ID not found");
            }

            TrainerBenefit trainerBenefit = optional.get();
            String currentUser = jwtFilter.getCurrentUserEmail();
            if (!(jwtFilter.isAdmin()
                    || trainerBenefit.getTrainer().getPartner().getUser().getEmail().equals(currentUser))) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            // Split the benefits string using "#" delimiter and store as a list
            List<String> benefits = Arrays.asList(requestMap.get("benefit").split("#"));
            trainerBenefit.setBenefits(benefits);
            trainerBenefit.setLastUpdate(new Date());
            TrainerBenefit savedTrainerBenefit = trainerBenefitRepo.save(trainerBenefit);
            String responseMessage;
            if (jwtFilter.isAdmin()) {
                responseMessage = trainerBenefit.getTrainer().getName() + "'s Trainer benefit updated successfully";
            } else {
                responseMessage = "Hello " +
                        trainerBenefit.getTrainer().getName() + " you have successfully " +
                        " updated your trainer benefit information";
            }

            String adminNotificationMessage = "Trainer benefit with id: " + savedTrainerBenefit.getId()
                    + ", and info: " + savedTrainerBenefit.getBenefits() + "/ has been updated";
            String notificationMessage = "You have update your trainer benefit: " +
                    savedTrainerBenefit.getBenefits();
            jwtFilter.sendNotifications("/topic/updateTrainerBenefit", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, savedTrainerBenefit);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteTrainerBenefit(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside deleteTrainerBenefit {}", id);
            Optional<TrainerBenefit> optional = trainerBenefitRepo.findById(id);
            TrainerBenefit trainerBenefit = optional.orElse(null);

            if (trainerBenefit == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Trainer benefit id not found");
            }

            if (!isAuthorizedToDeleteTrainerBenefit(trainerBenefit)) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!trainerBenefit.getTrainer().getStatus().equalsIgnoreCase("false")) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Trainer is inactive, Cannot complete request");
            }

            trainerBenefitRepo.delete(trainerBenefit);
            String adminNotificationMessage = "Trainer benefit with id: " + trainerBenefit.getId()
                    + ", and info " + trainerBenefit.getBenefits() + ", has been deleted";
            String notificationMessage = "You have successfully deleted your trainer benefit : "
                    + trainerBenefit.getBenefits();
            jwtFilter.sendNotifications("/topic/deleteTrainerBenefit", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, trainerBenefit);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Trainer benefit deleted successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<TrainerBenefit>> getAllTrainerBenefits() {
        try {
            log.info("Inside getAllTrainerBenefits");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<TrainerBenefit> trainerBenefits = trainerBenefitRepo.findAll();
            return new ResponseEntity<>(trainerBenefits, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<TrainerBenefit>> getMyTrainerBenefits() {
        try {
            log.info("Inside getMyTrainerBenefits");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            Trainer trainer = trainerRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (trainer == null) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            List<TrainerBenefit> trainerBenefits = trainerBenefitRepo.findByTrainer(trainer);
            return new ResponseEntity<>(trainerBenefits, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> addTrainerIntroduction(IntroductionRequest introductionRequest) throws JsonProcessingException {
        try {
            log.info("Inside addTrainerIntroduction {}", introductionRequest);
            boolean isValid = introductionRequest != null;
            log.info("Is request valid? {}", isValid);

            if (!(jwtFilter.isAdmin() || jwtFilter.isTrainer())) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            Trainer trainer = trainerRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (jwtFilter.isAdmin()) {
                if (introductionRequest.getTrainerId() == null) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Admin must provide trainerId");
                }

                trainer = trainerRepo.findByTrainerId(introductionRequest.getTrainerId());
            }

            if (trainer == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Trainer not found in db");
            }

            getTrainerIntroductionFromMap(introductionRequest, trainer);
            String responseMessage = jwtFilter.isAdmin() ?
                    trainer.getName() + "'s, trainer introduction have successfully been added to their list" :
                    "Hello " + trainer.getName() + ", you have successfully added an introduction to your trainer profile ";

            // check if all trainer entities are added and set the trainer status to true
            String resultMessage = canUpdateTrainerStatus(trainer, "true");
            if (resultMessage.startsWith("Conditions")) {
                trainer.setStatus("true");
                trainerRepo.save(trainer);
                responseMessage = jwtFilter.isAdmin() ?
                        trainer.getName() + "'s, trainer introduction have successfully been added to their list" +
                                " and their trainer account has been activated successfully" :
                        "Hello " + trainer.getName() + ", you have successfully added an introduction to your trainer profile " +
                                " and your trainer account has been successfully activated";
            }

            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateTrainerIntroduction(IntroductionRequest introductionRequest) throws JsonProcessingException {
        try {
            log.info("Inside updateTrainerIntroduction {}", introductionRequest);
            Integer userId = jwtFilter.getCurrentUserId();
            boolean isValid = introductionRequest != null;
            log.info("Is request valid? {}", isValid);

            if (!(jwtFilter.isAdmin() || jwtFilter.isTrainer())) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid || introductionRequest.getId() == null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            TrainerIntroduction trainerIntroduction = trainerIntroductionRepo.findById(introductionRequest.getId()).orElse(null);
            if (trainerIntroduction == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Trainer introductionRequest not found in db");
            }

            boolean validUser = jwtFilter.isAdmin() ||
                    (jwtFilter.isTrainer() && userId.equals(trainerIntroduction
                            .getTrainer().getPartner().getUser().getId()));
            if (!validUser) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            Trainer trainer = trainerIntroduction.getTrainer();
            byte[] photo = introductionRequest.getCoverPhoto().getBytes();
            trainerIntroduction.setCoverPhoto(photo);
            trainerIntroduction.setIntroduction(introductionRequest.getIntroduction());
            trainerIntroduction.setLastUpdate(new Date());
            TrainerIntroduction savedTrainerIntroduction = trainerIntroductionRepo.save(trainerIntroduction);
            String responseMessage = jwtFilter.isAdmin() ?
                    trainer.getName() + "!, introduction have successfully been updated in their album" :
                    "Hello " + trainer.getName() + "!, you have successfully updated your trainer's profile introduction";
            String adminNotificationMessage = "Trainer introduction with id: " + savedTrainerIntroduction.getId()
                    + ", and info: " + savedTrainerIntroduction.getIntroduction() + "/ has been updated";
            String notificationMessage = "You have update your trainer introduction: " +
                    savedTrainerIntroduction.getIntroduction();
            jwtFilter.sendNotifications("/topic/updateTrainerIntroduction", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, savedTrainerIntroduction);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteTrainerIntroduction(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside deleteTrainerPhotoAlbum {}", id);
            Optional<TrainerIntroduction> optional = trainerIntroductionRepo.findById(id);
            TrainerIntroduction trainerIntroduction = optional.orElse(null);

            if (trainerIntroduction == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Trainer photo id not found");
            }

            if (!isAuthorizedToDeleteTrainerIntroduction(trainerIntroduction)) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!trainerIntroduction.getTrainer().getStatus().equalsIgnoreCase("false")) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Trainer is inactive, Cannot complete request");
            }

            trainerIntroductionRepo.delete(trainerIntroduction);
            String adminNotificationMessage = "Trainer introduction with id: " + trainerIntroduction.getId()
                    + ", and info " + trainerIntroduction.getIntroduction() + ", has been deleted";
            String notificationMessage = "You have successfully deleted your trainer introduction : "
                    + trainerIntroduction.getIntroduction();
            jwtFilter.sendNotifications("/topic/deleteTrainerIntroduction", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, trainerIntroduction);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Trainer introduction deleted from profile successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<TrainerIntroduction>> getAllTrainerIntroductions() {
        try {
            log.info("Inside getAllTrainerIntroductions");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<TrainerIntroduction> trainerIntroductions = trainerIntroductionRepo.findAll();
            return new ResponseEntity<>(trainerIntroductions, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<TrainerIntroduction> getMyTrainerIntroduction() {
        try {
            log.info("Inside getMyTrainerIntroduction");
            if (!(jwtFilter.isAdmin() || jwtFilter.isTrainer())) {
                return new ResponseEntity<>(new TrainerIntroduction(), HttpStatus.UNAUTHORIZED);
            }

            Trainer trainer = trainerRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (trainer == null) {
                return new ResponseEntity<>(new TrainerIntroduction(), HttpStatus.UNAUTHORIZED);
            }

            TrainerIntroduction trainerIntroduction = trainerIntroductionRepo.findByTrainer(trainer);
            return new ResponseEntity<>(trainerIntroduction, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new TrainerIntroduction(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> addTrainerVideoAlbum(VideoAlbumRequest videoAlbumRequest) throws JsonProcessingException {
        try {
            log.info("Inside addTrainerVideoAlbum {}", videoAlbumRequest);

            boolean isValid = videoAlbumRequest != null;
            log.info("Is request valid? {}", isValid);

            if (!jwtFilter.isAdmin() || jwtFilter.isTrainer()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            Trainer trainer = trainerRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (jwtFilter.isAdmin()) {
                if (videoAlbumRequest.getTrainerId() == null) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Admin must provide trainerId");
                }

                trainer = trainerRepo.findByTrainerId(videoAlbumRequest.getTrainerId());
            }

            if (trainer == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Trainer not found in db");
            }

            getTrainerVideoAlbumFromMap(videoAlbumRequest, trainer);
            String responseMessage = jwtFilter.isAdmin() ?
                    trainer.getName() + "'s, video have successfully been added to their album." :
                    "Hello " + trainer.getName() + "!, you have successfully added a video to your album.";

            // check if all trainer entities are added and set the trainer status to true
            String resultMessage = canUpdateTrainerStatus(trainer, "true");
            if (resultMessage.startsWith("Conditions")) {
                trainer.setStatus("true");
                trainerRepo.save(trainer);
                responseMessage = jwtFilter.isAdmin() ?
                        trainer.getName() + "'s, video have successfully been added to their album" +
                                " and their trainer account has been activated successfully." :
                        "Hello " + trainer.getName() + "!, you have successfully added a video to your album" +
                                " and your trainer account has been successfully activated.";
            }

            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateTrainerVideoAlbum(VideoAlbumRequest videoAlbumRequest) throws JsonProcessingException {
        try {
            log.info("Inside updateTrainerVideoAlbum {}", videoAlbumRequest);
            Integer userId = jwtFilter.getCurrentUserId();
            boolean isValid = videoAlbumRequest != null;
            log.info("Is request valid? {}", isValid);

            if (!jwtFilter.isAdmin() || !jwtFilter.isTrainer()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid || videoAlbumRequest.getId() == null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            TrainerVideoAlbum trainerVideoAlbum = trainerVideoAlbumRepo.findById(videoAlbumRequest.getId()).orElse(null);
            if (trainerVideoAlbum == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Video album not found in db");
            }

            boolean validUser = jwtFilter.isAdmin() ||
                    (jwtFilter.isTrainer() && userId.equals(trainerVideoAlbum
                            .getTrainer().getPartner().getUser().getId()));
            if (!validUser) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!jwtFilter.isAdmin() && !trainerVideoAlbum.getTrainer().getId().equals(jwtFilter.getCurrentUserId())) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            Trainer trainer = trainerVideoAlbum.getTrainer();
            String videoFolderPath = BerlizConstants.TRAINER_VIDEO_ALBUM_LOCATION;
            String videoFileName = trainerVideoAlbum.getVideo();
            String videoFilePath = videoFolderPath + videoFileName;
            Path path = Paths.get(videoFilePath);

            // Update video content
            Files.write(path, videoAlbumRequest.getVideo().getBytes());

            // Update video details in the database
            trainerVideoAlbum.setComment(videoAlbumRequest.getComment());
            trainerVideoAlbum.setLastUpdate(new Date());
            TrainerVideoAlbum savedTrainerVideoAlbum = trainerVideoAlbumRepo.save(trainerVideoAlbum);
            String responseMessage = jwtFilter.isAdmin() ?
                    trainer.getName() + "!, video have successfully been updated in their album" :
                    "Hello " + trainer.getName() + "!, you have successfully updated the video in your album";
            String adminNotificationMessage = "Trainer video with id: " + savedTrainerVideoAlbum.getId()
                    + ", and info: " + savedTrainerVideoAlbum.getUuid() + "/ has been updated";
            String notificationMessage = "You have update your trainer video: " +
                    savedTrainerVideoAlbum.getUuid();
            jwtFilter.sendNotifications("/topic/updateTrainerVideoAlbum", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, savedTrainerVideoAlbum);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteTrainerVideoAlbum(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside deleteTrainerVideoAlbum {}", id);
            Optional<TrainerVideoAlbum> optional = trainerVideoAlbumRepo.findById(id);
            TrainerVideoAlbum trainerVideoAlbum = optional.orElse(null);

            if (trainerVideoAlbum == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Trainer video id not found");
            }

            if (!isAuthorizedToDeleteTrainerVideoAlbum(trainerVideoAlbum)) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!trainerVideoAlbum.getTrainer().getStatus().equalsIgnoreCase("false")) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Trainer is inactive, Cannot complete request");
            }

            trainerVideoAlbumRepo.delete(trainerVideoAlbum);
            String adminNotificationMessage = "Trainer video with id: " + trainerVideoAlbum.getId()
                    + ", and info " + trainerVideoAlbum.getComment() + ", has been deleted";
            String notificationMessage = "You have successfully deleted your trainer video : "
                    + trainerVideoAlbum.getComment();
            jwtFilter.sendNotifications("/topic/deleteTrainerVideoAlbum", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, trainerVideoAlbum);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Trainer video deleted from album successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<TrainerVideoAlbum>> getAllTrainerVideoAlbums() {
        try {
            log.info("Inside getAllTrainerVideoAlbums");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<TrainerVideoAlbum> trainerVideoAlbums = trainerVideoAlbumRepo.findAll();
            return new ResponseEntity<>(trainerVideoAlbums, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<TrainerVideoAlbum>> getMyTrainerVideoAlbums() {
        try {
            log.info("Inside getMyTrainerVideoAlbums");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            Trainer trainer = trainerRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (trainer == null) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            List<TrainerVideoAlbum> trainerVideoAlbums = trainerVideoAlbumRepo.findByTrainer(trainer);
            return new ResponseEntity<>(trainerVideoAlbums, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Client>> getMyClients() {
        try {
            log.info("Inside getMyClients");
            Trainer trainer = trainerRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (trainer == null) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            List<Client> clients = clientRepo.getMyClientsByTrainer(trainer);
            return new ResponseEntity<>(clients, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Client>> getMyActiveClients() {
        try {
            log.info("Inside getMyActiveClients");
            Trainer trainer = trainerRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (trainer == null) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            List<Client> clients = clientRepo.getMyActiveClientsByTrainer(trainer);
            return new ResponseEntity<>(clients, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> addTrainerFeatureVideo(FeatureVideoRequest featureVideoRequest) throws JsonProcessingException {
        try {
            log.info("Inside addTrainerFeatureVideo {}", featureVideoRequest);

            boolean isValid = featureVideoRequest != null;
            log.info("Is request valid? {}", isValid);

            if (!jwtFilter.isAdmin() || jwtFilter.isTrainer()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            Trainer trainer = trainerRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (jwtFilter.isAdmin()) {
                if (featureVideoRequest.getTrainerId() == null) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Admin must provide trainerId");
                }

                trainer = trainerRepo.findByTrainerId(featureVideoRequest.getTrainerId());
            }

            if (trainer == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Trainer not found in db");
            }

            List<TrainerFeatureVideo> trainerFeatureVideo = trainerFeatureVideoRepo.findByTrainer(trainer);
            if (trainerFeatureVideo == null || trainerFeatureVideo.size() < 5) {
                getTrainerFeatureVideoFromMap(featureVideoRequest, trainer);
            } else {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED,
                        "You are only allowed to have maximum of 4 feature videos");

            }
            String responseMessage = jwtFilter.isAdmin() ?
                    trainer.getName() + "'s, feature video have successfully been added to their album" :
                    "Hello " + trainer.getName() + "!, you have successfully added a feature video to your album";

            // check if all trainer entities are added and set the trainer status to true
            String resultMessage = canUpdateTrainerStatus(trainer, "true");
            if (resultMessage.startsWith("Conditions")) {
                trainer.setStatus("true");
                trainerRepo.save(trainer);
                responseMessage = jwtFilter.isAdmin() ?
                        trainer.getName() + "'s, feature video have successfully been added to their album" +
                                " and their trainer account has been activated successfully." :
                        "Hello " + trainer.getName() + "!, you have successfully added a feature video to your album" +
                                " and your trainer account has been successfully activated.";
            }

            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateTrainerFeatureVideo(FeatureVideoRequest featureVideoRequest) throws JsonProcessingException {
        try {
            log.info("Inside updateTrainerFeatureVideo {}", featureVideoRequest);
            Integer userId = jwtFilter.getCurrentUserId();
            boolean isValid = featureVideoRequest != null;
            log.info("Is request valid? {}", isValid);

            if (!jwtFilter.isAdmin() || !jwtFilter.isTrainer()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid || featureVideoRequest.getId() == null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            TrainerFeatureVideo featureVideo = trainerFeatureVideoRepo.findById(featureVideoRequest.getId()).orElse(null);
            if (featureVideo == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Trainer feature video not found in db");
            }

            boolean validUser = jwtFilter.isAdmin() ||
                    (jwtFilter.isTrainer() && userId.equals(featureVideo
                            .getTrainer().getPartner().getUser().getId()));
            if (!validUser) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            Trainer trainer = featureVideo.getTrainer();
            String videoFolderPath = BerlizConstants.TRAINER_PHOTO_ALBUM_LOCATION;
            String videoFileName = featureVideo.getVideo();
            String videoFilePath = videoFolderPath + videoFileName;
            Path path = Paths.get(videoFilePath);

            // Update photo content
            Files.write(path, featureVideoRequest.getVideo().getBytes());

            // Update photo details in the database
            featureVideo.setMotivation(featureVideoRequest.getMotivation());
            featureVideo.setLastUpdate(new Date());
            TrainerFeatureVideo savedFeatureVideo = trainerFeatureVideoRepo.save(featureVideo);
            String responseMessage = jwtFilter.isAdmin() ?
                    trainer.getName() + "!, feature video have successfully been updated in their album" :
                    "Hello " + trainer.getName() + "!, you have successfully updated the a feature video in your album";
            String adminNotificationMessage = "Trainer feature video with id: " + savedFeatureVideo.getId()
                    + ", and info: " + savedFeatureVideo.getMotivation() + "/ has been updated";
            String notificationMessage = "You have update your trainer feature video: " +
                    savedFeatureVideo.getMotivation();
            jwtFilter.sendNotifications("/topic/updateTrainerFeatureVideo", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, savedFeatureVideo);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteTrainerFeatureVideo(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside deleteTrainerFeatureVideo {}", id);
            Optional<TrainerFeatureVideo> optional = trainerFeatureVideoRepo.findById(id);
            TrainerFeatureVideo trainerFeatureVideo = optional.orElse(null);
            if (trainerFeatureVideo == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Trainer feature video id not found");
            }

            if (!isAuthorizedToDeleteTrainerFeatureVideo(trainerFeatureVideo)) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!trainerFeatureVideo.getTrainer().getStatus().equalsIgnoreCase("false")) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Trainer is inactive, Cannot complete request");
            }

            trainerFeatureVideoRepo.delete(trainerFeatureVideo);
            String adminNotificationMessage = "Trainer feature video with id: " + trainerFeatureVideo.getId()
                    + ", and info " + trainerFeatureVideo.getMotivation() + ", has been deleted";
            String notificationMessage = "You have successfully deleted your trainer feature video : "
                    + trainerFeatureVideo.getMotivation();
            jwtFilter.sendNotifications("/topic/deleteTrainerFeatureVideo", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, trainerFeatureVideo);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Trainer feature video deleted from album successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<TrainerFeatureVideo>> getAllTrainerFeatureVideos() {
        try {
            log.info("Inside getAllTrainerFeatureVideos");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<TrainerFeatureVideo> trainerFeatureVideos = trainerFeatureVideoRepo.findAll();
            return new ResponseEntity<>(trainerFeatureVideos, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<TrainerFeatureVideo>> getMyTrainerFeatureVideos() {
        try {
            log.info("Inside getMyTrainerFeatureVideos");
            if (!(jwtFilter.isAdmin() || jwtFilter.isTrainer())) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            Trainer trainer = trainerRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (trainer == null) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            List<TrainerFeatureVideo> trainerFeatureVideos = trainerFeatureVideoRepo.findByTrainer(trainer);
            return new ResponseEntity<>(trainerFeatureVideos, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> likeTrainerReview(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside likeClientReview {}", id);
            Optional<TrainerReview> optional = trainerReviewRepo.findById(id);
            TrainerReview trainerReview = optional.orElse(null);
            User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
            boolean validUser = jwtFilter.isBerlizUser();

            if (!validUser) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (trainerReview == null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Trainer review id not found");
            }

            boolean hasLiked = trainerReviewLikeRepo.existsByUserAndTrainerReview(user, trainerReview);
            String responseMessage;
            if (hasLiked) {
                // dislike trainer
                trainerReviewLikeRepo.deleteByUserAndTrainerReview(user, trainerReview);
                trainerReview.setLikes(trainerReview.getLikes() - 1);
                responseMessage = "Hello, " + user.getFirstname() + " you have disliked " +
                        trainerReview.getClient().getUser().getFirstname() + " review on" +
                        trainerReview.getTrainer().getName() + "'s profile";
                String adminNotificationMessage = "Trainer review with id: " + trainerReview.getId() +
                        ", and info: " + trainerReview.getReview() + " has just been disliked by: "
                        + user.getEmail();
                String notificationMessage = "You have successfully disliked a trainer review  : "
                        + trainerReview.getReview();
                jwtFilter.sendNotifications("/topic/likeTrainerReview", adminNotificationMessage,
                        jwtFilter.getCurrentUser(), notificationMessage, trainerReview);
            } else {
                // like trainer
                TrainerReviewLike trainerReviewLike = new TrainerReviewLike();
                trainerReviewLike.setUser(user);
                trainerReviewLike.setTrainerReview(trainerReview);
                trainerReviewLike.setDate(new Date());
                trainerReviewLikeRepo.save(trainerReviewLike);
                trainerReview.setLikes(trainerReview.getLikes() + 1);
                responseMessage = "Hello, " + user.getFirstname() + " you just liked " +
                        trainerReview.getClient().getUser().getFirstname() + " review on" +
                        trainerReview.getTrainer().getName() + "'s profile";
                String adminNotificationMessage = "Trainer review with id: " + trainerReview.getId() +
                        ", and info: " + trainerReview.getReview() + " has just been liked by: "
                        + user.getEmail();
                String notificationMessage = "You have successfully liked a trainer review  : "
                        + trainerReview.getReview();
                jwtFilter.sendNotifications("/topic/likeTrainerReview", adminNotificationMessage,
                        jwtFilter.getCurrentUser(), notificationMessage, trainerReview);
            }

            TrainerReview savedTrainerReview = trainerReviewRepo.save(trainerReview);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }


    @Override
    public ResponseEntity<List<TrainerReviewLike>> getTrainerReviewLikes() {
        try {
            log.info("Inside getTrainerLikes");
            List<TrainerReviewLike> trainerReviewLikes = trainerReviewLikeRepo.findAll();
            return new ResponseEntity<>(trainerReviewLikes, HttpStatus.OK);
        } catch (Exception ex) {
            log.error("Something went wrong while performing operation", ex);
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<CenterTrainer>> getMyCenterTrainers() {
        try {
            log.info("Inside getMyCenterTrainers");
            if (!(jwtFilter.isAdmin()) || jwtFilter.isCenter()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            Trainer trainer = trainerRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (trainer == null) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            List<CenterTrainer> centerTrainers = centerTrainerRepo.findByTrainer(trainer);
            return new ResponseEntity<>(centerTrainers, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> addTrainerReview(TrainerReviewRequest trainerReviewRequest) throws JsonProcessingException {
        try {
            log.info("Inside addTrainerReview {}", trainerReviewRequest);
            boolean isValid = trainerReviewRequest != null;
            log.info("Is request valid? {}", isValid);

            if (!(jwtFilter.isAdmin() || jwtFilter.isClient())) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            if (trainerReviewRequest.getTrainerId() == null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "trainer id is invalid");
            }

            Trainer trainer = trainerRepo.findByTrainerId(trainerReviewRequest.getTrainerId());
            if (trainer == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Trainer not found in db");
            }

            Client client = clientRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (jwtFilter.isAdmin()) {
                if (trainerReviewRequest.getTrainerId() == null) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Admin must provide center Id");
                }

                client = clientRepo.findByUserId(trainerReviewRequest.getClientId());
            }

            if (client == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Client not found in db");
            }

            getTrainerReviewFromMap(trainerReviewRequest, trainer, client);
            String responseMessage = jwtFilter.isAdmin() ?
                    client.getUser().getFirstname() + "!, review for " + trainer.getName() + " have " +
                            "successfully been added to their list" :
                    "Hello " + client.getUser().getFirstname() + "!, you have successfully " +
                            "added a review for your trainer " + trainer.getName();
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateTrainerReview(TrainerReviewRequest trainerReviewRequest) throws JsonProcessingException {
        try {
            log.info("Inside updateTrainerReview {}", trainerReviewRequest);
            Integer userId = jwtFilter.getCurrentUserId();
            boolean isValid = trainerReviewRequest != null;
            log.info("Is request valid? {}", isValid);

            if (!jwtFilter.isAdmin() || !jwtFilter.isClient()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid || trainerReviewRequest.getId() == null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            TrainerReview trainerReview = trainerReviewRepo.findById(trainerReviewRequest.getId()).orElse(null);
            if (trainerReview == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Trainer review" +
                        " for trainer not found in db");
            }

            boolean validUser = jwtFilter.isAdmin() ||
                    (jwtFilter.isTrainer() && userId.equals(trainerReview
                            .getClient().getUser().getId()));
            if (!validUser) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            Trainer trainer = trainerReview.getTrainer();
            String photoFolderPath = BerlizConstants.TRAINER_CLIENT_REVIEW;

            String frontBeforePhotoName = generateUniqueName(trainer.getName() + "_client_front_before");
            String frontAfterPhotoName = generateUniqueName(trainer.getName() + "_client_front_after");
            String sideBeforePhotoName = generateUniqueName(trainer.getName() + "_client_side_before");
            String sideAfterPhotoName = generateUniqueName(trainer.getName() + "_client_side_after");
            String backBeforePhotoName = generateUniqueName(trainer.getName() + "_client_back_before");
            String backAfterPhotoName = generateUniqueName(trainer.getName() + "_client_back_after");

            Path frontBeforePath = Paths.get(photoFolderPath, frontBeforePhotoName);
            Path frontAfterPath = Paths.get(photoFolderPath, frontAfterPhotoName);
            Path sideBeforePath = Paths.get(photoFolderPath, sideBeforePhotoName);
            Path sideAfterPath = Paths.get(photoFolderPath, sideAfterPhotoName);
            Path backBeforePath = Paths.get(photoFolderPath, backBeforePhotoName);
            Path backAfterPath = Paths.get(photoFolderPath, backAfterPhotoName);

            Files.write(frontBeforePath, trainerReviewRequest.getFrontBefore().getBytes());
            Files.write(frontAfterPath, trainerReviewRequest.getFrontAfter().getBytes());
            Files.write(sideBeforePath, trainerReviewRequest.getSideBefore().getBytes());
            Files.write(sideAfterPath, trainerReviewRequest.getSideAfter().getBytes());
            Files.write(backBeforePath, trainerReviewRequest.getBackBefore().getBytes());
            Files.write(backAfterPath, trainerReviewRequest.getBackAfter().getBytes());

            // Save photo details to the database
            trainerReview.setReview(trainerReviewRequest.getReview());
            trainerReview.setFrontBefore(frontBeforePhotoName);
            trainerReview.setFrontAfter(frontAfterPhotoName);
            trainerReview.setSideBefore(sideBeforePhotoName);
            trainerReview.setSideAfter(sideAfterPhotoName);
            trainerReview.setBackBefore(backBeforePhotoName);
            trainerReview.setBackAfter(backAfterPhotoName);
            trainerReview.setLastUpdate(new Date());

            TrainerReview savedTrainerReview = trainerReviewRepo.save(trainerReview);
            String responseMessage = jwtFilter.isAdmin() ?
                    "Trainer review for " + trainer.getName() + "!, has successfully been updated in their album" :
                    "Hello " + trainerReview.getClient().getUser().getFirstname() + "!, you have successfully " +
                            "updated your Trainer review for" +
                            trainer.getName();
            String adminNotificationMessage = "Trainer review with id: " + savedTrainerReview.getId()
                    + ", and info: " + savedTrainerReview.getReview() + "/ has been updated";
            String notificationMessage = "You have update your trainer review: " +
                    savedTrainerReview.getReview();
            jwtFilter.sendNotifications("/topic/updateTrainerReview", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, savedTrainerReview);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateTrainerReviewStatus(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside updateTrainerReviewStatus {}", id);
            String status;
            if (!(jwtFilter.isAdmin() || jwtFilter.isTrainer())) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            Optional<TrainerReview> optional = trainerReviewRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Trainer review ID not found");
            }

            log.info("Inside optional {}", optional);
            status = optional.get().getStatus();
            TrainerReview trainerTrainerReview = optional.get();
            String responseMessage;
            if (status.equalsIgnoreCase("true")) {
                status = "false";
                responseMessage = "Trainer review status updated successfully. Now deactivated";
            } else {
                status = "true";
                responseMessage = "Trainer review status updated successfully. Now activated";
            }

            trainerTrainerReview.setStatus(status);
            TrainerReview savedTrainerReview = trainerReviewRepo.save(trainerTrainerReview);
            String adminNotificationMessage = "Trainer review with id: " + savedTrainerReview.getId() +
                    ", status has been set to " + savedTrainerReview.getStatus();
            String notificationMessage = "You have successfully set your trainer review status to : " +
                    savedTrainerReview.getStatus();
            jwtFilter.sendNotifications("/topic/updateTrainerReviewStatus", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, savedTrainerReview);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> disableTrainerReview(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside disableTrainerReview {}", id);
            if (!(jwtFilter.isAdmin() || jwtFilter.isClient() || jwtFilter.isTrainer() || jwtFilter.isMemberClient())) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            Optional<TrainerReview> optional = trainerReviewRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Trainer review ID not found");
            }

            log.info("Inside optional {}", optional);
            TrainerReview trainerReview = optional.get();
            trainerReview.setStatus("false");
            TrainerReview savedTrainerReview = trainerReviewRepo.save(trainerReview);
            String adminNotificationMessage = "Trainer review with id: " + savedTrainerReview.getId() + ", and info "
                    + savedTrainerReview.getReview() + ", has been disabled for: " + savedTrainerReview.getTrainer().getName();
            String notificationMessage = "You have successfully disabled a review for trainer : "
                    + savedTrainerReview.getTrainer().getName() + " and review info: " + savedTrainerReview.getReview();
            jwtFilter.sendNotifications("/topic/disableTrainerReview", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, savedTrainerReview);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Review for " + trainerReview.getTrainer().getName() +
                    " has been removed from feed successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteTrainerReview(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside deleteTrainerReview {}", id);
            if (!(jwtFilter.isAdmin() || jwtFilter.isClient())) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            Optional<TrainerReview> optional = trainerReviewRepo.findById(id);
            TrainerReview trainerReview = optional.orElse(null);
            if (trainerReview == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Trainer review id not found");
            }

            if (isAuthorizedToDeleteTrainerReview(trainerReview)) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!trainerReview.getTrainer().getStatus().equalsIgnoreCase("false")) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Trainer is inactive, Cannot complete request");
            }

            trainerReviewRepo.delete(trainerReview);
            String adminNotificationMessage = "Trainer review with id: " + trainerReview.getId() +
                    ", and info " + trainerReview.getReview() + ", has been deleted";
            String notificationMessage = "You have successfully deleted your trainer trainer review : "
                    + trainerReview.getReview();
            jwtFilter.sendNotifications("/topic/deleteTrainerReview", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, trainerReview);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Trainer review for" +
                    trainerReview.getTrainer().getName() + "  deleted successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
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


    @Override
    public ResponseEntity<List<TrainerReview>> getAllTrainerReviews() {
        try {
            log.info("Inside getAllTrainerReviews");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<TrainerReview> trainerTrainerReviews = trainerReviewRepo.findAll();
            return new ResponseEntity<>(trainerTrainerReviews, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<TrainerReview>> getActiveTrainerReviews(Integer id) {
        try {
            log.info("Inside getActiveTrainerReviews");
            Trainer trainer = trainerRepo.findByTrainerId(id);
            if (trainer == null) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            List<TrainerReview> trainerTrainerReviews = trainerReviewRepo.getActiveTrainerReviewsByTrainer(trainer);
            return new ResponseEntity<>(trainerTrainerReviews, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
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
     */
    private void getTrainerFromMap(TrainerRequest trainerRequest) throws IOException {
        Trainer trainer = new Trainer();
        User user;
        Partner partner = partnerRepo.findByPartnerId(trainerRequest.getPartnerId());
        if (jwtFilter.isAdmin()) {
            String userEmail = partner.getUser().getEmail();
            user = userRepo.findByEmail(userEmail);
        } else {
            user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
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
        String adminNotificationMessage = "A new trainer with id: " + savedTrainer.getId()
                + " and info" + savedTrainer.getName() + ", has been added";
        String notificationMessage = "You have successfully added your account as a trainer: " + savedTrainer.getName();
        jwtFilter.sendNotifications("/topic/getTrainerFromMap", adminNotificationMessage,
                jwtFilter.getCurrentUser(), notificationMessage, savedTrainer);
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

            if (isValidRole(partnerId)) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Invalid partner role. Partner must be a trainer");
            }

            if (isApprovedPartner(partnerId)) {
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
            if (partner == null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Partner id not found");
            }

            Integer partnerId = partner.getId();
            Trainer trainer = trainerRepo.findByPartnerId(partnerId);
            if (trainer != null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Trainer already exists");
            }

            if (isValidRole(partnerId)) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Invalid partner role. Partner must be a trainer");
            }

            if (isApprovedPartner(partnerId)) {
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
     * @param partnerId ID of the partner to be validated
     * @return The valid partner
     */
    private boolean isValidRole(Integer partnerId) {
        Partner partner = partnerRepo.findByPartnerId(partnerId);
        return partner == null || !partner.getRole().equalsIgnoreCase("trainer");
    }

    /**
     * Validates if Trainer is an approved partner.
     *
     * @param partnerId ID of the partner to be approved
     * @return The valid partner
     */
    private boolean isApprovedPartner(Integer partnerId) {
        Partner partner = partnerRepo.findByPartnerId(partnerId);
        return partner == null || !partner.getStatus().equalsIgnoreCase("true");
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

    /**
     * Retrieves Trainer Pricing information from the given request map and associates it with the specified Trainer.
     *
     * @param requestMap A map containing pricing information as key-value pairs.
     * @param trainer    The Trainer entity to associate the pricing information with.
     */
    private void getTrainerPricingFromMap(Map<String, String> requestMap, Trainer trainer) {
        TrainerPricing trainerPricing = new TrainerPricing();
        trainerPricing.setTrainer(trainer);

        // Extract pricing information from the request map
        BigDecimal priceOnline = new BigDecimal(requestMap.get("priceOnline"));
        trainerPricing.setPriceOnline(priceOnline);
        BigDecimal priceHybrid = new BigDecimal(requestMap.get("priceHybrid"));
        trainerPricing.setPriceHybrid(priceHybrid);
        BigDecimal pricePersonal = new BigDecimal(requestMap.get("pricePersonal"));
        trainerPricing.setPricePersonal(pricePersonal);

        // Extract discount information from the request map
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

        // Set date and last update timestamps
        trainerPricing.setDate(new Date());
        trainerPricing.setLastUpdate(new Date());

        // Save TrainerPricing entity and broadcast the updated information
        TrainerPricing savedTrainerPricing = trainerPricingRepo.save(trainerPricing);
        String adminNotificationMessage = "A new pricing with id: " + savedTrainerPricing.getId()
                + " and info" + savedTrainerPricing.getPricePersonal()
                + ", has been added for trainer: " + savedTrainerPricing.getTrainer().getName();
        String notificationMessage = "You have successfully added pricing for your center: "
                + savedTrainerPricing.getPriceOnline();
        jwtFilter.sendNotifications("/topic/getTrainerPricingFromMap", adminNotificationMessage,
                jwtFilter.getCurrentUser(), notificationMessage, savedTrainerPricing);
    }

    /**
     * Processes a PhotoAlbumRequest and associates it with the specified Trainer, saving both the photo file and its details.
     *
     * @param photoAlbumRequest The PhotoAlbumRequest object containing the photo and associated information.
     * @param trainer           The Trainer entity to associate the PhotoAlbumRequest with.
     * @throws IOException If an I/O error occurs while saving the photo file or TrainerPhotoAlbum entity.
     */
    private void getTrainerPhotoAlbumFromMap(PhotoAlbumRequest photoAlbumRequest, Trainer trainer) throws IOException {
        TrainerPhotoAlbum trainerPhotoAlbum = new TrainerPhotoAlbum();

        // Generate a unique photo file name based on Trainer's name and a truncated UUID
        String photoFolderPath = BerlizConstants.TRAINER_PHOTO_ALBUM_LOCATION;
        long numericUUID = UUID.randomUUID().getMostSignificantBits();
        String truncatedUUID = String.valueOf(numericUUID).substring(0, 15);
        String photoFileName = trainer.getName() + "_photo-" + truncatedUUID;
        String photoFilePath = photoFolderPath + photoFileName;

        // Save the photo file to the specified path
        Path path = Paths.get(photoFilePath);
        Files.write(path, photoAlbumRequest.getPhoto().getBytes());

        // Save photo details to the database
        trainerPhotoAlbum.setTrainer(trainer);
        trainerPhotoAlbum.setPhoto(photoFileName);
        trainerPhotoAlbum.setComment(photoAlbumRequest.getComment());
        trainerPhotoAlbum.setDate(new Date());
        trainerPhotoAlbum.setLastUpdate(new Date());

        // Save TrainerPhotoAlbum entity and broadcast the updated information
        TrainerPhotoAlbum savedTrainerPhotoAlbum = trainerPhotoAlbumRepo.save(trainerPhotoAlbum);
        String adminNotificationMessage = "A new photo with id: " + savedTrainerPhotoAlbum.getId()
                + " and info" + savedTrainerPhotoAlbum.getUuid()
                + ", has been added for trainer: " + trainer.getName();
        String notificationMessage = "You have successfully added a photo to your trainer's profile: "
                + savedTrainerPhotoAlbum.getUuid();
        jwtFilter.sendNotifications("/topic/getTrainerPhotoAlbumFromMap", adminNotificationMessage,
                jwtFilter.getCurrentUser(), notificationMessage, savedTrainerPhotoAlbum);
    }

    /**
     * Processes a Map containing Trainer benefit information and associates it with the specified Trainer.
     *
     * @param requestMap The Map containing benefit information keyed by attribute names.
     * @param trainer    The Trainer entity to associate the benefit with.
     */
    private void getTrainerBenefitFromMap(Map<String, String> requestMap, Trainer trainer) {
        TrainerBenefit trainerBenefit = new TrainerBenefit();

        // Split the benefits string using "#" delimiter and store as a list
        List<String> benefits = Arrays.asList(requestMap.get("benefit").split("#"));
        trainerBenefit.setBenefits(benefits);

        // Set TrainerBenefit properties from the provided Map
        trainerBenefit.setTrainer(trainer);
        trainerBenefit.setDate(new Date());
        trainer.setLastUpdate(new Date());

        // Save TrainerBenefit entity and broadcast the updated information
        TrainerBenefit savedTrainerBenefit = trainerBenefitRepo.save(trainerBenefit);
        String adminNotificationMessage = "A new benefit with id: " + savedTrainerBenefit.getId()
                + " and info" + savedTrainerBenefit.getBenefits()
                + ", has been added for trainer: " + trainer.getName();
        String notificationMessage = "You have successfully added a new benefit for your trainer profile: "
                + savedTrainerBenefit.getBenefits();
        jwtFilter.sendNotifications("/topic/getTrainerBenefitFromMap", adminNotificationMessage,
                jwtFilter.getCurrentUser(), notificationMessage, savedTrainerBenefit);
    }

    /**
     * Validates a Map containing Trainer benefit information based on a given validation status.
     *
     * @param requestMap The Map containing benefit information keyed by attribute names.
     * @param isValid    A boolean indicating the current validation status.
     * @return True if the Map is valid according to the validation status, false otherwise.
     */
    private boolean validateTrainerBenefitFromMap(Map<String, String> requestMap, boolean isValid) {
        if (isValid) {
            // Validate additional attributes when isValid is true
            return requestMap.containsKey("id")
                    && requestMap.containsKey("benefit");
        }
        // Validate only the "benefit" attribute when isValid is false
        return requestMap.containsKey("benefit");
    }

    /**
     * Processes a Trainer's introductionRequest information from a Map and saves it to the database.
     *
     * @param introductionRequest The IntroductionRequest object containing introductionRequest details.
     * @param trainer             The Trainer object associated with the introductionRequest.
     * @throws IOException If an I/O error occurs while handling the introductionRequest's cover photo.
     */
    private void getTrainerIntroductionFromMap(IntroductionRequest introductionRequest, Trainer trainer) throws IOException {
        TrainerIntroduction trainerIntroduction = new TrainerIntroduction();
        trainerIntroduction.setTrainer(trainer);
        trainerIntroduction.setIntroduction(introductionRequest.getIntroduction());
        byte[] photo = introductionRequest.getCoverPhoto().getBytes();
        trainerIntroduction.setCoverPhoto(photo);
        trainerIntroduction.setDate(new Date());
        trainerIntroduction.setLastUpdate(new Date());
        TrainerIntroduction savedTrainerIntroduction = trainerIntroductionRepo.save(trainerIntroduction);
        String adminNotificationMessage = "A savedTrainerIntroduction introduction with id: " + savedTrainerIntroduction.getId()
                + " and info" + savedTrainerIntroduction.getIntroduction()
                + ", has been added for trainer: " + trainer.getName();
        String notificationMessage = "You have successfully added an introduction for your trainer: "
                + savedTrainerIntroduction.getIntroduction();
        jwtFilter.sendNotifications("/topic/getTrainerIntroductionFromMap", adminNotificationMessage,
                jwtFilter.getCurrentUser(), notificationMessage, savedTrainerIntroduction);
    }

    /**
     * Processes a Trainer's video album information from a VideoAlbumRequest object and saves it to the database.
     *
     * @param videoAlbumRequest The VideoAlbumRequest object containing video album details.
     * @param trainer           The Trainer object associated with the video album.
     * @throws IOException If an I/O error occurs while handling the video file.
     */
    private void getTrainerVideoAlbumFromMap(VideoAlbumRequest videoAlbumRequest, Trainer trainer) throws IOException {
        TrainerVideoAlbum trainerVideoAlbum = new TrainerVideoAlbum();
        String videoFolderPath = BerlizConstants.TRAINER_VIDEO_ALBUM_LOCATION;
        long numericUUID = UUID.randomUUID().getMostSignificantBits();
        String truncatedUUID = String.valueOf(numericUUID).substring(0, 15);
        String videoFileName = trainer.getName() + "_video-" + truncatedUUID;
        String videoFilePath = videoFolderPath + videoFileName;
        Path path = Paths.get(videoFilePath);
        Files.write(path, videoAlbumRequest.getVideo().getBytes());

        // Save video details to the database
        trainerVideoAlbum.setTrainer(trainer);
        trainerVideoAlbum.setVideo(videoFileName);
        trainerVideoAlbum.setComment(videoAlbumRequest.getComment());
        trainerVideoAlbum.setDate(new Date());
        trainerVideoAlbum.setLastUpdate(new Date());
        TrainerVideoAlbum savedTrainerVideoAlbum = trainerVideoAlbumRepo.save(trainerVideoAlbum);
        String adminNotificationMessage = "A new video with id: " + savedTrainerVideoAlbum.getId()
                + " and info" + savedTrainerVideoAlbum.getComment()
                + ", has been added for trainer: " + trainer.getName();
        String notificationMessage = "You have successfully added a video to your trainer's profile: "
                + savedTrainerVideoAlbum.getComment();
        jwtFilter.sendNotifications("/topic/getTrainerVideoAlbumFromMap", adminNotificationMessage,
                jwtFilter.getCurrentUser(), notificationMessage, savedTrainerVideoAlbum);
    }

    /**
     * Processes a Trainer's feature video information from a FeatureVideoRequest object and saves it to the database.
     *
     * @param featureVideoRequest The FeatureVideoRequest object containing feature video details.
     * @param trainer             The Trainer object associated with the feature video.
     * @throws IOException If an I/O error occurs while handling the video file.
     */
    private void getTrainerFeatureVideoFromMap(FeatureVideoRequest featureVideoRequest, Trainer trainer) throws IOException {
        TrainerFeatureVideo trainerFeatureVideo = new TrainerFeatureVideo();
        String videoFolderPath = BerlizConstants.TRAINER_FEATURE_VIDEO;
        long numericUUID = UUID.randomUUID().getMostSignificantBits();
        String truncatedUUID = String.valueOf(numericUUID).substring(0, 15);
        String videoFileName = trainer.getName() + "_video-" + truncatedUUID;
        String videoFilePath = videoFolderPath + videoFileName;
        Path path = Paths.get(videoFilePath);
        Files.write(path, featureVideoRequest.getVideo().getBytes());

        // Save video details to the database
        trainerFeatureVideo.setTrainer(trainer);
        trainerFeatureVideo.setVideo(videoFileName);
        trainerFeatureVideo.setMotivation(featureVideoRequest.getMotivation());
        trainerFeatureVideo.setDate(new Date());
        trainerFeatureVideo.setLastUpdate(new Date());
        TrainerFeatureVideo savedTrainerFeatureVideo = trainerFeatureVideoRepo.save(trainerFeatureVideo);
        String adminNotificationMessage = "A new feature video with id: " + savedTrainerFeatureVideo.getId()
                + " and info" + savedTrainerFeatureVideo.getMotivation()
                + ", has been added for trainer: " + trainer.getName();
        String notificationMessage = "You have successfully added a feature video to your trainer's profile: "
                + savedTrainerFeatureVideo.getMotivation();
        jwtFilter.sendNotifications("/topic/getTrainerFeatureVideoFromMap", adminNotificationMessage,
                jwtFilter.getCurrentUser(), notificationMessage, savedTrainerFeatureVideo);
    }

    /**
     * Processes a Trainer's review information from a TrainerReviewRequest object and saves it to the database.
     *
     * @param trainerReviewRequest The TrainerReviewRequest object containing client review details.
     * @param trainer              The Trainer object associated with the review.
     * @param client               The Client object associated with the review.
     * @throws IOException If an I/O error occurs while handling the client review photos.
     */
    private void getTrainerReviewFromMap(TrainerReviewRequest trainerReviewRequest, Trainer trainer, Client client) throws IOException {
        TrainerReview trainerReview = new TrainerReview();
        String photoFolderPath = BerlizConstants.TRAINER_CLIENT_REVIEW;

        String frontBeforePhotoName = generateUniqueName(trainer.getName() + "_client_front_before");
        String frontAfterPhotoName = generateUniqueName(trainer.getName() + "_client_front_after");
        String sideBeforePhotoName = generateUniqueName(trainer.getName() + "_client_side_before");
        String sideAfterPhotoName = generateUniqueName(trainer.getName() + "_client_side_after");
        String backBeforePhotoName = generateUniqueName(trainer.getName() + "_client_back_before");
        String backAfterPhotoName = generateUniqueName(trainer.getName() + "_client_back_after");

        Path frontBeforePath = Paths.get(photoFolderPath, frontBeforePhotoName);
        Path frontAfterPath = Paths.get(photoFolderPath, frontAfterPhotoName);
        Path sideBeforePath = Paths.get(photoFolderPath, sideBeforePhotoName);
        Path sideAfterPath = Paths.get(photoFolderPath, sideAfterPhotoName);
        Path backBeforePath = Paths.get(photoFolderPath, backBeforePhotoName);
        Path backAfterPath = Paths.get(photoFolderPath, backAfterPhotoName);

        Files.write(frontBeforePath, trainerReviewRequest.getFrontBefore().getBytes());
        Files.write(frontAfterPath, trainerReviewRequest.getFrontAfter().getBytes());
        Files.write(sideBeforePath, trainerReviewRequest.getSideBefore().getBytes());
        Files.write(sideAfterPath, trainerReviewRequest.getSideAfter().getBytes());
        Files.write(backBeforePath, trainerReviewRequest.getBackBefore().getBytes());
        Files.write(backAfterPath, trainerReviewRequest.getBackAfter().getBytes());

        // Save photo details to the database
        trainerReview.setTrainer(trainer);
        trainerReview.setClient(client);
        trainerReview.setReview(trainerReviewRequest.getReview());
        trainerReview.setLikes(0);
        trainerReview.setFrontBefore(frontBeforePhotoName);
        trainerReview.setFrontAfter(frontAfterPhotoName);
        trainerReview.setSideBefore(sideBeforePhotoName);
        trainerReview.setSideAfter(sideAfterPhotoName);
        trainerReview.setBackBefore(backBeforePhotoName);
        trainerReview.setBackAfter(backAfterPhotoName);
        trainerReview.setDate(new Date());
        trainerReview.setStatus("false");
        trainerReview.setLastUpdate(new Date());
        TrainerReview savedTrainerReview = trainerReviewRepo.save(trainerReview);
        String adminNotificationMessage = "A new review with id: " + savedTrainerReview.getId() + " and info"
                + savedTrainerReview.getReview() + ", has been added for trainer: " + trainer.getName();
        String notificationMessage = "You have successfully added a review for trainer: " + trainer.getName();
        jwtFilter.sendNotifications("/topic/getTrainerReviewFromMap", adminNotificationMessage,
                jwtFilter.getCurrentUser(), notificationMessage, savedTrainerReview);
    }

    /**
     * Generates a unique name by combining the given prefix with a truncated UUID.
     *
     * @param prefix The prefix to be included in the unique name.
     * @return The generated unique name.
     */
    String generateUniqueName(String prefix) {
        long numericUUID = UUID.randomUUID().getMostSignificantBits();
        String truncatedUUID = String.valueOf(numericUUID).substring(0, 15);
        return prefix + "-" + truncatedUUID;
    }

    /**
     * Checks if the current user is authorized to delete a TrainerClientReview.
     *
     * @param trainerReview The TrainerReview to be checked for authorization.
     * @return True if the user is authorized, false otherwise.
     */
    private boolean isAuthorizedToDeleteTrainerReview(TrainerReview trainerReview) {
        User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
        boolean authorizedUser = user.getEmail().equalsIgnoreCase(trainerReview.getTrainer()
                .getPartner().getUser().getEmail());
        return !jwtFilter.isAdmin() && !authorizedUser;
    }

    /**
     * Checks if the current user is authorized to delete a TrainerPhotoAlbum.
     *
     * @param trainerPhotoAlbum The TrainerPhotoAlbum to be checked for authorization.
     * @return True if the user is authorized, false otherwise.
     */
    private boolean isAuthorizedToDeleteTrainerPhotoAlbum(TrainerPhotoAlbum trainerPhotoAlbum) {
        User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
        boolean authorizedUser = user.getEmail().equalsIgnoreCase(trainerPhotoAlbum.getTrainer().getPartner().getUser().getEmail());
        return jwtFilter.isAdmin() || authorizedUser;
    }

    /**
     * Checks if the current user is authorized to delete a TrainerBenefit.
     *
     * @param trainerBenefit The TrainerBenefit to be checked for authorization.
     * @return True if the user is authorized, false otherwise.
     */
    private boolean isAuthorizedToDeleteTrainerBenefit(TrainerBenefit trainerBenefit) {
        User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
        boolean authorizedUser = user.getEmail().equalsIgnoreCase(trainerBenefit.getTrainer().getPartner().getUser().getEmail());
        return jwtFilter.isAdmin() || authorizedUser;
    }

    /**
     * Checks if the current user is authorized to delete a TrainerIntroduction.
     *
     * @param trainerIntroduction The TrainerIntroduction to be checked for authorization.
     * @return True if the user is authorized, false otherwise.
     */
    private boolean isAuthorizedToDeleteTrainerIntroduction(TrainerIntroduction trainerIntroduction) {
        User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
        boolean authorizedUser = user.getEmail().equalsIgnoreCase(trainerIntroduction.getTrainer().getPartner().getUser().getEmail());
        return jwtFilter.isAdmin() || authorizedUser;
    }

    /**
     * Checks if the current user is authorized to delete a TrainerVideoAlbum.
     *
     * @param trainerVideoAlbum The TrainerVideoAlbum to be checked for authorization.
     * @return True if the user is authorized, false otherwise.
     */
    private boolean isAuthorizedToDeleteTrainerVideoAlbum(TrainerVideoAlbum trainerVideoAlbum) {
        User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
        boolean authorizedUser = user.getEmail().equalsIgnoreCase(trainerVideoAlbum.getTrainer().getPartner().getUser().getEmail());
        return jwtFilter.isAdmin() || authorizedUser;
    }

    /**
     * Checks if the current user is authorized to delete a TrainerFeatureVideo.
     *
     * @param trainerFeatureVideo The TrainerFeatureVideo to be checked for authorization.
     * @return True if the user is authorized, false otherwise.
     */
    private boolean isAuthorizedToDeleteTrainerFeatureVideo(TrainerFeatureVideo trainerFeatureVideo) {
        User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
        boolean authorizedUser = user.getEmail().equalsIgnoreCase(trainerFeatureVideo.getTrainer().getPartner().getUser().getEmail());
        return jwtFilter.isAdmin() || authorizedUser;
    }

    /**
     * Checks if the Trainer's status can be updated based on certain conditions.
     *
     * @param trainer The Trainer object to be checked.
     * @param status  The new status to be set ("true" for active, "false" for inactive).
     * @return A message indicating whether the Trainer's status can be updated or providing reasons if not.
     */
    private String canUpdateTrainerStatus(Trainer trainer, String status) {
        if (status.equalsIgnoreCase("true")) {
            if (isTrainerBenefitsInvalid(trainer)) {
                return "Cannot update status, Trainer must have at least 5 benefits.";
            }

            if (isTrainerIntroductionInvalid(trainer)) {
                return "Cannot update status, Trainer must have an introduction.";
            }

            if (isTrainerPhotoAlbumInvalid(trainer)) {
                return "Cannot update status, Trainer must have at least 5 photos in the photo album.";
            }

            if (isTrainerVideoAlbumInvalid(trainer)) {
                return "Cannot update status, Trainer must have at least 5 videos in the video album.";
            }

            if (isTrainerPricingInvalid(trainer)) {
                return "Cannot update status, Trainer must have pricing information.";
            }

            if (isTrainerFeatureVideoInvalid(trainer)) {
                return "Cannot update status, Trainer must have featured videos.";
            }
        }

        // If all conditions are met, return a success message
        return "Conditions met for updating status.";
    }

    /**
     * Checks if the Trainer's benefits are invalid.
     *
     * @param trainer The Trainer object to be checked.
     * @return True if the Trainer has less than 5 benefits or no benefits; false otherwise.
     */
    private boolean isTrainerBenefitsInvalid(Trainer trainer) {
        List<TrainerBenefit> trainerBenefits = trainerBenefitRepo.findByTrainer(trainer);
        return trainerBenefits.isEmpty() || trainerBenefits.size() < 5;
    }

    /**
     * Checks if the Trainer's introduction is invalid.
     *
     * @param trainer The Trainer object to be checked.
     * @return True if the Trainer has no introduction; false otherwise.
     */
    private boolean isTrainerIntroductionInvalid(Trainer trainer) {
        TrainerIntroduction trainerIntroduction = trainerIntroductionRepo.findByTrainer(trainer);
        return trainerIntroduction == null;
    }

    /**
     * Checks if the Trainer's photo album is invalid.
     *
     * @param trainer The Trainer object to be checked.
     * @return True if the Trainer has less than 5 photos in the photo album or no album; false otherwise.
     */
    private boolean isTrainerPhotoAlbumInvalid(Trainer trainer) {
        List<TrainerPhotoAlbum> trainerPhotoAlbums = trainerPhotoAlbumRepo.findByTrainer(trainer);
        return trainerPhotoAlbums.isEmpty() || trainerPhotoAlbums.size() < 5;
    }

    /**
     * Checks if the Trainer's video album is invalid.
     *
     * @param trainer The Trainer object to be checked.
     * @return True if the Trainer has less than 5 videos in the video album or no album; false otherwise.
     */
    private boolean isTrainerVideoAlbumInvalid(Trainer trainer) {
        List<TrainerVideoAlbum> trainerVideoAlbums = trainerVideoAlbumRepo.findByTrainer(trainer);
        return trainerVideoAlbums.isEmpty() || trainerVideoAlbums.size() < 5;
    }

    /**
     * Checks if the Trainer's pricing information is invalid.
     *
     * @param trainer The Trainer object to be checked.
     * @return True if the Trainer has no pricing information; false otherwise.
     */
    private boolean isTrainerPricingInvalid(Trainer trainer) {
        TrainerPricing trainerPricing = trainerPricingRepo.findByTrainer(trainer);
        return trainerPricing == null;
    }

    /**
     * Checks if the Trainer's featured videos are invalid.
     *
     * @param trainer The Trainer object to be checked.
     * @return True if the Trainer has no featured videos; false otherwise.
     */
    private boolean isTrainerFeatureVideoInvalid(Trainer trainer) {
        List<TrainerFeatureVideo> trainerFeatureVideos = trainerFeatureVideoRepo.findByTrainer(trainer);
        return trainerFeatureVideos.isEmpty();
    }

}

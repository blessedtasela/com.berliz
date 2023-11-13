package com.berliz.serviceImplement;

import com.berliz.DTO.CenterRequest;
import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.*;
import com.berliz.repository.*;
import com.berliz.services.CenterService;
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
import java.util.*;

@Slf4j
@Service
public class CenterServiceImplement implements CenterService {

    @Autowired
    JWTFilter jwtFilter;

    @Autowired
    CenterRepo centerRepo;

    @Autowired
    PartnerRepo partnerRepo;

    @Autowired
    TrainerRepo trainerRepo;

    @Autowired
    CategoryRepo categoryRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    CenterLikeRepo centerLikeRepo;

    @Autowired
    EmailUtilities emailUtilities;

    @Autowired
    FileUtilities fileUtilities;

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    /**
     * adds a center based on data provided
     *
     * @param centerRequest The request body containing data
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<String> addCenter(CenterRequest centerRequest) throws JsonProcessingException {
        try {
            log.info("Inside addCenter {}", centerRequest);
            boolean validRequest = centerRequest != null;
            log.info("Is request valid? {}", validRequest);

            if (!validRequest) {
                return BerlizUtilities.getResponseEntity(BerlizConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
            if (jwtFilter.isAdmin()) {
                return handleCenterAdditionByAdmin(centerRequest);
            } else {
                return handleCenterAdditionByUser(centerRequest);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }


    /**
     * Updates a center based on data provided
     *
     * @param requestMap The request body containing data
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<String> updateCenter(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside updateCenter {}", requestMap);
            boolean isValid;
            if (jwtFilter.isAdmin()) {
                isValid = validateCenterFromMap(requestMap, true);
            } else {
                isValid = validateCenterFromMap(requestMap, false);
            }

            Center validCenter = centerRepo.findByCenterId(Integer.valueOf(requestMap.get("id")));
            log.info("Is request valid? {}", isValid);

            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            if (validCenter == null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Center id not found");
            }

            if (validCenter.getStatus().equalsIgnoreCase("false")) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Cannot make update yet. Account is inactive");
            }

            if (!(jwtFilter.isAdmin() || (jwtFilter.isCenter()
                    && jwtFilter.getCurrentUserId().equals(validCenter
                    .getPartner().getUser().getId())))) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            centerRepo.save(updateCenterFromMap(requestMap));
            if (jwtFilter.isAdmin()) {
                return BerlizUtilities.buildResponse(HttpStatus.OK, "Center information updated successfully");
            } else {
                return BerlizUtilities.buildResponse(HttpStatus.OK, "Hello " + validCenter.getName()
                        + ", your center information has been updated successfully");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }


    /**
     * Returns a list of centers
     *
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<List<Center>> getAllCenters() {
        try {
            log.info("Inside getAllCenters");
            if (jwtFilter.isAdmin()) {
                List<Center> centers = centerRepo.findAll();
                return new ResponseEntity<>(centers, HttpStatus.OK);
            } else {
                return new ResponseEntity(BerlizConstants.UNAUTHORIZED_REQUEST, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Deletes a center based on the provided center ID.
     *
     * @param id The ID of the center to be deleted
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<String> deleteCenter(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside deleteCenter {}", id);
            User user = userRepo.findByEmail(jwtFilter.getCurrentUser());
            boolean authorizedUser = user.getEmail().equalsIgnoreCase(BerlizConstants.BERLIZ_SUPER_ADMIN);
            if (!(jwtFilter.isAdmin() && authorizedUser)) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            Center center = centerRepo.findByCenterId(id);
            if (center == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Center id not found");
            }

            centerRepo.delete(center);
            simpMessagingTemplate.convertAndSend("/topic/deleteCenter", center);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Center deleted successfully");
        } catch (Exception ex) {
            log.error("Something went wrong while performing operation", ex);
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Updates a center status based on the provided center ID.
     *
     * @param id The ID of the center status to be updated
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside updateStatus {}", id);
            Integer validUserId = jwtFilter.getCurrentUserId();
            Optional<Center> optional = centerRepo.findById(id);

            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Center id not found");
            }

            log.info("Inside optional {}", optional);
            Integer userId = optional.get().getPartner().getUser().getId();
            String status = optional.get().getStatus();
            String userEmail = optional.get().getPartner().getUser().getEmail();
            Center center = optional.get();
            boolean validUser = jwtFilter.isAdmin() || (validUserId.equals(userId));

            if (!validUser) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED,
                        BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            status = status.equalsIgnoreCase("true") ? "false" : "true";
            center.setStatus(status);
            centerRepo.save(center);

            emailUtilities.sendStatusMailToAdmins(status, userEmail,
                    userRepo.getAllAdminsMail(), "Center");
            emailUtilities.sendStatusMailToUser(status, "Center", userEmail);

            String responseMessage;
            if (jwtFilter.isAdmin()) {
                responseMessage = status.equalsIgnoreCase("true") ?
                        "Center has been activated successfully" :
                        "Center deactivated successfully";
            } else {
                responseMessage = status.equalsIgnoreCase("true") ?
                        "Hello " + userEmail + ", your Center account has successfully been activated" :
                        "Hello " + userEmail + ", your Center account has been deactivated";
            }
            simpMessagingTemplate.convertAndSend("/topic/updateCenterStatus", center);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Gets a center based on the provided center ID.
     *
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<Center> getCenter() {
        try {
            log.info("Inside getCenter");
            Integer userId = jwtFilter.getCurrentUserId();
            Partner partner = partnerRepo.findByUserId(userId);
            Center center = centerRepo.findByPartnerId(partner.getId());

            if (partner == null) {
                return new ResponseEntity<>(new Center(), HttpStatus.NOT_FOUND);
            }

            if (center == null || center.getId() == null) {
                log.error("Center or its ID is null. Check database.");
                return new ResponseEntity("Center is null", HttpStatus.NOT_FOUND);
            }

            // Check if the partner and the Center matches
            Integer currentUser = partner.getUser().getId();
            boolean validUser = currentUser.equals(userId)
                    && center.getPartner().getId().equals(partner.getId());
            if (validUser || jwtFilter.isAdmin()) {
                return new ResponseEntity<>(center, HttpStatus.OK);
            }
            return new ResponseEntity<>(new Center(), HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Center(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Gets a center based on the provided user ID.
     *
     * @param id The ID of the user related to the center to be fetched
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<Center> getByUserId(Integer id) {
        try {
            log.info("Inside getByUserId");
            Partner partner = partnerRepo.findByUserId(id);
            if (partner == null) {
                return new ResponseEntity<>(new Center(), HttpStatus.BAD_REQUEST);
            }

            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new Center(), HttpStatus.UNAUTHORIZED);
            }
            Center center = centerRepo.findByPartnerId(partner.getId());

            if (center == null) {
                return new ResponseEntity<>(new Center(), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(center, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Center(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Like a center.
     *
     * @param id The ID of the center to be liked
     * @return ResponseEntity with a success message or an error message
     */

    @Override
    public ResponseEntity<String> likeCenter(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside likeCenter {}", id);
            Center center = centerRepo.findByCenterId(id);
            User user = userRepo.findByEmail(jwtFilter.getCurrentUser());
            boolean validUser = jwtFilter.isBerlizUser();

            if (!validUser) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (center == null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Center id not found");
            }

            boolean hasLiked = centerLikeRepo.existsByUserAndCenter(user, center);
            String responseMessage;
            if (hasLiked) {
                // dislike center
                centerLikeRepo.deleteByUserAndCenter(user, center);
                center.setLikes(center.getLikes() - 1);
                responseMessage = "Hello, " + user.getFirstname() + " you have disliked " + center.getName();
            } else {
                // like center
                CenterLike centerLike = new CenterLike();
                centerLike.setUser(user);
                centerLike.setCenter(center);
                centerLike.setDate(new Date());
                centerLikeRepo.save(centerLike);

                center.setLikes(center.getLikes() + 1);
                responseMessage = "Hello, " + user.getFirstname() + " you just liked " + center.getName();
            }

            centerRepo.save(center);
            simpMessagingTemplate.convertAndSend("/topic/updateCenterStatus", center);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Retrieves a list of all center likes.
     *
     * @return ResponseEntity containing the list of center likes.
     */
    @Override
    public ResponseEntity<List<CenterLike>> getCenterLikes() {
        try {
            log.info("Inside getCenterLikes {}");
            List<CenterLike> centerLikes = centerLikeRepo.findAll();
            return new ResponseEntity<>(centerLikes, HttpStatus.OK);
        } catch (Exception ex) {
            log.error("Something went wrong while performing operation", ex);
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updatePhoto(CenterRequest centerRequest) throws JsonProcessingException {
        try {
            log.info("Inside updatePhoto{}", centerRequest);
            Integer centerId = centerRequest.getId();
            Center center = centerRepo.findByCenterId(centerId);

            if (center == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Center id not found");
            }

            User user = center.getPartner().getUser();
            boolean validUser = jwtFilter.isAdmin() || jwtFilter.getCurrentUserId().equals(user.getId());

            if (!validUser) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            MultipartFile file = centerRequest.getPhoto();
            if (file == null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "No profile photo provided");
            }

            if (!fileUtilities.isValidImageType(file)) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Invalid file type");
            }

            if (!fileUtilities.isValidImageSize(file)) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Invalid file type");
            }
            String responseMessage;
            center.setPhoto(file.getBytes());
            centerRepo.save(center);
            if (jwtFilter.isAdmin()) {
                responseMessage = center.getName() + "'s center photo updated successfully";
            } else {
                responseMessage = "Hello " + center.getName() + ", your center photo has been updated successfully";
            }
            simpMessagingTemplate.convertAndSend("/topic/updateCenterPhoto", center);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<Center>> getActiveCenters() {
        try {
            log.info("Inside getActiveTrainers");
            List<Center> centers = centerRepo.getActiveCenters();
            return new ResponseEntity<>(centers, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    /**
     * Validates the data in the request map for center creation or update.
     *
     * @param requestMap The map containing the request data
     * @return True if the request data is valid, otherwise false
     */
    private boolean validateCenterFromMap(Map<String, String> requestMap, boolean admin) {
        if (admin) {
            return requestMap.containsKey("id")
                    && requestMap.containsKey("name")
                    && requestMap.containsKey("motto")
                    && requestMap.containsKey("address")
                    && requestMap.containsKey("experience")
                    && requestMap.containsKey("location")
                    && requestMap.containsKey("likes")
                    && requestMap.containsKey("categoryIds");
        } else {
            return requestMap.containsKey("id")
                    && requestMap.containsKey("name")
                    && requestMap.containsKey("motto")
                    && requestMap.containsKey("address")
                    && requestMap.containsKey("experience")
                    && requestMap.containsKey("location")
                    && requestMap.containsKey("categoryIds");
        }
    }

    /**
     * Constructs a Center object from the provided request map and saves it to the repository.
     *
     * @param centerRequest The class containing the request data
     * @return The constructed and saved Center object
     */
    private Center getCenterFromMap(CenterRequest centerRequest) throws IOException {
        Center center = new Center();
        User user;
        Partner partner = partnerRepo.findByPartnerId(centerRequest.getPartnerId());
        if (jwtFilter.isAdmin()) {
            String userEmail = partner.getUser().getEmail();
            user = userRepo.findByEmail(userEmail);
        } else {
            user = userRepo.findByEmail(jwtFilter.getCurrentUser());
        }
        user.setRole("center");
        userRepo.save(user);

        byte[] photo = centerRequest.getPhoto().getBytes();

        // Parse tagIds as a comma-separated string
        String categoryIdsString = centerRequest.getCategoryIds();
        String[] categoryIdsArray = categoryIdsString.split(",");

        Set<Category> categorySet = new HashSet<>();
        for (String categoryIdString : categoryIdsArray) {
            int categoryId = Integer.parseInt(categoryIdString.trim());

            Category category = new Category();
            category.setId(categoryId);
            categorySet.add(category);
        }

        center.setCategorySet(categorySet);
        center.setPartner(partner);
        center.setName(centerRequest.getName());
        center.setMotto(centerRequest.getMotto());
        center.setLocation(centerRequest.getLocation());
        center.setAddress(centerRequest.getAddress());
        center.setExperience(centerRequest.getExperience());
        center.setPhoto(photo);
        center.setLikes(0);
        center.setDate(new Date());
        center.setLastUpdate(new Date());
        center.setStatus("false"); // Initializing status

        simpMessagingTemplate.convertAndSend("/topic/getCenterFromMap", center);
        return center;
    }

    /**
     * Updates a Center object from the provided request map and saves it to the repository.
     *
     * @param requestMap The map containing the request data
     * @return The constructed and saved Center object
     */
    private Center updateCenterFromMap(Map<String, String> requestMap) {
        Optional<Center> optional = centerRepo.findById(Integer.valueOf(requestMap.get("id")));
        Center existingCenter = optional.get();

        // Parse categoryIds as a comma-separated string
        String categoryIdsString = requestMap.get("categoryIds");
        String[] categoryIdsArray = categoryIdsString.split(",");

        Set<Category> categorySet = new HashSet<>();
        for (String categoryIdString : categoryIdsArray) {
            int categoryId = Integer.parseInt(categoryIdString.trim());
            Optional<Category> optionalCategory = categoryRepo.findById(categoryId);
            if (optionalCategory.isEmpty()) {
                log.error("Category with ID " + categoryId + " not found");
            }
            categorySet.add(optionalCategory.get());
        }

        existingCenter.setCategorySet(categorySet);
        existingCenter.setName(requestMap.get("name"));
        existingCenter.setMotto(requestMap.get("motto"));
        existingCenter.setAddress(requestMap.get("address"));
        existingCenter.setLocation(requestMap.get("location"));
        existingCenter.setExperience(requestMap.get("experience"));
        if (jwtFilter.isAdmin()) {
            existingCenter.setLikes(Integer.parseInt(requestMap.get("likes")));
        }
        existingCenter.setLastUpdate(new Date());
        simpMessagingTemplate.convertAndSend("/topic/updateCenter", existingCenter);
        return existingCenter;
    }

    /**
     * Handle use case of adding center by an admin.
     *
     * @param centerRequest The map containing the request data
     * @return The constructed and saved Center object
     */
    private ResponseEntity<String> handleCenterAdditionByAdmin(CenterRequest centerRequest) throws
            JsonProcessingException {
        try {
            log.info("Handling Center addition by admin");
            Integer partnerId = centerRequest.getPartnerId();
            Center center = centerRepo.findByPartnerId(partnerId);
            Partner partner = partnerRepo.findByPartnerId(partnerId);

            if (partnerId == null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Admin must provide partnerId");
            }

            if (partner == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Partner id not found");
            }

            if (center != null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Center already exists");
            }

            if (!isValidRole(partnerId, "center")) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Invalid partner role. Partner must be a center");
            }

            if (!isApprovedCenterPartner(partnerId)) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, "Partner application hasn't been approved yet");
            }

            if (isCenterNameAlreadyExists(centerRequest.getName())) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Center name is already taken. Please choose another name");
            }

            centerRepo.save(getCenterFromMap(centerRequest));
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Center added successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }


    /**
     * Handle use case of adding center by a valid user.
     *
     * @param centerRequest The map containing the request data
     * @return The constructed and saved Center object
     */
    private ResponseEntity<String> handleCenterAdditionByUser(CenterRequest centerRequest) {
        try {
            log.info("Handling Trainer addition by user");
            Integer userId = jwtFilter.getCurrentUserId();
            Partner partner = partnerRepo.findByUserId(userId);
            Integer partnerId = partner.getId();
            Center center = centerRepo.findByPartnerId(partnerId);
            if (partner == null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Partner id not found");
            }


            if (center != null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Center already exists");
            }

            if (!isValidRole(partnerId, "center")) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Invalid partner role. Partner must be a center");
            }

            if (!isApprovedCenterPartner(partnerId)) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Your partnership application is under review, please wait for admin approval");
            }

            if (isCenterNameAlreadyExists(centerRequest.getName())) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Center name is already taken. Please choose another name");
            }

            centerRepo.save(getCenterFromMap(centerRequest));
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Your Trainer account has successfully been created");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Validates partner.
     *
     * @param partnerId ID of the partner to be validated
     * @return The valid partner
     */
    private boolean isValidPartner(Integer partnerId) {
        Partner partner = partnerRepo.findByPartnerId(partnerId);
        return partner != null;
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
     * Validates if center is an approved partner.
     *
     * @param partnerId ID of the partner to be approved
     * @return The valid partner
     */
    private boolean isApprovedCenterPartner(Integer partnerId) {
        Partner partner = partnerRepo.findByPartnerId(partnerId);
        return partner != null && partner.getStatus().equalsIgnoreCase("true");
    }

    /**
     * Checks if a user is linked to a center.
     *
     * @param userId ID of the center to be checked
     * @return The valid partner
     */
    private boolean isUserAssociatedWithCenter(Integer userId) {
        Partner currentUserPartner = partnerRepo.findByUserId(userId);
        return Optional.ofNullable(currentUserPartner).map(Partner::getUser).map(User::getId).isPresent();
    }

    /**
     * Checks if a center name already exists.
     *
     * @param centerName name of center to be checked
     * @return The valid partner
     */
    private boolean isCenterNameAlreadyExists(String centerName) {
        Center centerByName = centerRepo.findByName(centerName);
        return centerByName != null;
    }
}

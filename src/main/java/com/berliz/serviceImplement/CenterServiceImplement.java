package com.berliz.serviceImplement;

import com.berliz.DTO.*;
import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.*;
import com.berliz.repositories.*;
import com.berliz.services.CenterService;
import com.berliz.utils.BerlizUtilities;
import com.berliz.utils.EmailUtilities;
import com.berliz.utils.FileUtilities;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityNotFoundException;
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
import java.text.MessageFormat;
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
    CenterVideoAlbumRepo centerVideoAlbumRepo;

    @Autowired
    CenterPhotoAlbumRepo centerPhotoAlbumRepo;

    @Autowired
    CenterTrainerRepo centerTrainerRepo;

    @Autowired
    CenterLocationRepo centerLocationRepo;

    @Autowired
    CenterPricingRepo centerPricingRepo;

    @Autowired
    CenterIntroductionRepo centerIntroductionRepo;

    @Autowired
    CenterAnnouncementRepo centerAnnouncementRepo;

    @Autowired
    CenterEquipmentRepo centerEquipmentRepo;

    @Autowired
    CenterLikeRepo centerLikeRepo;

    @Autowired
    MemberRepo memberRepo;

    @Autowired
    CenterReviewRepo centerReviewRepo;

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

            User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
            centerRepo.save(updateCenterFromMap(requestMap, validCenter, user));
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

    @Override
    public ResponseEntity<String> updateMyCenterTrainers(Map<String, String> requestMap) {
        return null;
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
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
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
            User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
            boolean authorizedUser = user.getEmail().equalsIgnoreCase(BerlizConstants.BERLIZ_SUPER_ADMIN);
            if (!(jwtFilter.isAdmin() && authorizedUser)) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            Center center = centerRepo.findByCenterId(id);
            if (center == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Center id not found");
            }

            centerRepo.delete(center);
            String adminNotificationMessage = "Center with id: " + center.getId() + ", and name " + center.getName() +
                    ", account has been deleted";
            String notificationMessage = "You have successfully deleted your center your account : " + center.getName();
            jwtFilter.sendNotifications("/topic/deleteCenter", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, center);
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
            Center savedCenter = centerRepo.save(center);
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

            String adminNotificationMessage = "Center with id: " + savedCenter.getId() +
                    ", status has been set to " + savedCenter.getStatus();
            String notificationMessage = "You have successfully set your center status to : " +
                    savedCenter.getStatus();
            jwtFilter.sendNotifications("/topic/updateCenterStatus", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, savedCenter);
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
            if (partner == null) {
                return new ResponseEntity<>(new Center(), HttpStatus.NOT_FOUND);
            }
            Center center = centerRepo.findByPartnerId(partner.getId());
            if (center == null || center.getId() == null) {
                log.error("Center or its ID is null. Check database.");
                return new ResponseEntity<>(new Center(), HttpStatus.NOT_FOUND);
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
            User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
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
                String adminNotificationMessage = "Center with id: " + center.getId() +
                        ", and name: " + center.getName() + " has just been disliked by: " + user.getEmail();
                String notificationMessage = "You have successfully disliked center : " + center.getName();
                jwtFilter.sendNotifications("/topic/likeCenter", adminNotificationMessage,
                        jwtFilter.getCurrentUser(), notificationMessage, center);
            } else {
                // like center
                CenterLike centerLike = new CenterLike();
                centerLike.setUser(user);
                centerLike.setCenter(center);
                centerLike.setDate(new Date());
                centerLikeRepo.save(centerLike);
                center.setLikes(center.getLikes() + 1);
                responseMessage = "Hello, " + user.getFirstname() + " you just liked " + center.getName();
                String adminNotificationMessage = "Center with id: " + center.getId() +
                        ", and name: " + center.getName() + " has just been liked by: " + user.getEmail();
                String notificationMessage = "You have successfully liked center : " + center.getName();
                jwtFilter.sendNotifications("/topic/likeCenter", adminNotificationMessage,
                        jwtFilter.getCurrentUser(), notificationMessage, center);
            }

            centerRepo.save(center);
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
            log.info("Inside getCenterLikes");
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

            String adminNotificationMessage = "Center with id: " + center.getId() + ", and name: "
                    + center.getName() + ", cover photo has been changed";
            String notificationMessage = "You have changed your center cover photo : " + center.getName();
            jwtFilter.sendNotifications("/topic/updateCenterPhoto", adminNotificationMessage,
                    user, notificationMessage, center);
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

    @Override
    public ResponseEntity<String> addCenterAnnouncement(AnnouncementRequest announcementRequest) throws JsonProcessingException {
        try {
            log.info("Inside addCenterAnnouncement {}", announcementRequest);
            boolean isValid = announcementRequest != null;
            log.info("Is request valid? {}", isValid);

            if (!(jwtFilter.isAdmin() || jwtFilter.isCenter())) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            Center center = centerRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (jwtFilter.isAdmin()) {
                if (announcementRequest.getCenterId() == null) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Admin must provide center Id");
                }

                center = centerRepo.findByCenterId(announcementRequest.getCenterId());
            }

            if (center == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Center not found in db");
            }

            getCenterAnnouncementFromMap(announcementRequest, center);
            String responseMessage = jwtFilter.isAdmin() ?
                    center.getName() + "'s, center announcement have successfully been added to their list" :
                    "Hello " + center.getName() + ", you have successfully added an announcement to your center profile ";

            // check if all trainer entities are added and set the trainer status to true
            String resultMessage = canUpdateCenterStatus(center, center.getStatus());
            if (resultMessage.startsWith("Conditions")) {
                center.setStatus("true");
                centerRepo.save(center);
                responseMessage = jwtFilter.isAdmin() ?
                        center.getName() + "'s, center announcement have successfully been added to their list" +
                                " and their center account has been activated successfully" :
                        "Hello " + center.getName() + ", you have successfully added an announcement to your center profile " +
                                " and your center account has been successfully activated";
            }

            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateCenterAnnouncement(AnnouncementRequest announcementRequest) throws JsonProcessingException {
        try {
            log.info("Inside updateCenterAnnouncement {}", announcementRequest);
            Integer userId = jwtFilter.getCurrentUserId();
            boolean isValid = announcementRequest != null;
            log.info("Is request valid? {}", isValid);

            if (!jwtFilter.isAdmin() || !jwtFilter.isCenter()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid || announcementRequest.getId() == null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            CenterAnnouncement centerAnnouncement = centerAnnouncementRepo.findById(announcementRequest.getId()).orElse(null);
            if (centerAnnouncement == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Center announcement not found in db");
            }

            boolean validUser = jwtFilter.isAdmin() ||
                    (jwtFilter.isCenter() && userId.equals(centerAnnouncement
                            .getCenter().getPartner().getUser().getId()));
            if (!validUser) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
            Center center = centerAnnouncement.getCenter();
            centerAnnouncement.setAnnouncement(announcementRequest.getAnnouncement());
            byte[] icon = announcementRequest.getIcon().getBytes();
            centerAnnouncement.setIcon(icon);
            centerAnnouncement.setLastUpdate(new Date());
            CenterAnnouncement savedCenterAnnouncement = centerAnnouncementRepo.save(centerAnnouncement);
            String responseMessage = jwtFilter.isAdmin() ?
                    center.getName() + "!, announcement have successfully been updated in their album" :
                    "Hello " + center.getName() + "!, you have successfully updated your center's announcement";
            String adminNotificationMessage = "Center announcement with id: " + centerAnnouncement.getId() + ", and info: "
                    + centerAnnouncement.getAnnouncement() + "/ has been updated";
            String notificationMessage = "You have update your center announcement for : " + centerAnnouncement.getAnnouncement();
            jwtFilter.sendNotifications("/topic/updateCenterAnnouncement", adminNotificationMessage,
                    user, notificationMessage, savedCenterAnnouncement);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteCenterAnnouncement(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside deleteCenterAnnouncement {}", id);
            Optional<CenterAnnouncement> optional = centerAnnouncementRepo.findById(id);
            CenterAnnouncement centerAnnouncement = optional.orElse(null);

            if (centerAnnouncement == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Center announcement id not found");
            }

            if (!isAuthorizedToDeleteCenterAnnouncement(centerAnnouncement)) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (centerAnnouncement.getCenter().getStatus().equalsIgnoreCase("false")) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Center is inactive, Cannot complete request");
            }

            centerAnnouncementRepo.delete(centerAnnouncement);
            String adminNotificationMessage = "Center announcement with id: " + centerAnnouncement.getId()
                    + ", and info " + centerAnnouncement.getAnnouncement() + ", has been deleted";
            String notificationMessage = "You have successfully deleted your center announcement : "
                    + centerAnnouncement.getAnnouncement();
            jwtFilter.sendNotifications("/topic/deleteCenterAnnouncement", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, centerAnnouncement);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Center announcement deleted from profile successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateCenterAnnouncementStatus(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside updateCenterAnnouncementStatus {}", id);
            String status;
            if (!jwtFilter.isAdmin()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            Optional<CenterAnnouncement> optional = centerAnnouncementRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Center announcement ID not found");
            }

            log.info("Inside optional {}", optional.get());
            status = optional.get().getStatus();
            CenterAnnouncement centerAnnouncement = optional.get();
            String responseMessage;
            if (status.equalsIgnoreCase("true")) {
                status = "false";
                responseMessage = "Center announcement status updated successfully. Now deactivated";
            } else {
                status = "true";
                responseMessage = "Center announcement status updated successfully. Now activated";
            }

            centerAnnouncement.setStatus(status);
            CenterAnnouncement savedCenterAnnouncement = centerAnnouncementRepo.save(centerAnnouncement);
            String adminNotificationMessage = "Center announcement with id: " + savedCenterAnnouncement.getId() +
                    ", status has been set to " + savedCenterAnnouncement.getStatus();
            String notificationMessage = "You have successfully set your center announcement status to : " +
                    savedCenterAnnouncement.getStatus();
            jwtFilter.sendNotifications("/topic/updateCenterAnnouncementStatus", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, savedCenterAnnouncement);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<CenterAnnouncement>> getAllCenterAnnouncements() {
        try {
            log.info("Inside getAllCenterAnnouncements");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<CenterAnnouncement> centerAnnouncements = centerAnnouncementRepo.findAll();
            return new ResponseEntity<>(centerAnnouncements, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<CenterAnnouncement>> getMyCenterAnnouncements() {
        try {
            log.info("Inside getMyCenterAnnouncements");
            if (!(jwtFilter.isAdmin()) || jwtFilter.isCenter()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            Center center = centerRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (center == null) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            List<CenterAnnouncement> centerAnnouncements = centerAnnouncementRepo.findByCenter(center);
            return new ResponseEntity<>(centerAnnouncements, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<CenterAnnouncement>> getActiveCenterAnnouncements(Integer id) {
        try {
            log.info("Inside getActiveCenterAnnouncements");
            Center center = centerRepo.findByCenterId(id);
            if (center == null) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            List<CenterAnnouncement> centerAnnouncements = centerAnnouncementRepo.getActiveCenterAnnouncements(center);
            return new ResponseEntity<>(centerAnnouncements, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> addCenterEquipment(EquipmentRequest equipmentRequest) throws JsonProcessingException {
        try {
            log.info("Inside addCenterEquipment {}", equipmentRequest);
            boolean isValid = equipmentRequest.isValidRequest(false);
            log.info("Is request valid? {}", isValid);

            if (!(jwtFilter.isAdmin() || jwtFilter.isCenter())) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            Center center = centerRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (jwtFilter.isAdmin()) {
                if (equipmentRequest.getCenterId() == null) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Admin must provide center Id");
                }

                center = centerRepo.findByCenterId(equipmentRequest.getCenterId());
            }

            if (center == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Center not found in db");
            }

            CenterEquipment centerEquipment = centerEquipmentRepo.findByName(equipmentRequest.getName());
            if (centerEquipment != null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Equipment exists already");
            }

            getCenterEquipmentFromMap(equipmentRequest, center);
            String responseMessage = jwtFilter.isAdmin() ?
                    center.getName() + "'s, center announcement have successfully been added to their list" :
                    "Hello " + center.getName() + ", you have successfully added an announcement to your center profile ";

            // check if all trainer entities are added and set the trainer status to true
            String resultMessage = canUpdateCenterStatus(center, "true");
            if (resultMessage.startsWith("Conditions")) {
                center.setStatus("true");
                centerRepo.save(center);
                responseMessage = jwtFilter.isAdmin() ?
                        center.getName() + "'s, center announcement have successfully been added to their list" +
                                " and their center account has been activated successfully" :
                        "Hello " + center.getName() + ", you have successfully added an announcement to your center profile " +
                                " and your center account has been successfully activated";
            }

            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateCenterEquipment(EquipmentRequest equipmentRequest) throws JsonProcessingException {
        try {
            log.info("Inside updateCenterEquipment {}", equipmentRequest);
            Integer userId = jwtFilter.getCurrentUserId();
            boolean isValid = equipmentRequest.isValidRequest(true);
            log.info("Is request valid? {}", isValid);

            if (!jwtFilter.isAdmin() || !jwtFilter.isCenter()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid || equipmentRequest.getId() == null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            CenterEquipment centerEquipment = centerEquipmentRepo.findById(equipmentRequest.getId()).orElse(null);
            if (centerEquipment == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Center announcement not found in db");
            }

            boolean validUser = jwtFilter.isAdmin() ||
                    (jwtFilter.isCenter() && userId.equals(centerEquipment
                            .getCenter().getPartner().getUser().getId()));
            if (!validUser) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
            Center center = centerEquipment.getCenter();
            CenterEquipment savedCenterEquipment = centerEquipmentRepo.save(updateCenterEquipmentFromMap(centerEquipment, equipmentRequest));
            String responseMessage = jwtFilter.isAdmin() ?
                    center.getName() + "!, equipment have successfully been updated in their profile" :
                    "Hello " + center.getName() + "!, you have successfully updated your center's equipment";
            String adminNotificationMessage = "Center equipment with id: " + savedCenterEquipment.getId()
                    + ", and equipment name: " + savedCenterEquipment.getName() + " has been updated";
            String notificationMessage = "You have updated your center equipment for : "
                    + savedCenterEquipment.getName();
            jwtFilter.sendNotifications("/topic/updateCenterEquipment", adminNotificationMessage,
                    user, notificationMessage, savedCenterEquipment);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteCenterEquipment(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside deleteCenterEquipment {}", id);
            Optional<CenterEquipment> optional = centerEquipmentRepo.findById(id);
            CenterEquipment centerEquipment = optional.orElse(null);

            if (centerEquipment == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Center equipment id not found");
            }

            if (!isAuthorizedToDeleteCenterEquipment(centerEquipment)) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (centerEquipment.getCenter().getStatus().equalsIgnoreCase("false")) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Center is inactive, Cannot complete request");
            }

            centerEquipmentRepo.delete(centerEquipment);
            String adminNotificationMessage = "Center equipment with id: " + centerEquipment.getId()
                    + ", and info " + centerEquipment.getName() + ", has been deleted";
            String notificationMessage = "You have successfully deleted your center equipment : "
                    + centerEquipment.getName();
            jwtFilter.sendNotifications("/topic/deleteCenterEquipment", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, centerEquipment);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Center equipment deleted from profile successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<CenterEquipment>> getAllCenterEquipments() {
        try {
            log.info("Inside getAllCenterEquipments");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<CenterEquipment> centerEquipments = centerEquipmentRepo.findAll();
            return new ResponseEntity<>(centerEquipments, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<CenterEquipment>> getMyCenterEquipments() {
        try {
            log.info("Inside getMyCenterEquipments");
            if (!(jwtFilter.isAdmin()) || jwtFilter.isCenter()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            Center center = centerRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (center == null) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            List<CenterEquipment> centerEquipments = centerEquipmentRepo.findByCenter(center);
            return new ResponseEntity<>(centerEquipments, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> addCenterIntroduction(IntroductionRequest introductionRequest) throws JsonProcessingException {
        try {
            log.info("Inside addCenterIntroduction {}", introductionRequest);
            boolean isValid = introductionRequest.isValidRequest(false);
            log.info("Is request valid? {}", isValid);

            if (!(jwtFilter.isAdmin() || jwtFilter.isCenter())) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            Center center = centerRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (jwtFilter.isAdmin()) {
                if (introductionRequest.getCenterId() == null) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Admin must provide center Id");
                }

                center = centerRepo.findByCenterId(introductionRequest.getCenterId());
            }

            if (center == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Center not found in db");
            }

            List<CenterIntroduction> centerIntroduction = centerIntroductionRepo.findByCenter(center);
            if (centerIntroduction.size() > 3) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "3 Introduction exists for " +
                        center.getName() + " already");
            }

            getCenterIntroductionFromMap(introductionRequest, center);
            String responseMessage = jwtFilter.isAdmin() ?
                    center.getName() + "'s, center introduction have successfully been added to their list" :
                    "Hello " + center.getName() + ", you have successfully added an introduction to your center profile ";

            // check if all trainer entities are added and set the trainer status to true
            String resultMessage = canUpdateCenterStatus(center, "true");
            if (resultMessage.startsWith("Conditions")) {
                center.setStatus("true");
                centerRepo.save(center);
                responseMessage = jwtFilter.isAdmin() ?
                        center.getName() + "'s, center introduction have successfully been added to their list" +
                                " and their center account has been activated successfully" :
                        "Hello " + center.getName() + ", you have successfully added an introduction to your center profile " +
                                " and your center account has been successfully activated";
            }

            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateCenterIntroduction(IntroductionRequest introductionRequest) throws JsonProcessingException {
        try {
            log.info("Inside updateCenterIntroduction {}", introductionRequest);
            Integer userId = jwtFilter.getCurrentUserId();
            boolean isValid = introductionRequest.isValidRequest(true);
            log.info("Is request valid? {}", isValid);

            if (!jwtFilter.isAdmin() || !jwtFilter.isCenter()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid || introductionRequest.getId() == null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            CenterIntroduction centerIntroduction = centerIntroductionRepo.findById(introductionRequest.getId()).orElse(null);
            if (centerIntroduction == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Center introduction not found in db");
            }

            boolean validUser = jwtFilter.isAdmin() ||
                    (jwtFilter.isCenter() && userId.equals(centerIntroduction
                            .getCenter().getPartner().getUser().getId()));
            if (!validUser) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
            Center center = centerIntroduction.getCenter();
            centerIntroduction.setIntroduction(introductionRequest.getIntroduction());
            byte[] icon = introductionRequest.getCoverPhoto().getBytes();
            centerIntroduction.setCoverPhoto(icon);
            centerIntroduction.setLastUpdate(new Date());
            CenterIntroduction savedCenterIntroduction = centerIntroductionRepo.save(centerIntroduction);
            String responseMessage = jwtFilter.isAdmin() ?
                    center.getName() + "!, introduction have successfully been updated in their profile" :
                    "Hello " + center.getName() + "!, you have successfully updated your center's introduction";
            String adminNotificationMessage = "Center introduction with id: " + savedCenterIntroduction.getId()
                    + ", and info: " + savedCenterIntroduction.getIntroduction() + "/ has been updated";
            String notificationMessage = "You have update your center introduction: " +
                    savedCenterIntroduction.getIntroduction();
            jwtFilter.sendNotifications("/topic/updateCenterIntroduction", adminNotificationMessage,
                    user, notificationMessage, savedCenterIntroduction);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteCenterIntroduction(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside deleteCenterIntroduction {}", id);
            Optional<CenterIntroduction> optional = centerIntroductionRepo.findById(id);
            CenterIntroduction centerIntroduction = optional.orElse(null);

            if (centerIntroduction == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Center introduction id not found");
            }

            if (!isAuthorizedToDeleteCenterIntroduction(centerIntroduction)) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (centerIntroduction.getCenter().getStatus().equalsIgnoreCase("false")) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Center is inactive, Cannot complete request");
            }

            centerIntroductionRepo.delete(centerIntroduction);
            centerIntroduction.getCenter().setStatus("false");
            centerRepo.save(centerIntroduction.getCenter());
            String adminNotificationMessage = "Center introduction with id: " + centerIntroduction.getId()
                    + ", and info " + centerIntroduction.getIntroduction() + ", has been deleted";
            String notificationMessage = "You have successfully deleted your center introduction : "
                    + centerIntroduction.getIntroduction();
            jwtFilter.sendNotifications("/topic/deleteCenterIntroduction", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, centerIntroduction);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Center introduction deleted from profile successfully" +
                    " and center is now inactive");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<CenterIntroduction>> getAllCenterIntroductions() {
        try {
            log.info("Inside getAllCenterIntroductions");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<CenterIntroduction> centerIntroductions = centerIntroductionRepo.findAll();
            return new ResponseEntity<>(centerIntroductions, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<CenterIntroduction>> getMyCenterIntroductions() {
        try {
            log.info("Inside getMyCenterIntroductions");
            if (!(jwtFilter.isAdmin()) || jwtFilter.isCenter()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            Center center = centerRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (center == null) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            List<CenterIntroduction> centerIntroductions = centerIntroductionRepo.findByCenter(center);
            return new ResponseEntity<>(centerIntroductions, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> addCenterLocation(LocationRequest locationRequest) throws JsonProcessingException {
        try {
            log.info("Inside addCenterLocation {}", locationRequest);
            boolean isValid = locationRequest.isValidRequest(false);
            log.info("Is request valid? {}", isValid);

            if (!(jwtFilter.isAdmin() || jwtFilter.isCenter())) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            Center center = centerRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (jwtFilter.isAdmin()) {
                if (locationRequest.getCenterId() == null) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Admin must provide center Id");
                }

                center = centerRepo.findByCenterId(locationRequest.getCenterId());
            }

            if (center == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Center not found in db");
            }

            CenterLocation centerLocation = centerLocationRepo.findBySubName(locationRequest.getSubName());
            if (centerLocation != null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Location exists already");
            }

            getCenterLocationFromMap(locationRequest, center);
            String responseMessage = jwtFilter.isAdmin() ?
                    center.getName() + "'s, center location have successfully been added to their list" :
                    "Hello " + center.getName() + ", you have successfully added an location to your center profile ";

            // check if all trainer entities are added and set the trainer status to true
            String resultMessage = canUpdateCenterStatus(center, "true");
            if (resultMessage.startsWith("Conditions")) {
                center.setStatus("true");
                centerRepo.save(center);
                responseMessage = jwtFilter.isAdmin() ?
                        center.getName() + "'s, center location have successfully been added to their list" +
                                " and their center account has been activated successfully" :
                        "Hello " + center.getName() + ", you have successfully added an location to your center profile " +
                                " and your center account has been successfully activated";
            }

            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateCenterLocation(LocationRequest locationRequest) throws JsonProcessingException {
        try {
            log.info("Inside updateCenterLocation {}", locationRequest);
            Integer userId = jwtFilter.getCurrentUserId();
            boolean isValid = locationRequest.isValidRequest(true);
            log.info("Is request valid? {}", isValid);

            if (!jwtFilter.isAdmin() || !jwtFilter.isCenter()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid || locationRequest.getId() == null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            CenterLocation centerLocation = centerLocationRepo.findById(locationRequest.getId()).orElse(null);
            if (centerLocation == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Center location not found in db");
            }

            boolean validUser = jwtFilter.isAdmin() ||
                    (jwtFilter.isCenter() && userId.equals(centerLocation
                            .getCenter().getPartner().getUser().getId()));
            if (!validUser) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
            Center center = centerLocation.getCenter();
            String filesFolderPath = BerlizConstants.CENTER_SUB_LOCATION;
            String fileName = generateFileName(center.getName(), locationRequest.getSubName(),
                    "location");
            writeToFile(filesFolderPath, fileName, locationRequest.getCoverPhoto());

            // Save location details to the database
            centerLocation.setSubName(locationRequest.getSubName());
            centerLocation.setCoverPhoto(fileName);
            centerLocation.setAddress(locationRequest.getAddress());
            centerLocation.setLocationUrl(locationRequest.getLocationUrl());
            centerLocation.setLastUpdate(new Date());
            CenterLocation savedCenterLocation = centerLocationRepo.save(centerLocation);
            String responseMessage = jwtFilter.isAdmin() ?
                    center.getName() + "!, location have successfully been updated in their profile" :
                    "Hello " + center.getName() + "!, you have successfully updated your center's location";
            String adminNotificationMessage = "Center location with id: " + savedCenterLocation.getId()
                    + ", and info: " + savedCenterLocation.getSubName() + "/ has been updated";
            String notificationMessage = "You have update your center location: " +
                    savedCenterLocation.getSubName();
            jwtFilter.sendNotifications("/topic/updateCenterLocation", adminNotificationMessage,
                    user, notificationMessage, savedCenterLocation);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteCenterLocation(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside deleteCenterLocation {}", id);
            Optional<CenterLocation> optional = centerLocationRepo.findById(id);
            CenterLocation centerLocation = optional.orElse(null);

            if (centerLocation == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Center location id not found");
            }

            if (!isAuthorizedToDeleteCenterLocation(centerLocation)) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (centerLocation.getCenter().getStatus().equalsIgnoreCase("false")) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Center is inactive, Cannot complete request");
            }

            centerLocationRepo.delete(centerLocation);
            String adminNotificationMessage = "Center location with id: " + centerLocation.getId()
                    + ", and info " + centerLocation.getSubName() + ", has been deleted";
            String notificationMessage = "You have successfully deleted a location from your center : "
                    + centerLocation.getSubName();
            jwtFilter.sendNotifications("/topic/deleteCenterLocation", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, centerLocation);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Center location deleted from profile successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<CenterLocation>> getAllCenterLocations() {
        try {
            log.info("Inside getAllCenterLocations");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<CenterLocation> centerLocations = centerLocationRepo.findAll();
            return new ResponseEntity<>(centerLocations, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<CenterLocation>> getMyCenterLocations() {
        try {
            log.info("Inside getMyCenterLocations");
            if (!(jwtFilter.isAdmin()) || jwtFilter.isCenter()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            Center center = centerRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (center == null) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            List<CenterLocation> centerLocations = centerLocationRepo.findByCenter(center);
            return new ResponseEntity<>(centerLocations, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> addCenterPhotoAlbum(PhotoAlbumRequest photoAlbumRequest) throws JsonProcessingException {
        try {
            log.info("Inside addCenterPhotoAlbum {}", photoAlbumRequest);
            boolean isValid = false;
            log.info("Is request valid? {}", isValid);

            if (!(jwtFilter.isAdmin() || jwtFilter.isCenter())) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            Center center = centerRepo.findByUserId(jwtFilter.getCurrentUserId());
//            if (jwtFilter.isAdmin()) {
//                if (photoAlbumRequest.get() == null) {
//                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Admin must provide center Id");
//                }
//
//                center = centerRepo.findByCenterId(photoAlbumRequest.getCenterId());
//            }

            if (center == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Center not found in db");
            }

            getCenterPhotoAlbumFromMap(photoAlbumRequest, center);
            String responseMessage = jwtFilter.isAdmin() ?
                    center.getName() + "'s, center photo have successfully been added to their album" :
                    "Hello " + center.getName() + ", you have successfully added a photo to your center profile ";

            // check if all center entities are added and set the center status to true
            String resultMessage = canUpdateCenterStatus(center, "true");
            if (resultMessage.startsWith("Conditions")) {
                center.setStatus("true");
                centerRepo.save(center);
                responseMessage = jwtFilter.isAdmin() ?
                        center.getName() + "'s, center photo have successfully been added to their album" +
                                " and their center account has been activated successfully" :
                        "Hello " + center.getName() + ", you have successfully added a photo to your center profile " +
                                " and your center account has been successfully activated";
            }

            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateCenterPhotoAlbum(PhotoAlbumRequest photoAlbumRequest) throws JsonProcessingException {
        try {
            log.info("Inside updateCenterPhotoAlbum {}", photoAlbumRequest);
            Integer userId = jwtFilter.getCurrentUserId();
            boolean isValid = true;
            log.info("Is request valid? {}", isValid);

            if (!jwtFilter.isAdmin() || !jwtFilter.isCenter()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid || photoAlbumRequest.getId() == null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            CenterPhotoAlbum centerPhotoAlbum = centerPhotoAlbumRepo.findById(photoAlbumRequest.getId()).orElse(null);
            if (centerPhotoAlbum == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Center photo album not found in db");
            }

            boolean validUser = jwtFilter.isAdmin() ||
                    (jwtFilter.isCenter() && userId.equals(centerPhotoAlbum
                            .getCenter().getPartner().getUser().getId()));
            if (!validUser) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
            Center center = centerPhotoAlbum.getCenter();
            String filesFolderPath = BerlizConstants.CENTER_PHOTO_ALBUM_LOCATION;
            long numericUUID = UUID.randomUUID().getMostSignificantBits();
            String truncatedUUID = String.valueOf(numericUUID).substring(0, 15);
            String fileName = generateFileName(center.getName(), "photo", truncatedUUID);
//            writeToFile(filesFolderPath, fileName, photoAlbumRequest.getPhoto());

            // Save location details to the database
            centerPhotoAlbum.setComment(photoAlbumRequest.getComment());
            centerPhotoAlbum.setPhoto(fileName);
            center.setLastUpdate(new Date());
            CenterPhotoAlbum savedCenterPhotoAlbum = centerPhotoAlbumRepo.save(centerPhotoAlbum);
            String responseMessage = jwtFilter.isAdmin() ?
                    center.getName() + "!, photo have successfully been updated in their profile" :
                    "Hello " + center.getName() + "!, you have successfully updated your center's photo";
            String adminNotificationMessage = "Center photo with id: " + savedCenterPhotoAlbum.getId()
                    + ", and info: " + savedCenterPhotoAlbum.getUuid() + "/ has been updated";
            String notificationMessage = "You have update your center photo: " +
                    savedCenterPhotoAlbum.getUuid();
            jwtFilter.sendNotifications("/topic/updateCenterPhotoAlbum", adminNotificationMessage,
                    user, notificationMessage, savedCenterPhotoAlbum);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteCenterPhotoAlbum(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside deleteCenterPhotoAlbum {}", id);
            Optional<CenterPhotoAlbum> optional = centerPhotoAlbumRepo.findById(id);
            CenterPhotoAlbum centerPhotoAlbum = optional.orElse(null);

            if (centerPhotoAlbum == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Center photo album id not found");
            }

            if (!isAuthorizedToDeleteCenterPhotoAlbum(centerPhotoAlbum)) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (centerPhotoAlbum.getCenter().getStatus().equalsIgnoreCase("false")) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Center is inactive, Cannot complete request");
            }

            centerPhotoAlbumRepo.delete(centerPhotoAlbum);
            String adminNotificationMessage = "Center photo with id: " + centerPhotoAlbum.getId()
                    + ", and info " + centerPhotoAlbum.getUuid() + ", has been deleted";
            String notificationMessage = "You have successfully deleted a photo from your center : "
                    + centerPhotoAlbum.getUuid();
            jwtFilter.sendNotifications("/topic/deleteCenterPhotoAlbum", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, centerPhotoAlbum);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Center photo deleted from album successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<CenterPhotoAlbum>> getAllCenterPhotoAlbums() {
        try {
            log.info("Inside getAllCenterPhotoAlbums");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<CenterPhotoAlbum> centerPhotoAlbums = centerPhotoAlbumRepo.findAll();
            return new ResponseEntity<>(centerPhotoAlbums, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<CenterPhotoAlbum>> getMyCenterPhotoAlbums() {
        try {
            log.info("Inside getMyCenterPhotoAlbums");
            if (!(jwtFilter.isAdmin()) || jwtFilter.isCenter()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            Center center = centerRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (center == null) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);
            }

            List<CenterPhotoAlbum> centerPhotoAlbums = centerPhotoAlbumRepo.findByCenter(center);
            return new ResponseEntity<>(centerPhotoAlbums, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> addCenterPricing(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside addCenterPricing {}", requestMap);
            boolean isValid = validateCenterPricingRequestFromMap(requestMap, false);
            log.info("Is request valid? {}", isValid);

            if (!(jwtFilter.isAdmin() || jwtFilter.isTrainer())) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            Center center = centerRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (jwtFilter.isAdmin()) {
                if (requestMap.get("centerId").isEmpty()) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Admin must provide centerId");
                }

                center = centerRepo.findByCenterId(Integer.valueOf(requestMap.get("centerId")));
            }

            if (center == null) {
                String centerNotFoundResponse = jwtFilter.isAdmin() ? "Center not found in db" : "You are not authorized to make this request, " +
                        "Please contact admin to check your center status";
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, centerNotFoundResponse);
            }

            CenterPricing centerPricing = centerPricingRepo.findByCenter(center);
            if (centerPricing != null) {
                String centerPricingExitsResponse = jwtFilter.isAdmin() ? "Center pricing already exits for " +
                        center.getName() : "Hello " + center.getName() +
                        ", you already have an active pricing";
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, centerPricingExitsResponse);
            }

            getCenterPricingFromMap(requestMap, center);
            String responseMessage = jwtFilter.isAdmin() ?
                    center.getName() + ", pricing have successfully been added to their profile " :
                    "Congratulations! " + center.getName() +
                            ", your pricing has been added to your center profile successfully";

            // check if all trainer entities are added and set the trainer status to true
            String resultMessage = canUpdateCenterStatus(center, "true");
            if (resultMessage.startsWith("Conditions")) {
                center.setStatus("true");
                centerRepo.save(center);
                responseMessage = jwtFilter.isAdmin() ?
                        center.getName() + ", pricing have successfully been added to their profile " +
                                "and their center account has been activated successfully" :
                        "Congratulations! " + center.getName() +
                                ", your pricing has been added successfully " +
                                "and your center account has been successfully activated";
            }

            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateCenterPricing(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside updateCenterPricing {}", requestMap);
            boolean isValid = validateCenterPricingRequestFromMap(requestMap, true);
            log.info("Is request valid? {}", isValid);

            if (!jwtFilter.isAdmin() || jwtFilter.isTrainer()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            Optional<CenterPricing> optional = centerPricingRepo.findById(Integer.valueOf(requestMap.get("id")));
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Center pricing ID not found");
            }

            CenterPricing centerPricing = optional.get();
            String currentUser = jwtFilter.getCurrentUserEmail();
            if (!(jwtFilter.isAdmin()
                    || centerPricing.getCenter().getPartner().getUser().getEmail().equals(currentUser))) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            BigDecimal price = new BigDecimal(requestMap.get("price"));
            centerPricing.setPrice(price);
            BigDecimal discount2Programs = new BigDecimal(requestMap.get("discount2Programs"));
            centerPricing.setDiscount2Programs(discount2Programs);
            BigDecimal discount3Months = new BigDecimal(requestMap.get("discount3Months"));
            centerPricing.setDiscount3Months(discount3Months);
            BigDecimal discount6Months = new BigDecimal(requestMap.get("discount6Months"));
            centerPricing.setDiscount6Months(discount6Months);
            BigDecimal discount9Months = new BigDecimal(requestMap.get("discount9Months"));
            centerPricing.setDiscount9Months(discount9Months);
            BigDecimal discount12Months = new BigDecimal(requestMap.get("discount12Months"));
            centerPricing.setDiscount12Months(discount12Months);
            centerPricing.setLastUpdate(new Date());
            CenterPricing savedcenterPricing = centerPricingRepo.save(centerPricing);
            String responseMessage;
            if (jwtFilter.isAdmin()) {
                responseMessage = centerPricing.getCenter().getName() + "'s centerPricing updated successfully";
            } else {
                responseMessage = "Hello " +
                        centerPricing.getCenter().getName() + " you have successfully " +
                        " updated your pricing information";
            }

            String adminNotificationMessage = "Center pricing with id: " + savedcenterPricing.getId()
                    + ", and info: " + savedcenterPricing.getCenter().getName() + "/ has been updated";
            String notificationMessage = "You have update your center pricing: " +
                    savedcenterPricing.getCenter().getName();
            jwtFilter.sendNotifications("/topic/updateCenterPricing", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, savedcenterPricing);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteCenterPricing(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside deleteCenterPricing {}", id);
            User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
            boolean authorizedUser = user.getEmail().equalsIgnoreCase(BerlizConstants.BERLIZ_SUPER_ADMIN);
            if (!(jwtFilter.isAdmin() && authorizedUser)) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            Optional<CenterPricing> optional = centerPricingRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "CenterPricing id not found");
            }

            CenterPricing centerPricing = optional.get();
            centerPricingRepo.delete(centerPricing);
            String adminNotificationMessage = "Center pricing with id: " + centerPricing.getId()
                    + ", and info " + centerPricing.getPrice() + ", has been deleted";
            String notificationMessage = "You have successfully deleted your center pricing: "
                    + centerPricing.getPrice();
            jwtFilter.sendNotifications("/topic/deleteCenterPricing", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, centerPricing);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "CenterPricing deleted successfully");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<CenterPricing>> getAllCenterPricing() {
        try {
            log.info("Inside getAllCenterPricing");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<CenterPricing> centerPricing = centerPricingRepo.findAll();
            return new ResponseEntity<>(centerPricing, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<CenterPricing> getMyCenterPricing() {
        try {
            log.info("Inside getMyCenterPricing");
            if (!(jwtFilter.isAdmin()) || jwtFilter.isCenter()) {
                return new ResponseEntity<>(new CenterPricing(), HttpStatus.UNAUTHORIZED);
            }

            Center center = centerRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (center == null) {
                return new ResponseEntity<>(new CenterPricing(), HttpStatus.UNAUTHORIZED);
            }

            CenterPricing centerPricing = centerPricingRepo.findByCenter(center);
            return new ResponseEntity<>(centerPricing, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new CenterPricing(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> addCenterTrainer(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside addCenterTrainer {}", requestMap);
            boolean isValid = requestMap != null;
            log.info("Is request valid? {}", isValid);
            if (!(jwtFilter.isAdmin() || jwtFilter.isTrainer() || jwtFilter.isCenter())) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            Center center = null;
            Trainer trainer = null;
            if (jwtFilter.isAdmin()) {
                if (requestMap.get("centerId").isEmpty() || requestMap.get("trainerId").isEmpty()) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Admin must provide center and trainer ids");
                }

                center = centerRepo.findByCenterId(Integer.valueOf(requestMap.get("centerId")));
                trainer = trainerRepo.findByTrainerId(Integer.valueOf(requestMap.get("trainerId")));
            }

            if (jwtFilter.isCenter()) {
                if (requestMap.get("centerId").isEmpty()) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Center must provide trainer id");
                }
                center = centerRepo.findByUserId(jwtFilter.getCurrentUserId());
                trainer = trainerRepo.findByTrainerId(Integer.valueOf(requestMap.get("trainerId")));
            }

            if (jwtFilter.isTrainer()) {
                if (requestMap.get("centerId").isEmpty()) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Trainer must provide center id");
                }

                trainer = trainerRepo.findByTrainerId(jwtFilter.getCurrentUserId());
                center = centerRepo.findByCenterId(Integer.valueOf(requestMap.get("centerId")));
            }

            if (center == null) {
                String centerNotFoundResponse = jwtFilter.isAdmin() ? "Center not found in db" : "You are not authorized to make this request, " +
                        "Please contact admin to check center status";
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, centerNotFoundResponse);
            }

            if (trainer == null) {
                String centerNotFoundResponse = jwtFilter.isAdmin() ? "Trainer not found in db" : "You are not authorized to make this request, " +
                        "Please contact admin to check trainer status";
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, centerNotFoundResponse);
            }

            boolean exists = centerTrainerRepo.existsByCenterAndTrainer(center, trainer);
            String existsResponseMessage;
            if (exists) {
                if (jwtFilter.isAdmin()) {
                    existsResponseMessage = "Trainer already assigned to " + center.getName();
                } else if (jwtFilter.isCenter()) {
                    existsResponseMessage = "Hello " + center.getName() + ", you already have this trainer assigned";
                } else {
                    existsResponseMessage = "Hello " + trainer.getName() + ", you already have this center assigned";
                }

                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, existsResponseMessage);
            }


            getCenterTrainerFromMap(center, trainer);
            String responseMessage;
            if (jwtFilter.isAdmin()) {
                responseMessage = MessageFormat.format("Trainer {0} has been successfully assigned to {1}", trainer.getName(), center.getName());
            } else if (jwtFilter.isTrainer()) {
                responseMessage = MessageFormat.format("Hello {0}, you have been successfully assigned as the trainer for {1}", trainer.getName(), center.getName());
            } else {
                responseMessage = MessageFormat.format("Hello {0}, you have successfully added {1} as the trainer for your center", center.getName(), trainer.getName());
            }

            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateCenterTrainerStatus(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside updateCenterTrainerStatus {}", id);
            boolean isValid = id != null;
            log.info("Is request valid? {}", isValid);
            if (!(jwtFilter.isAdmin() || jwtFilter.isCenter())) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            Center center;
            if (jwtFilter.isCenter()) {
                center = centerRepo.findByUserId(jwtFilter.getCurrentUserId());
                if (center == null) {
                    String centerNotFoundResponse = "You are not authorized to make this request, " +
                            "Please contact admin to check center status";
                    return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, centerNotFoundResponse);
                }
            }

            Optional<CenterTrainer> optional = centerTrainerRepo.findById(id);
            CenterTrainer centerTrainer = optional.orElse(null);

            if (centerTrainer == null) {
                String centerTrainerNotFoundResponse = jwtFilter.isAdmin() ? "Center trainer entity not found in db" :
                        "You are not authorized to make this request, " +
                                "Please contact admin to check center trainer entity status";
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, centerTrainerNotFoundResponse);
            }

            center = centerTrainer.getCenter();
            Trainer trainer = centerTrainer.getTrainer();
            boolean validUser = jwtFilter.isAdmin() || (jwtFilter.isCenter() && centerTrainer.getCenter().equals(center));
            if (!validUser) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            String status = centerTrainer.getStatus();
            String newStatus = status.equalsIgnoreCase("true") ? "false" : "true";
            centerTrainer.setStatus(newStatus);

            String responseMessage;
            String activationMessage;

            if (jwtFilter.isAdmin()) {
                activationMessage = newStatus.equals("true") ? "activated" : "deactivated";
                responseMessage = MessageFormat.format("Center {0} has been successfully {1} for {2}", center.getName(), activationMessage, trainer.getName());
            } else {
                activationMessage = newStatus.equals("true") ? "activated" : "deactivated";
                responseMessage = MessageFormat.format("Hello {0}, you have successfully {1} {2} as the trainer for your center", center.getName(), activationMessage, trainer.getName());
            }

            CenterTrainer savedCenterTrainer = centerTrainerRepo.save(centerTrainer);
            String adminNotificationMessage = "Center trainer with id: " + savedCenterTrainer.getId() +
                    ", status has been set to " + savedCenterTrainer.getStatus();
            String notificationMessage = "You have successfully set your center trainer status to : " +
                    savedCenterTrainer.getStatus();
            jwtFilter.sendNotifications("/topic/updateCenterTrainerStatus", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, savedCenterTrainer);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteCenterTrainer(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside deleteCenterTrainer {}", id);
            User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
            boolean authorizedUser = user.getEmail().equalsIgnoreCase(BerlizConstants.BERLIZ_SUPER_ADMIN);
            if (!(jwtFilter.isAdmin() && authorizedUser)) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            Optional<CenterTrainer> optional = centerTrainerRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Center trainer id not found");
            }
            CenterTrainer centerTrainer = optional.get();
            centerTrainerRepo.delete(centerTrainer);
            String adminNotificationMessage = "Center trainer with id: " + centerTrainer.getId()
                    + ", and info " + centerTrainer.getTrainer().getName() + ", has been deleted";
            String notificationMessage = "You have successfully deleted a trainer from your center: "
                    + centerTrainer.getTrainer();
            jwtFilter.sendNotifications("/topic/deleteCenterTrainer", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, centerTrainer);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Center trainer deleted successfully");
        } catch (Exception ex) {
            log.error("Something went wrong while performing operation", ex);
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }


    @Override
    public ResponseEntity<List<CenterTrainer>> getAllCenterTrainers() {
        try {
            log.info("Inside getAllCenterTrainers");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<CenterTrainer> centerTrainers = centerTrainerRepo.findAll();
            return new ResponseEntity<>(centerTrainers, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
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

            Center center = centerRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (center == null) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            List<CenterTrainer> centerTrainers = centerTrainerRepo.findByCenter(center);
            return new ResponseEntity<>(centerTrainers, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> addCenterVideoAlbum(VideoAlbumRequest videoAlbumRequest) throws JsonProcessingException {
        try {
            log.info("Inside addCenterVideoAlbum {}", videoAlbumRequest);
            boolean isValid = videoAlbumRequest.isValidRequest(false);
            log.info("Is request valid? {}", isValid);

            if (!(jwtFilter.isAdmin() || jwtFilter.isCenter())) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            Center center = centerRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (jwtFilter.isAdmin()) {
                if (videoAlbumRequest.getCenterId() == null) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Admin must provide center Id");
                }

                center = centerRepo.findByCenterId(videoAlbumRequest.getCenterId());
            }

            if (center == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Center not found in db");
            }

            getCenterVideoAlbumFromMap(videoAlbumRequest, center);
            String responseMessage = jwtFilter.isAdmin() ?
                    center.getName() + "'s, center video have successfully been added to their album" :
                    "Hello " + center.getName() + ", you have successfully added a video to your center profile ";

            // check if all center entities are added and set the center status to true
            String resultMessage = canUpdateCenterStatus(center, "true");
            if (resultMessage.startsWith("Conditions")) {
                center.setStatus("true");
                centerRepo.save(center);
                responseMessage = jwtFilter.isAdmin() ?
                        center.getName() + "'s, center video have successfully been added to their album" +
                                " and their center account has been activated successfully" :
                        "Hello " + center.getName() + ", you have successfully added a video to your center profile " +
                                " and your center account has been successfully activated";
            }

            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateCenterVideoAlbum(VideoAlbumRequest videoAlbumRequest) throws JsonProcessingException {
        try {
            log.info("Inside updateCenterVideoAlbum {}", videoAlbumRequest);
            Integer userId = jwtFilter.getCurrentUserId();
            boolean isValid = videoAlbumRequest.isValidRequest(true);
            log.info("Is request valid? {}", isValid);

            if (!jwtFilter.isAdmin() || !jwtFilter.isCenter()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid || videoAlbumRequest.getId() == null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            CenterVideoAlbum centerVideoAlbum = centerVideoAlbumRepo.findById(videoAlbumRequest.getId()).orElse(null);
            if (centerVideoAlbum == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Center video album not found in db");
            }

            boolean validUser = jwtFilter.isAdmin() ||
                    (jwtFilter.isCenter() && userId.equals(centerVideoAlbum
                            .getCenter().getPartner().getUser().getId()));
            if (!validUser) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            Center center = centerVideoAlbum.getCenter();
            String filesFolderPath = BerlizConstants.CENTER_VIDEO_ALBUM_LOCATION;
            long numericUUID = UUID.randomUUID().getMostSignificantBits();
            String truncatedUUID = String.valueOf(numericUUID).substring(0, 15);
            String fileName = generateFileName(center.getName(), "video", truncatedUUID);
            writeToFile(filesFolderPath, fileName, videoAlbumRequest.getVideo());

            // Save location details to the database
            centerVideoAlbum.setComment(videoAlbumRequest.getComment());
            centerVideoAlbum.setVideo(fileName);
            center.setLastUpdate(new Date());
            CenterVideoAlbum savedCenterVideoAlbum = centerVideoAlbumRepo.save(centerVideoAlbum);
            String responseMessage = jwtFilter.isAdmin() ?
                    center.getName() + "!, photo have successfully been updated in their profile" :
                    "Hello " + center.getName() + "!, you have successfully updated your center's photo";
            String adminNotificationMessage = "Center video with id: " + savedCenterVideoAlbum.getId()
                    + ", and info: " + savedCenterVideoAlbum.getUuid() + "/ has been updated";
            String notificationMessage = "You have update your center video: " +
                    savedCenterVideoAlbum.getUuid();
            jwtFilter.sendNotifications("/topic/updateCenterVideoAlbum", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, savedCenterVideoAlbum);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteCenterVideoAlbum(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside deleteCenterVideoAlbum {}", id);
            Optional<CenterVideoAlbum> optional = centerVideoAlbumRepo.findById(id);
            CenterVideoAlbum centerVideoAlbum = optional.orElse(null);

            if (centerVideoAlbum == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Center video album id not found");
            }

            if (!isAuthorizedToDeleteCenterVideoAlbum(centerVideoAlbum)) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (centerVideoAlbum.getCenter().getStatus().equalsIgnoreCase("false")) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Center is inactive, Cannot complete request");
            }

            centerVideoAlbumRepo.delete(centerVideoAlbum);
            String adminNotificationMessage = "Center video with id: " + centerVideoAlbum.getId()
                    + ", and info " + centerVideoAlbum.getUuid() + ", has been deleted";
            String notificationMessage = "You have successfully deleted a video from your center album: "
                    + centerVideoAlbum.getUuid();
            jwtFilter.sendNotifications("/topic/deleteCenterVideoAlbum", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, centerVideoAlbum);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Center video deleted from album successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<CenterVideoAlbum>> getAllCenterVideoAlbums() {
        try {
            log.info("Inside getAllCenterVideoAlbums");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<CenterVideoAlbum> centerVideoAlbums = centerVideoAlbumRepo.findAll();
            return new ResponseEntity<>(centerVideoAlbums, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<CenterVideoAlbum>> getMyCenterVideoAlbums() {
        try {
            log.info("Inside getMyCenterVideoAlbums");
            if (!(jwtFilter.isAdmin()) || jwtFilter.isCenter()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            Center center = centerRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (center == null) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);
            }

            List<CenterVideoAlbum> centerVideoAlbums = centerVideoAlbumRepo.findByCenter(center);
            return new ResponseEntity<>(centerVideoAlbums, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> addCenterReview(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside addCenterReview {}", requestMap);
            boolean isValid = requestMap.containsKey("comment");
            log.info("Is request valid? {}", isValid);

            if (!(jwtFilter.isAdmin() || jwtFilter.isCenter())) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            if (requestMap.get("centerId") == null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Center id is invalid");
            }

            Center center = centerRepo.findByCenterId(Integer.valueOf(requestMap.get("centerId")));
            if (center == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Center not found in db");
            }

            Member member = memberRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (jwtFilter.isAdmin()) {
                if (requestMap.get("memberId") == null) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Admin must provide member id");
                }

                member = memberRepo.findByMemberId(Integer.valueOf(requestMap.get("memberId")));
            }

            if (member == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Member not found in db");
            }

            getCenterReviewFromMap(requestMap, center, member);
            String responseMessage = jwtFilter.isAdmin() ?
                    member.getUser().getFirstname() + "!, review for " + center.getName() + " have " +
                            "successfully been added to their list" :
                    "Hello " + member.getUser().getFirstname() + "!, you have successfully " +
                            "added a review for your center " + center.getName();
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateCenterReview(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside updateTrainerReview {}", requestMap);
            Integer userId = jwtFilter.getCurrentUserId();
            boolean isValid = requestMap.containsKey("comment");
            log.info("Is request valid? {}", isValid);

            if (!(jwtFilter.isAdmin() || jwtFilter.isMember() || jwtFilter.isMemberClient())) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValid || requestMap.get("id") == null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            CenterReview centerReview = centerReviewRepo.findById(Integer.valueOf(requestMap.get("id")))
                    .orElse(null);
            if (centerReview == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Center review" +
                        " for center not found in db");
            }

            boolean validUser = jwtFilter.isAdmin() ||
                    (jwtFilter.isTrainer() && userId.equals(centerReview
                            .getMember().getUser().getId()));
            if (!validUser) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            Center center = centerReview.getCenter();
            if (jwtFilter.isAdmin()) {
                centerReview.setLikes(Integer.valueOf(requestMap.get("likes")));
            }
            centerReview.setComment(requestMap.get("comment"));
            centerReview.setLastUpdate(new Date());

            CenterReview savedCenterReview = centerReviewRepo.save(centerReview);
            String responseMessage = jwtFilter.isAdmin() ?
                    "Center review for " + center.getName() + "!, has successfully been updated in their profile" :
                    "Hello " + centerReview.getMember().getUser().getFirstname() + "!, you have successfully " +
                            "updated your center review for" + center.getName();
            String adminNotificationMessage = "Center review with id: " + savedCenterReview.getId()
                    + ", and info: " + savedCenterReview.getComment() + "/ has been updated";
            String notificationMessage = "You have update your center review: " +
                    savedCenterReview.getComment();
            jwtFilter.sendNotifications("/topic/updateTrainerReview", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, savedCenterReview);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateCenterReviewStatus(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside updateCenterReviewStatus {}", id);
            String status;
            if (!(jwtFilter.isAdmin() || jwtFilter.isCenter())) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            Optional<CenterReview> optional = centerReviewRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Center review ID not found");
            }

            log.info("Inside optional {}", optional);
            status = optional.get().getStatus();
            CenterReview centerReview = optional.get();
            String responseMessage;
            if (status.equalsIgnoreCase("true")) {
                status = "false";
                responseMessage = "Center review status updated successfully. Now deactivated";
            } else {
                status = "true";
                responseMessage = "Center review status updated successfully. Now activated";
            }

            centerReview.setStatus(status);
            CenterReview savedCenterReview = centerReviewRepo.save(centerReview);
            String adminNotificationMessage = "Center review with id: " + savedCenterReview.getId() +
                    ", status has been set to " + savedCenterReview.getStatus();
            String notificationMessage = "You have successfully set your center review status to : " +
                    savedCenterReview.getStatus();
            jwtFilter.sendNotifications("/topic/updateCenterReviewStatus", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, savedCenterReview);
            return BerlizUtilities.buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> disableCenterReview(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside disableCenterReview {}", id);
            if (!(jwtFilter.isAdmin() || jwtFilter.isMember() || jwtFilter.isCenter() || jwtFilter.isMemberClient())) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            Optional<CenterReview> optional = centerReviewRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Center review ID not found");
            }

            log.info("Inside optional {}", optional);
            CenterReview centerReview = optional.get();
            centerReview.setStatus("false");
            CenterReview savedCenterReview = centerReviewRepo.save(centerReview);
            String adminNotificationMessage = "Center review with id: " + savedCenterReview.getId() +
                    ", has been disabled: " + savedCenterReview.getComment();
            String notificationMessage = "You have successfully disabled a review in your center : " +
                    savedCenterReview.getComment();
            jwtFilter.sendNotifications("/topic/updateCenterReviewStatus", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, savedCenterReview);
            simpMessagingTemplate.convertAndSend("/topic/disableCenterReview", savedCenterReview);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Review for " + centerReview.getCenter().getName() +
                    " has been removed from feed successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteCenterReview(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside deleteCenterReview {}", id);
            if (!(jwtFilter.isAdmin() || jwtFilter.isClient())) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            Optional<CenterReview> optional = centerReviewRepo.findById(id);
            CenterReview centerReview = optional.orElse(null);
            if (centerReview == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Center review id not found");
            }

            if (isAuthorizedToDeleteCenterReview(centerReview)) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!centerReview.getCenter().getStatus().equalsIgnoreCase("false")) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Center is inactive, Cannot complete request");
            }

            centerReviewRepo.delete(centerReview);
            String adminNotificationMessage = "Center review with id: " + centerReview.getId()
                    + ", and info " + centerReview.getComment() + ", has been deleted";
            String notificationMessage = "You have successfully deleted a review from your center: "
                    + centerReview.getComment();
            jwtFilter.sendNotifications("/topic/deleteCenterReview", adminNotificationMessage,
                    jwtFilter.getCurrentUser(), notificationMessage, centerReview);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Center review for" +
                    centerReview.getCenter().getName() + "  deleted successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<CenterReview>> getMyCenterReviews() {
        try {
            log.info("Inside getMyCenterReviews");
            if (!(jwtFilter.isAdmin() || jwtFilter.isMember() || jwtFilter.isCenter()) || jwtFilter.isMemberClient()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            Member member = memberRepo.findByUserId(jwtFilter.getCurrentUserId());
            if (member == null) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            List<CenterReview> centerReviews = centerReviewRepo.findByMember(member);
            return new ResponseEntity<>(centerReviews, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<CenterReview>> getAllCenterReviews() {
        try {
            log.info("Inside getAllCenterReviews");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<CenterReview> centerReviews = centerReviewRepo.findAll();
            return new ResponseEntity<>(centerReviews, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<CenterReview>> getActiveCenterReviews(Integer id) {
        try {
            log.info("Inside getActiveCenterReviews");
            Center center = centerRepo.findByCenterId(id);
            if (center == null) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            List<CenterReview> centerReviews = centerReviewRepo.getActiveCenterReviewsByCenter(center);
            return new ResponseEntity<>(centerReviews, HttpStatus.OK);
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
     */
    private void getCenterFromMap(CenterRequest centerRequest) throws IOException {
        Center center = new Center();
        User user;
        Partner partner = partnerRepo.findByPartnerId(centerRequest.getPartnerId());
        if (jwtFilter.isAdmin()) {
            String userEmail = partner.getUser().getEmail();
            user = userRepo.findByEmail(userEmail);
        } else {
            user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
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
        Center savedCenter = centerRepo.save(center);
        String adminNotificationMessage = "A new center with id: " + savedCenter.getId() + " and name"
                + savedCenter.getName() + ", has been added for " + user.getEmail();
        String notificationMessage = "You have successfully created your center account: " + savedCenter.getName();
        jwtFilter.sendNotifications("/topic/getCenterFromMap", adminNotificationMessage,
                jwtFilter.getCurrentUser(), notificationMessage, savedCenter);
    }

    /**
     * Updates a Center object from the provided request map and saves it to the repository.
     *
     * @param requestMap The map containing the request data
     * @return The constructed and saved Center object
     */
    private Center updateCenterFromMap(Map<String, String> requestMap, Center existingCenter, User user) {
        String categoryIdsString = requestMap.get("categoryIds");
        String[] categoryIdsArray = categoryIdsString.split(",");
        Set<Category> categorySet = new HashSet<>();
        for (String categoryIdString : categoryIdsArray) {
            int categoryId = Integer.parseInt(categoryIdString.trim());
            Optional<Category> optionalCategory = categoryRepo.findById(categoryId);
            if (optionalCategory.isEmpty()) {
                log.error("Category with ID " + categoryId + " not found");
            }
            categorySet.add(optionalCategory.orElse(null));
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
        String adminNotificationMessage = "Center with id: " + existingCenter.getId() + ", and name: "
                + existingCenter.getName() + ", account information has been updated";
        String notificationMessage = "Your center account information has been updated : " + existingCenter.getName();
        jwtFilter.sendNotifications("/topic/updateCenter", adminNotificationMessage,
                user, notificationMessage, existingCenter);
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

            if (isValidRole(partnerId)) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Invalid partner role. Partner must be a center");
            }

            if (isApprovedCenterPartner(partnerId)) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, "Partner application hasn't been approved yet");
            }

            if (isCenterNameAlreadyExists(centerRequest.getName())) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Center name is already taken. Please choose another name");
            }

            getCenterFromMap(centerRequest);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Center added successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    private void getCenterReviewFromMap(Map<String, String> requestMap, Center center, Member member) {
        CenterReview centerReview = new CenterReview();
        centerReview.setCenter(center);
        centerReview.setMember(member);
        centerReview.setComment(requestMap.get("comment"));
        centerReview.setLikes(0);
        centerReview.setDate(new Date());
        centerReview.setLastUpdate(new Date());
        centerReview.setStatus("false"); // Initializing status
        CenterReview savedCenterReview = centerReviewRepo.save(centerReview);
        String adminNotificationMessage = "A new review with id: " + savedCenterReview.getId() + " and info"
                + savedCenterReview.getComment() + ", has been added for center: " + center.getName();
        String notificationMessage = "You have successfully added a review for center: " + center.getName();
        jwtFilter.sendNotifications("/topic/getCenterReviewFromMap", adminNotificationMessage,
                jwtFilter.getCurrentUser(), notificationMessage, savedCenterReview);
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
            if (partner == null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Partner id not found");
            }

            Integer partnerId = partner.getId();
            Center center = centerRepo.findByPartnerId(partnerId);
            if (center != null) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Center already exists");
            }

            if (isValidRole(partnerId)) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Invalid partner role. Partner must be a center");
            }

            if (isApprovedCenterPartner(partnerId)) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Your partnership application is under review, please wait for admin approval");
            }

            if (isCenterNameAlreadyExists(centerRequest.getName())) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Center name is already taken. Please choose another name");
            }

            getCenterFromMap(centerRequest);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Your Trainer account has successfully been created");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void getCenterAnnouncementFromMap(AnnouncementRequest announcementRequest, Center center) throws IOException {
        CenterAnnouncement centerAnnouncement = new CenterAnnouncement();
        centerAnnouncement.setCenter(center);
        centerAnnouncement.setAnnouncement(announcementRequest.getAnnouncement());
        byte[] icon = announcementRequest.getIcon().getBytes();
        centerAnnouncement.setIcon(icon);
        centerAnnouncement.setDate(new Date());
        centerAnnouncement.setLastUpdate(new Date());
        centerAnnouncement.setStatus(announcementRequest.getStatus());
        CenterAnnouncement savedCenterAnnouncement = centerAnnouncementRepo.save(centerAnnouncement);
        String adminNotificationMessage = "A new announcement with id: " + savedCenterAnnouncement.getId()
                + " and info" + savedCenterAnnouncement.getAnnouncement()
                + ", has been added for center: " + center.getName();
        String notificationMessage = "You have successfully added an announcement for center: " + center.getName();
        jwtFilter.sendNotifications("/topic/getCenterAnnouncementFromMap", adminNotificationMessage,
                jwtFilter.getCurrentUser(), notificationMessage, savedCenterAnnouncement);
    }

    private void getCenterEquipmentFromMap(EquipmentRequest equipmentRequest, Center center) throws IOException {
        CenterEquipment centerEquipment = new CenterEquipment();
        String filesFolderPath = BerlizConstants.CENTER_EQUIPMENT_LOCATION;

        // Constructing file names in a more robust way
        String equipmentFileName = generateFileName(center.getName(), equipmentRequest.getName(), "equipment");
        String sideFileName = generateFileName(center.getName(), equipmentRequest.getName(), "side_view");
        String rearFileName = generateFileName(center.getName(), equipmentRequest.getName(), "rear_view");
        String frontFileName = generateFileName(center.getName(), equipmentRequest.getName(), "front_view");

        // Writing files
        writeToFile(filesFolderPath, equipmentFileName, equipmentRequest.getImage());
        writeToFile(filesFolderPath, sideFileName, equipmentRequest.getSideView());
        writeToFile(filesFolderPath, rearFileName, equipmentRequest.getRearView());
        writeToFile(filesFolderPath, frontFileName, equipmentRequest.getFrontView());

        // Processing category IDs
        String categoryIdsString = equipmentRequest.getCategoryIds();
        if (!categoryIdsString.isEmpty()) {
            String[] categoryIdsArray = categoryIdsString.split(",");
            Set<Category> categories = new HashSet<>();
            for (String categoryIdString : categoryIdsArray) {
                int categoryId = Integer.parseInt(categoryIdString.trim());
                Category category = categoryRepo.findById(categoryId)
                        .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + categoryId));
                categories.add(category);
            }
            centerEquipment.setCategories(categories);
        }

        // Save equipment details to the database
        centerEquipment.setCenter(center);
        centerEquipment.setName(equipmentRequest.getName());
        centerEquipment.setStockNumber(equipmentRequest.getStockNumber());
        centerEquipment.setImage(equipmentFileName);
        centerEquipment.setSideView(sideFileName);
        centerEquipment.setRearView(rearFileName);
        centerEquipment.setFrontView(frontFileName);
        centerEquipment.setDescription(equipmentRequest.getDescription());
        centerEquipment.setDate(new Date());
        centerEquipment.setLastUpdate(new Date());
        CenterEquipment savedCenterEquipment = centerEquipmentRepo.save(centerEquipment);
        String adminNotificationMessage = "A new equipment with id: " + savedCenterEquipment.getId()
                + " and info" + savedCenterEquipment.getName()
                + ", has been added for center: " + center.getName();
        String notificationMessage = "You have successfully added an equipment for your center: " + centerEquipment.getName();
        jwtFilter.sendNotifications("/topic/getCenterEquipmentFromMap", adminNotificationMessage,
                jwtFilter.getCurrentUser(), notificationMessage, savedCenterEquipment);
    }

    private CenterEquipment updateCenterEquipmentFromMap(CenterEquipment centerEquipment, EquipmentRequest equipmentRequest) throws IOException {
        String filesFolderPath = BerlizConstants.CENTER_EQUIPMENT_LOCATION;

        // Constructing file names in a more robust way
        String equipmentFileName = generateFileName(centerEquipment.getName(), equipmentRequest.getName(), "equipment");
        String sideFileName = generateFileName(centerEquipment.getName(), equipmentRequest.getName(), "side_view");
        String rearFileName = generateFileName(centerEquipment.getName(), equipmentRequest.getName(), "rear_view");
        String frontFileName = generateFileName(centerEquipment.getName(), equipmentRequest.getName(), "front_view");

        // Writing files
        writeToFile(filesFolderPath, equipmentFileName, equipmentRequest.getImage());
        writeToFile(filesFolderPath, sideFileName, equipmentRequest.getSideView());
        writeToFile(filesFolderPath, rearFileName, equipmentRequest.getRearView());
        writeToFile(filesFolderPath, frontFileName, equipmentRequest.getFrontView());

        // Processing category IDs
        String categoryIdsString = equipmentRequest.getCategoryIds();
        if (!categoryIdsString.isEmpty()) {
            String[] categoryIdsArray = categoryIdsString.split(",");
            Set<Category> categories = new HashSet<>();
            for (String categoryIdString : categoryIdsArray) {
                int categoryId = Integer.parseInt(categoryIdString.trim());
                Category category = categoryRepo.findById(categoryId)
                        .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + categoryId));
                categories.add(category);
            }
            centerEquipment.setCategories(categories);
        }

        // Save equipment details to the database
        centerEquipment.setName(equipmentRequest.getName());
        centerEquipment.setStockNumber(equipmentRequest.getStockNumber());
        centerEquipment.setImage(equipmentFileName);
        centerEquipment.setSideView(sideFileName);
        centerEquipment.setRearView(rearFileName);
        centerEquipment.setFrontView(frontFileName);
        centerEquipment.setDescription(equipmentRequest.getDescription());
        centerEquipment.setLastUpdate(new Date());
        return centerEquipment;
    }

    private void getCenterIntroductionFromMap(IntroductionRequest introductionRequest, Center center) throws IOException {
        CenterIntroduction centerIntroduction = new CenterIntroduction();
        centerIntroduction.setCenter(center);
        centerIntroduction.setIntroduction(introductionRequest.getIntroduction());
        byte[] photo = introductionRequest.getCoverPhoto().getBytes();
        centerIntroduction.setCoverPhoto(photo);
        centerIntroduction.setDate(new Date());
        centerIntroduction.setLastUpdate(new Date());
        CenterIntroduction savedCenterIntroduction = centerIntroductionRepo.save(centerIntroduction);
        String adminNotificationMessage = "A new introduction with id: " + savedCenterIntroduction.getId()
                + " and info" + savedCenterIntroduction.getIntroduction()
                + ", has been added for center: " + center.getName();
        String notificationMessage = "You have successfully added an introduction for your center: " + centerIntroduction.getIntroduction();
        jwtFilter.sendNotifications("/topic/getCenterIntroductionFromMap", adminNotificationMessage,
                jwtFilter.getCurrentUser(), notificationMessage, savedCenterIntroduction);
    }

    private void getCenterLocationFromMap(LocationRequest locationRequest, Center center) throws IOException {
        CenterLocation centerLocation = new CenterLocation();
        String filesFolderPath = BerlizConstants.CENTER_SUB_LOCATION;

        // Constructing file names in a more robust way
        String fileName = generateFileName(center.getName(), locationRequest.getSubName(), "location");
        // Writing files
        writeToFile(filesFolderPath, fileName, locationRequest.getCoverPhoto());

        // Save location details to the database
        centerLocation.setCenter(center);
        centerLocation.setSubName(locationRequest.getSubName());
        centerLocation.setCoverPhoto(fileName);
        centerLocation.setAddress(locationRequest.getAddress());
        centerLocation.setLocationUrl(locationRequest.getLocationUrl());
        centerLocation.setDate(new Date());
        centerLocation.setLastUpdate(new Date());

        CenterLocation savedCenterLocation = centerLocationRepo.save(centerLocation);
        String adminNotificationMessage = "A new location with id: " + savedCenterLocation.getId()
                + " and info" + savedCenterLocation.getSubName()
                + ", has been added for center: " + center.getName();
        String notificationMessage = "You have successfully added a location for your center: "
                + savedCenterLocation.getSubName();
        jwtFilter.sendNotifications("/topic/getCenterLocationFromMap", adminNotificationMessage,
                jwtFilter.getCurrentUser(), notificationMessage, savedCenterLocation);
    }

    private void getCenterPhotoAlbumFromMap(PhotoAlbumRequest photoAlbumRequest, Center center) throws IOException {
        CenterPhotoAlbum centerPhotoAlbum = new CenterPhotoAlbum();
        String filesFolderPath = BerlizConstants.CENTER_PHOTO_ALBUM_LOCATION;
        long numericUUID = UUID.randomUUID().getMostSignificantBits();
        String truncatedUUID = String.valueOf(numericUUID).substring(0, 15);
        String fileName = generateFileName(center.getName(), "photo", truncatedUUID);
//        writeToFile(filesFolderPath, fileName, photoAlbumRequest.getPhoto());

        // Save location details to the database
        centerPhotoAlbum.setCenter(center);
        centerPhotoAlbum.setComment(photoAlbumRequest.getComment());
        centerPhotoAlbum.setPhoto(fileName);
        centerPhotoAlbum.setDate(new Date());
        centerPhotoAlbum.setLastUpdate(new Date());
        CenterPhotoAlbum savedCenterPhotoAlbum = centerPhotoAlbumRepo.save(centerPhotoAlbum);
        String adminNotificationMessage = "A new photo with id: " + savedCenterPhotoAlbum.getId()
                + " and info" + savedCenterPhotoAlbum.getUuid()
                + ", has been added for center: " + center.getName();
        String notificationMessage = "You have successfully added a photo for your center: "
                + savedCenterPhotoAlbum.getUuid();
        jwtFilter.sendNotifications("/topic/getCenterPhotoAlbumFromMap", adminNotificationMessage,
                jwtFilter.getCurrentUser(), notificationMessage, savedCenterPhotoAlbum);
    }

    private void getCenterVideoAlbumFromMap(VideoAlbumRequest videoAlbumRequest, Center center) throws IOException {
        CenterVideoAlbum centerVideoAlbum = new CenterVideoAlbum();
        String filesFolderPath = BerlizConstants.CENTER_VIDEO_ALBUM_LOCATION;
        long numericUUID = UUID.randomUUID().getMostSignificantBits();
        String truncatedUUID = String.valueOf(numericUUID).substring(0, 15);
        String fileName = generateFileName(center.getName(), "video", truncatedUUID);
        writeToFile(filesFolderPath, fileName, videoAlbumRequest.getVideo());

        // Save location details to the database
        centerVideoAlbum.setCenter(center);
        centerVideoAlbum.setComment(videoAlbumRequest.getComment());
        centerVideoAlbum.setVideo(fileName);
        centerVideoAlbum.setDate(new Date());
        centerVideoAlbum.setLastUpdate(new Date());
        CenterVideoAlbum savedCenterVideoAlbum = centerVideoAlbumRepo.save(centerVideoAlbum);
        String adminNotificationMessage = "A new video with id: " + savedCenterVideoAlbum.getId()
                + " and info" + savedCenterVideoAlbum.getUuid()
                + ", has been added for center: " + center.getName();
        String notificationMessage = "You have successfully added a video for your center: "
                + savedCenterVideoAlbum.getUuid();
        jwtFilter.sendNotifications("/topic/getCenterVideoAlbumFromMap", adminNotificationMessage,
                jwtFilter.getCurrentUser(), notificationMessage, savedCenterVideoAlbum);
    }

    private void getCenterPricingFromMap(Map<String, String> requestMap, Center center) {
        CenterPricing centerPricing = new CenterPricing();
        centerPricing.setCenter(center);

        // Extract pricing information from the request map
        BigDecimal price = new BigDecimal(requestMap.get("price"));
        centerPricing.setPrice(price);

        // Extract discount information from the request map
        BigDecimal discount2Programs = new BigDecimal(requestMap.get("discount2Programs"));
        centerPricing.setDiscount2Programs(discount2Programs);
        BigDecimal discount3Months = new BigDecimal(requestMap.get("discount3Months"));
        centerPricing.setDiscount3Months(discount3Months);
        BigDecimal discount6Months = new BigDecimal(requestMap.get("discount6Months"));
        centerPricing.setDiscount6Months(discount6Months);
        BigDecimal discount9Months = new BigDecimal(requestMap.get("discount9Months"));
        centerPricing.setDiscount9Months(discount9Months);
        BigDecimal discount12Months = new BigDecimal(requestMap.get("discount12Months"));
        centerPricing.setDiscount12Months(discount12Months);

        // Set date and last update timestamps
        centerPricing.setDate(new Date());
        centerPricing.setLastUpdate(new Date());

        // Save centerPricing entity and broadcast the updated information
        CenterPricing savedCenterPricing = centerPricingRepo.save(centerPricing);
        String adminNotificationMessage = "A new pricing with id: " + savedCenterPricing.getId()
                + " and info" + savedCenterPricing.getPrice()
                + ", has been added for center: " + center.getName();
        String notificationMessage = "You have successfully added pricing for your center: "
                + savedCenterPricing.getPrice();
        jwtFilter.sendNotifications("/topic/getCenterPricingFromMap", adminNotificationMessage,
                jwtFilter.getCurrentUser(), notificationMessage, savedCenterPricing);
    }


    private boolean validateCenterPricingRequestFromMap(Map<String, String> requestMap, boolean isValid) {
        if (isValid) {
            return requestMap.containsKey("id")
                    && requestMap.containsKey("price")
                    && requestMap.containsKey("discount3Months")
                    && requestMap.containsKey("discount6Months")
                    && requestMap.containsKey("discount9Months")
                    && requestMap.containsKey("discount12Months")
                    && requestMap.containsKey("discount2Programs");
        }
        return requestMap.containsKey("price")
                && requestMap.containsKey("discount3Months")
                && requestMap.containsKey("discount6Months")
                && requestMap.containsKey("discount9Months")
                && requestMap.containsKey("discount12Months")
                && requestMap.containsKey("discount2Programs");
    }

    private void getCenterTrainerFromMap(Center center, Trainer trainer) {
        CenterTrainer centerTrainer = new CenterTrainer();
        centerTrainer.setCenter(center);
        centerTrainer.setTrainer(trainer);
        centerTrainer.setDate(new Date());
        centerTrainer.setLastUpdate(new Date());
        centerTrainer.setStatus("false");

        // Save centerTrainer entity and broadcast the updated information
        CenterTrainer savedCenterTrainer = centerTrainerRepo.save(centerTrainer);
        simpMessagingTemplate.convertAndSend("/topic/getCenterTrainerFromMap", savedCenterTrainer);
    }

    private String generateFileName(String centerName, String itemName, String type) {
        long numericUUID = UUID.randomUUID().getMostSignificantBits();
        String truncatedUUID = String.valueOf(numericUUID).substring(0, 15);
        return centerName + "_" + itemName + "_" + type + "-" + truncatedUUID;
    }

    private void writeToFile(String folderPath, String fileName, MultipartFile content) throws IOException {
        String filePath = folderPath + fileName;
        Path path = Paths.get(filePath);
        Files.write(path, content.getBytes());
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
     * @param partnerId ID of the partner to be validated
     * @return The valid partner
     */
    private boolean isValidRole(Integer partnerId) {
        Partner partner = partnerRepo.findByPartnerId(partnerId);
        return partner == null || !partner.getRole().equalsIgnoreCase("center");
    }

    /**
     * Validates if center is an approved partner.
     *
     * @param partnerId ID of the partner to be approved
     * @return The valid partner
     */
    private boolean isApprovedCenterPartner(Integer partnerId) {
        Partner partner = partnerRepo.findByPartnerId(partnerId);
        return partner == null || !partner.getStatus().equalsIgnoreCase("true");
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

    /**
     * Checks if the current user is authorized to delete a CenterLocation.
     *
     * @param centerLocation The CenterLocation to be checked for authorization.
     * @return True if the user is authorized, false otherwise.
     */
    private boolean isAuthorizedToDeleteCenterLocation(CenterLocation centerLocation) {
        User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
        boolean authorizedUser = user.getEmail().equalsIgnoreCase(centerLocation.getCenter().getPartner().getUser().getEmail());
        return jwtFilter.isAdmin() || authorizedUser;
    }

    /**
     * Checks if the current user is authorized to delete a CenterPhotoAlbum.
     *
     * @param centerPhotoAlbum The CenterPhotoAlbum to be checked for authorization.
     * @return True if the user is authorized, false otherwise.
     */
    private boolean isAuthorizedToDeleteCenterPhotoAlbum(CenterPhotoAlbum centerPhotoAlbum) {
        User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
        boolean authorizedUser = user.getEmail().equalsIgnoreCase(centerPhotoAlbum.getCenter().getPartner().getUser().getEmail());
        return jwtFilter.isAdmin() || authorizedUser;
    }

    /**
     * Checks if the current user is authorized to delete a CenterEquipment.
     *
     * @param centerEquipment The CenterEquipment to be checked for authorization.
     * @return True if the user is authorized, false otherwise.
     */
    private boolean isAuthorizedToDeleteCenterEquipment(CenterEquipment centerEquipment) {
        User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
        boolean authorizedUser = user.getEmail().equalsIgnoreCase(centerEquipment.getCenter().getPartner().getUser().getEmail());
        return jwtFilter.isAdmin() || authorizedUser;
    }

    /**
     * Checks if the current user is authorized to delete a CenterAnnouncement.
     *
     * @param centerAnnouncement The CenterAnnouncement to be checked for authorization.
     * @return True if the user is authorized, false otherwise.
     */
    private boolean isAuthorizedToDeleteCenterAnnouncement(CenterAnnouncement centerAnnouncement) {
        User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
        boolean authorizedUser = user.getEmail().equalsIgnoreCase(centerAnnouncement.getCenter().getPartner().getUser().getEmail());
        return jwtFilter.isAdmin() || authorizedUser;
    }

    /**
     * Checks if the current user is authorized to delete a CenterVideoAlbum.
     *
     * @param centerVideoAlbum The CenterVideoAlbum to be checked for authorization.
     * @return True if the user is authorized, false otherwise.
     */
    private boolean isAuthorizedToDeleteCenterVideoAlbum(CenterVideoAlbum centerVideoAlbum) {
        User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
        boolean authorizedUser = user.getEmail().equalsIgnoreCase(centerVideoAlbum.getCenter().getPartner().getUser().getEmail());
        return jwtFilter.isAdmin() || authorizedUser;
    }

    /**
     * Checks if the current user is authorized to delete a CenterIntroduction.
     *
     * @param centerIntroduction The CenterIntroduction to be checked for authorization.
     * @return True if the user is authorized, false otherwise.
     */
    private boolean isAuthorizedToDeleteCenterIntroduction(CenterIntroduction centerIntroduction) {
        User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
        boolean authorizedUser = user.getEmail().equalsIgnoreCase(centerIntroduction.getCenter().getPartner().getUser().getEmail());
        return jwtFilter.isAdmin() || authorizedUser;
    }

    /**
     * Checks if the current user is authorized to delete a CenterReview.
     *
     * @param centerReview The CenterReview to be checked for authorization.
     * @return True if the user is authorized, false otherwise.
     */
    private boolean isAuthorizedToDeleteCenterReview(CenterReview centerReview) {
        User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
        boolean authorizedUser = user.getEmail().equalsIgnoreCase(centerReview.getCenter()
                .getPartner().getUser().getEmail());
        return !jwtFilter.isAdmin() && !authorizedUser;
    }

    /**
     * Checks if the Center's status can be updated based on certain conditions.
     *
     * @param center The Center object to be checked.
     * @param status The new status to be set ("true" for active, "false" for inactive).
     * @return A message indicating whether the Center's status can be updated or providing reasons if not.
     */
    private String canUpdateCenterStatus(Center center, String status) {
        if (status.equalsIgnoreCase("true")) {
            if (isCenterIntroductionInvalid(center)) {
                return "Cannot update status, Center must have an introduction.";
            }

            if (isCenterAnnouncementsInvalid(center)) {
                return "Cannot update status, Center must have at least 4 announcements.";
            }

            if (isCenterPricingInvalid(center)) {
                return "Cannot update status, Center must have price rates.";
            }

            if (isCenterPhotoAlbumInvalid(center)) {
                return "Cannot update status, Center must have at least 5 photos in the photo album.";
            }

            if (isCenterVideoAlbumInvalid(center)) {
                return "Cannot update status, Center must have at least 5 videos in the video album.";
            }

            if (isCenterEquipmentInvalid(center)) {
                return "Cannot update status, Center must have at least 10 equipments.";
            }
        }

        // If all conditions are met, return a success message
        return "Conditions met for updating status.";
    }

    /**
     * Checks if the Center's introduction is invalid.
     *
     * @param center The Center object to be checked.
     * @return True if the Center has no introduction; false otherwise.
     */
    private boolean isCenterIntroductionInvalid(Center center) {
        List<CenterIntroduction> centerIntroduction = centerIntroductionRepo.findByCenter(center);
        return centerIntroduction == null;
    }

    /**
     * Checks if the Center's benefits are invalid.
     *
     * @param center The Center object to be checked.
     * @return True if the Center has less than 5 announcements or no announcement; false otherwise.
     */
    private boolean isCenterAnnouncementsInvalid(Center center) {
        List<CenterAnnouncement> centerAnnouncements = centerAnnouncementRepo.findByCenter(center);
        return centerAnnouncements.isEmpty() || centerAnnouncements.size() < 5;
    }


    /**
     * Checks if the Center's photo album is invalid.
     *
     * @param center The Center object to be checked.
     * @return True if the Center has less than 5 photos in the photo album or no album; false otherwise.
     */
    private boolean isCenterPhotoAlbumInvalid(Center center) {
        List<CenterPhotoAlbum> centerPhotoAlbums = centerPhotoAlbumRepo.findByCenter(center);
        return centerPhotoAlbums.isEmpty() || centerPhotoAlbums.size() < 5;
    }

    /**
     * Checks if the Center's video album is invalid.
     *
     * @param center The Center object to be checked.
     * @return True if the Center has less than 5 videos in the video album or no album; false otherwise.
     */
    private boolean isCenterVideoAlbumInvalid(Center center) {
        List<CenterVideoAlbum> centerVideoAlbums = centerVideoAlbumRepo.findByCenter(center);
        return centerVideoAlbums.isEmpty() || centerVideoAlbums.size() < 5;
    }

    /**
     * Checks if the Center's pricing information is invalid.
     *
     * @param center The Center object to be checked.
     * @return True if the Trainer has no pricing information; false otherwise.
     */
    private boolean isCenterPricingInvalid(Center center) {
        CenterPricing centerPricing = centerPricingRepo.findByCenter(center);
        return centerPricing == null;
    }

    /**
     * Checks if the Center's equipments are invalid.
     *
     * @param center The Center object to be checked.
     * @return True if the Center has no equipments or is less than 10; false otherwise.
     */
    private boolean isCenterEquipmentInvalid(Center center) {
        List<CenterEquipment> centerEquipments = centerEquipmentRepo.findByCenter(center);
        return centerEquipments.isEmpty() || centerEquipments.size() < 10;
    }
}

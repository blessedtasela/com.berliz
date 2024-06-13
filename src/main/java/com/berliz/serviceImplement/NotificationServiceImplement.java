package com.berliz.serviceImplement;

import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.*;
import com.berliz.repositories.NotificationRepo;
import com.berliz.repositories.UserRepo;
import com.berliz.services.NotificationService;
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
public class NotificationServiceImplement implements NotificationService {

    @Autowired
    UserRepo userRepo;

    @Autowired
    JWTFilter jwtFilter;

    @Autowired
    EmailUtilities emailUtilities;

    @Autowired
    NotificationRepo notificationRepo;

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public ResponseEntity<String> addNotification(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside addNotification {}", requestMap);
            boolean isValid = validateRequestFromMap(requestMap);
            log.info("Is request valid? {}", isValid);

            if (!isValid) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
            if (jwtFilter.isAdmin()) {
                if (requestMap.get("userId").isEmpty()) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Admin must provide userId");
                }
                user = userRepo.findByUserId(Integer.valueOf(requestMap.get("userId")));
            }
            if (user == null) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "User not found in db");
            }

            getNotificationFromMap(requestMap, user);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "Notification added for "
                    + user.getFirstname());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<Notification>> getAllNotifications() {
        try {
            log.info("Inside getAllNotifications");
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
            List<Notification> notifications = notificationRepo.findAll();
            return new ResponseEntity<>(notifications, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Notification>> getMyNotifications() {
        try {
            log.info("Inside getMyNotifications");
            if (!jwtFilter.isBerlizUser()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

            User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
            if (user == null) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);
            }

            List<Notification> notifications = notificationRepo.findByUser(user);
            return new ResponseEntity<>(notifications, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "No notification selected");
            }

            List<Integer> idList = Arrays.stream(idArray)
                    .map(Integer::valueOf)
                    .toList();
            List<Notification> notifications = notificationRepo.findAllById(idList);
            if (notifications.isEmpty() || notifications.size() != idList.size()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Notifications id not found");
            }

            log.info("inside optional {}", requestMap);
            boolean isDelete = requestMap.get("action").equalsIgnoreCase("delete");
            boolean isRead = requestMap.get("action").equalsIgnoreCase("read");
            boolean isUnread = requestMap.get("action").equalsIgnoreCase("unread");
            User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
            Boolean isNotification = false;
            if (jwtFilter.isAdmin()) {
                isNotification = true;
            } else {
                for (Integer notificationId : idList) {
                    Optional<Notification> notificationOptional = notificationRepo.findById(notificationId);
                    if (notificationOptional.isEmpty()) {
                        isNotification = false;
                        break;
                    }
                    isNotification = notificationRepo.existsByUser(user);
                }
            }

            if (!isNotification) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            String successMessage = "";
            if (!(isDelete ^ isRead ^ isUnread)) {
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Action not recognized");
            }

            if (isDelete) {
                int updatedCount = notificationRepo.bulkDeleteByIds(idList);
                if (updatedCount < 0) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.SOMETHING_WENT_WRONG);
                }
                successMessage = "Notifications deleted successfully";
            }

            if (isRead) {
                int updatedCount = notificationRepo.bulkReadByIds(idList);
                if (updatedCount < 0) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.SOMETHING_WENT_WRONG);
                }
                successMessage = "Notifications are now read";
            }

            if (isUnread) {
                int updatedCount = notificationRepo.bulkUnreadByIds(idList);
                if (updatedCount < 0) {
                    return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.SOMETHING_WENT_WRONG);
                }
                successMessage = "Notifications are now unread";
            }

            simpMessagingTemplate.convertAndSend("/topic/notificationBulkAction", notifications);
            return BerlizUtilities.buildResponse(HttpStatus.OK, successMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteNotification(Integer id) throws JsonProcessingException {
        try {
            log.info("inside deleteNotification {}", id);
            if (!jwtFilter.isBerlizUser()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            Optional<Notification> optional = notificationRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Notification not found");
            }
            log.info("inside optional {}", optional);
            try {
                Notification notification = optional.get();
                notificationRepo.deleteById(id);
                simpMessagingTemplate.convertAndSend("/topic/deleteNotification", notification);
                return BerlizUtilities.buildResponse(HttpStatus.OK, "notification deleted successfully");
            } catch (DataIntegrityViolationException ex) {
                // Handle foreign key constraint violation when deleting
                ex.printStackTrace();
                return BerlizUtilities.buildResponse(HttpStatus.BAD_REQUEST, "Cannot delete notification" +
                        " due to a foreign key constraint violation.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> readNotification(Integer id) throws JsonProcessingException {
        try {
            log.info("inside readNotification {}", id);
            if (!jwtFilter.isBerlizUser()) {
                return BerlizUtilities.buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            Optional<Notification> optional = notificationRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.buildResponse(HttpStatus.NOT_FOUND, "Notification not found");
            }
            log.info("inside optional {}", optional);
            Notification notification = optional.get();
            notification.setRead(true);
            notificationRepo.save(notification);
            simpMessagingTemplate.convertAndSend("/topic/readNotification", notification);
            return BerlizUtilities.buildResponse(HttpStatus.OK, "notification read successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    private void getNotificationFromMap(Map<String, String> requestMap, User user) throws ParseException {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setNotification(requestMap.get("notification"));
        Notification savedNotification = notificationRepo.save(notification);
        List<User> admins = userRepo.findAllAdmins();
        for (User admin : admins) {
            Notification adminNotification = new Notification();
            notification.setNotification(requestMap.get("adminNotification"));
            adminNotification.setUser(admin);
            adminNotification.setDate(new Date());
            notificationRepo.save(adminNotification);
        }

        simpMessagingTemplate.convertAndSend("/topic/getNotificationFromMap", savedNotification);
    }

    private boolean validateRequestFromMap(Map<String, String> requestMap) {
        return requestMap.containsKey("notification");
    }
}

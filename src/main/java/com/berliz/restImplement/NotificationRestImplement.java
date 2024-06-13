package com.berliz.restImplement;

import com.berliz.constants.BerlizConstants;
import com.berliz.models.Notification;
import com.berliz.rest.NotificationRest;
import com.berliz.services.NotificationService;
import com.berliz.utils.BerlizUtilities;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class NotificationRestImplement implements NotificationRest {

    @Autowired
    NotificationService notificationService;

    @Override
    public ResponseEntity<String> addNotification(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            return notificationService.addNotification(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<Notification>> getAllNotifications() {
        try {
            return notificationService.getAllNotifications();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Notification>> getMyNotifications() {
        try {
            return notificationService.getMyNotifications();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> bulkAction(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            return notificationService.bulkAction(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteNotification(Integer id) throws JsonProcessingException {
        try {
            return notificationService.deleteNotification(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> readNotification(Integer id) throws JsonProcessingException {
        try {
            return notificationService.readNotification(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }
}

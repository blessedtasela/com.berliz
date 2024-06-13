package com.berliz.services;

import com.berliz.models.Notification;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface NotificationService {
    ResponseEntity<String> addNotification(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<List<Notification>> getAllNotifications();

    ResponseEntity<List<Notification>> getMyNotifications();

    ResponseEntity<String> bulkAction(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> deleteNotification(Integer id) throws JsonProcessingException;

    ResponseEntity<String> readNotification(Integer id) throws JsonProcessingException;
}

package com.berliz.rest;

import com.berliz.models.Notification;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API endpoints for managing notification-related operations.
 */
@RequestMapping(path = "/notification")
public interface NotificationRest {

    @PostMapping(path = "/add")
    ResponseEntity<String> addNotification(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    @GetMapping(path = "/get")
    ResponseEntity<List<Notification>> getAllNotifications();

    @GetMapping(path = "/getMyNotifications")
    ResponseEntity<List<Notification>> getMyNotifications();

    @PutMapping(path = "/bulkAction")
    ResponseEntity<String> bulkAction(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteNotification(@PathVariable Integer id) throws JsonProcessingException;

    @PutMapping(path = "/read/{id}")
    ResponseEntity<String> readNotification(@PathVariable Integer id) throws JsonProcessingException;

}

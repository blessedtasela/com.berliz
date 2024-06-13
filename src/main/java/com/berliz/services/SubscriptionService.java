package com.berliz.services;

import com.berliz.models.Subscription;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface SubscriptionService {
    public ResponseEntity<String> addSubscription(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<List<Subscription>> getAllSubscriptions();

    ResponseEntity<List<Subscription>> getActiveSubscriptions();

    ResponseEntity<String> updateSubscription(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> deleteSubscription(Integer id) throws JsonProcessingException;

    ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException;

    ResponseEntity<Subscription> getSubscription(Integer id);

    ResponseEntity<List<Subscription>> getMySubscriptions();

    ResponseEntity<String> bulkAction(Map<String, String> requestMap) throws JsonProcessingException;
}

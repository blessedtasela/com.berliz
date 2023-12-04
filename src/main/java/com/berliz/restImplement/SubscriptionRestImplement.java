package com.berliz.restImplement;

import com.berliz.constants.BerlizConstants;
import com.berliz.models.Subscription;
import com.berliz.rest.SubscriptionRest;
import com.berliz.services.SubscriptionService;
import com.berliz.utils.BerlizUtilities;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SubscriptionRestImplement implements SubscriptionRest {

    @Autowired
    SubscriptionService subscriptionService;

    @Override
    public ResponseEntity<String> addSubscription(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            return subscriptionService.addSubscription(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<Subscription>> getAllSubscriptions() {
        try {
            return subscriptionService.getAllSubscriptions();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Subscription>> getActiveSubscriptions() {
        try {
            return subscriptionService.getActiveSubscriptions();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateSubscription(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            return subscriptionService.updateSubscription(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteSubscription(Integer id) throws JsonProcessingException {
        try {
            return subscriptionService.deleteSubscription(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException {
        try {
            return subscriptionService.updateStatus(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<Subscription> getSubscription(Integer id) {
        try {
            return subscriptionService.getSubscription(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Subscription(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}

package com.berliz.restImplement;

import com.berliz.constants.BerlizConstants;
import com.berliz.models.Payment;
import com.berliz.rest.PaymentRest;
import com.berliz.services.PaymentService;
import com.berliz.utils.BerlizUtilities;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PaymentRestImplement implements PaymentRest {

    @Autowired
    PaymentService paymentService;

    @Override
    public ResponseEntity<String> addPayment(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            return paymentService.addPayment(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<Payment>> getAllPayments() {
        try {
            return paymentService.getAllPayments();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Payment>> getActivePayments() {
        try {
            return paymentService.getActivePayments();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updatePayment(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            return paymentService.updatePayment(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }


    @Override
    public ResponseEntity<String> deletePayment(Integer id) throws JsonProcessingException {
        try {
            return paymentService.deletePayment(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException {
        try {
            return paymentService.updateStatus(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<Payment> getPayment(Integer id) {
        try {
            return paymentService.getPayment(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Payment(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}

package com.berliz.services;

import com.berliz.models.Client;
import com.berliz.models.Payment;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface PaymentService {
    public ResponseEntity<String> addPayment(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<List<Payment>> getAllPayments();

    ResponseEntity<List<Payment>> getActivePayments();

    ResponseEntity<String> updatePayment(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> deletePayment(Integer id) throws JsonProcessingException;

    ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException;

    ResponseEntity<Payment> getPayment(Integer id);
}

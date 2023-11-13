package com.berliz.services;

import com.berliz.models.ContactUs;
import com.berliz.models.ContactUsMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface ContactUsService {
    ResponseEntity<List<ContactUs>> getAllContactUs();

    ResponseEntity<String> addContactUs(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<?> getContactUs(Integer id);

    ResponseEntity<String> updateContactUs(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException;

    ResponseEntity<String> deleteContactUs(Integer id) throws JsonProcessingException;

    ResponseEntity<String> reviewContactUs(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<List<ContactUsMessage>> getContactUsMessages();
}

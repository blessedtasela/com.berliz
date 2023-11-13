package com.berliz.services;

import com.berliz.models.Newsletter;
import com.berliz.models.NewsletterMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface NewsletterService {
    ResponseEntity<String> updateNewsletter(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> addNewsletter(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<List<Newsletter>> getAllNewsletters(String filterValue);

    ResponseEntity<?> getNewsletter(Integer id);

    ResponseEntity<String> deleteNewsletter(Integer id) throws JsonProcessingException;

    ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException;

    ResponseEntity<String> sendBulkMessage(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> sendMessage(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<List<Newsletter>> getActiveNewsletters();

    ResponseEntity<List<NewsletterMessage>> getNewsletterMessages();
}

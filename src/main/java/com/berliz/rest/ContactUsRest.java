package com.berliz.rest;

import com.berliz.models.ContactUs;
import com.berliz.models.ContactUsMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping(path = "/contactUs")
public interface ContactUsRest {

    @PostMapping(path = "/add")
    ResponseEntity<String> addContactUs(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    @GetMapping(path = "/get")
    ResponseEntity<List<ContactUs>> getAllContactUs();

    @GetMapping(path = "/getContactUsMessages")
    ResponseEntity<List<ContactUsMessage>> getContactUsMessages();

    @GetMapping(path = "/getContactUs/{id}")
    ResponseEntity<?> getContactUs(@PathVariable Integer id) throws JsonProcessingException;

    @PutMapping(path = "/update")
    ResponseEntity<String> updateContactUs(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    @PutMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@PathVariable Integer id) throws JsonProcessingException;

    @PostMapping(path = "/review")
    ResponseEntity<String> reviewContactUs(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteContactUs(@PathVariable Integer id) throws JsonProcessingException;
}

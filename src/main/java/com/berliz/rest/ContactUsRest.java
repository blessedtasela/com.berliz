package com.berliz.rest;

import com.berliz.models.ContactUs;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping(path = "/contactUs")
public interface ContactUsRest {

    @PostMapping(path = "/add")
    ResponseEntity<String> addContactUs(@RequestBody Map<String, String> requestMap);

    @GetMapping(path = "/get")
    ResponseEntity<List<ContactUs>> getAllContactUs();

    @GetMapping(path = "/getContactUs/{id}")
    ResponseEntity<?> getContactUs(@PathVariable Integer id);

    @PutMapping(path = "/update")
    ResponseEntity<String> updateContactUs(@RequestBody Map<String, String> requestMap);

    @PutMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@PathVariable Integer id);

    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteContactUs(@PathVariable Integer id);
}

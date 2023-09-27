package com.berliz.rest;

import com.berliz.models.Newsletter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping(path = "/newsletter")
public interface NewsletterRest {

    @PostMapping(path = "/add")
    ResponseEntity<String> addNewsletter(@RequestBody(required = true) Map<String, String> requestMap);

    @GetMapping(path = "/get")
    ResponseEntity<List<Newsletter>> getAllNewsletters(@RequestParam(required = false) String filterValue);

    @GetMapping(path = "/getNewsletter/{id}")
    ResponseEntity<?> getNewsletter(@PathVariable Integer id);

    @PutMapping(path = "/update")
    ResponseEntity<String> updateNewsletter(@RequestBody(required = true) Map<String, String> requestMap);

    @PutMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@PathVariable Integer id);

    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteNewsletter(@PathVariable Integer id);
}

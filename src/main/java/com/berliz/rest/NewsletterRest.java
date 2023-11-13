package com.berliz.rest;

import com.berliz.models.Newsletter;
import com.berliz.models.NewsletterMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping(path = "/newsletter")
public interface NewsletterRest {

    /**
     * Add a newsletter.
     *
     * @param requestMap A map containing newsletter details.
     * @return ResponseEntity containing a message about the result.
     */
    @PostMapping(path = "/add")
    ResponseEntity<String> addNewsletter(@RequestBody Map<String, String> requestMap);

    /**
     * Get a list of all newsletters.
     *
     * @param filterValue An optional filter value to narrow down the results.
     * @return ResponseEntity containing the list of all newsletters.
     */
    @GetMapping(path = "/get")
    ResponseEntity<List<Newsletter>> getAllNewsletters(@RequestParam(required = false) String filterValue);

    /**
     * Get a list of active newsletters.
     *
     * @return ResponseEntity containing the list of active newsletters.
     */
    @GetMapping(path = "/getActiveNewsletters")
    ResponseEntity<List<Newsletter>> getActiveNewsletters();

    /**
     * Get a list of newsletterMessages.
     *
     * @return ResponseEntity containing the list of active newsletters.
     */
    @GetMapping(path = "/getNewsletterMessages")
    ResponseEntity<List<NewsletterMessage>> getNewsletterMessages();

    /**
     * Get a specific newsletter by ID.
     *
     * @param id The ID of the newsletter to retrieve.
     * @return ResponseEntity containing the specific newsletter.
     */
    @GetMapping(path = "/getNewsletter/{id}")
    ResponseEntity<?> getNewsletter(@PathVariable Integer id);

    /**
     * Update a newsletter.
     *
     * @param requestMap A map containing newsletter details for the update.
     * @return ResponseEntity containing a message about the result.
     */
    @PutMapping(path = "/update")
    ResponseEntity<String> updateNewsletter(@RequestBody Map<String, String> requestMap);

    /**
     * Send a message associated with a newsletter.
     *
     * @param requestMap A map containing the message to send.
     * @return ResponseEntity containing a message about the result.
     */
    @PostMapping(path = "/sendMessage")
    ResponseEntity<String> sendMessage(@RequestBody Map<String, String> requestMap);

    /**
     * Send a bulk message to multiple newsletters.
     *
     * @param requestMap A map containing the bulk message to send.
     * @return ResponseEntity containing a message about the result.
     */
    @PostMapping(path = "/sendBulkMessage")
    ResponseEntity<String> sendBulkMessage(@RequestBody Map<String, String> requestMap);

    /**
     * Update the status of a newsletter by ID.
     *
     * @param id The ID of the newsletter to update.
     * @return ResponseEntity containing a message about the result.
     */
    @PutMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@PathVariable Integer id);

    /**
     * Delete a newsletter by ID.
     *
     * @param id The ID of the newsletter to delete.
     * @return ResponseEntity containing a message about the result.
     */
    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteNewsletter(@PathVariable Integer id);
}

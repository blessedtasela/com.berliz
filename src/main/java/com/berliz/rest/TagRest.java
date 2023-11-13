package com.berliz.rest;

import com.berliz.models.Tag;
import com.berliz.models.Trainer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping(path = "/tag")
public interface TagRest {

    @PostMapping(path = "/add")
    ResponseEntity<String> addTag(@RequestBody(required = true) Map<String, String> requestMap);

    @GetMapping(path = "/get")
    ResponseEntity<List<Tag>> getAllTags();

    /**
     * Get a list of active trainers.
     *
     * @return ResponseEntity containing the list of all trainers.
     */
    @GetMapping(path = "/getActiveTags")
    ResponseEntity<List<Tag>> getActiveTags();

    @PutMapping(path = "/update")
    ResponseEntity<String> updateTag(@RequestBody(required = true) Map<String, String> requestMap);

    @PutMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@PathVariable Integer id);

    @GetMapping(path = "/getTag/{id}")
    ResponseEntity<?> getTag(@PathVariable Integer id);

    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteTag(@PathVariable Integer id);
}

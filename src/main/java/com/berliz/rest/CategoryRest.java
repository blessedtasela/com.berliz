package com.berliz.rest;

import com.berliz.models.Category;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping(path = "/category")
public interface CategoryRest {

    @PostMapping(path = "/add")
    ResponseEntity<String> addCategory(@RequestBody(required = true) Map<String, String> requestMap) throws JsonProcessingException;

    @GetMapping(path = "/get")
    ResponseEntity<List<Category>> getCategories();

    @GetMapping(path = "/getActiveCategories")
    ResponseEntity<List<Category>> getActiveCategories();

    @PutMapping(path = "/update")
    ResponseEntity<String> updateCategory(@RequestBody Map<String, String> requestMap) throws JsonProcessingException;

    @PutMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@PathVariable Integer id) throws JsonProcessingException;

    @GetMapping(path = "/getCategory/{id}")
    ResponseEntity<?> getCategory(@PathVariable Integer id) throws JsonProcessingException;

    @GetMapping(path = "/getByTag/{id}")
    ResponseEntity<List<Category>> getByTag(@PathVariable Integer id);

    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteCategory(@PathVariable Integer id) throws JsonProcessingException;

    /**
     * like a category by its ID.
     *
     * @return ResponseEntity containing the category with the specified ID.
     */
    @PutMapping(path = "/like/{id}")
    ResponseEntity<String> likeCategory(@PathVariable Integer id) throws JsonProcessingException;

}

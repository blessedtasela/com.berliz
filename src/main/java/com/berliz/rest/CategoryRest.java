package com.berliz.rest;

import com.berliz.models.Category;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping(path = "/category")
public interface CategoryRest {

    @PostMapping(path = "/add")
    ResponseEntity<String> addCategory(@RequestBody(required = true) Map<String, String> requestMap);

    @GetMapping(path = "/get")
    ResponseEntity<List<Category>> getCategories();

    @GetMapping(path = "/getActiveCategories")
    ResponseEntity<List<Category>> getActiveCategories();

    @PutMapping(path = "/update")
    ResponseEntity<String> updateCategory(@RequestBody Map<String, String> requestMap);

    @PutMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@PathVariable Integer id);

    @GetMapping(path = "/getCategory/{id}")
    ResponseEntity<?> getCategory(@PathVariable Integer id);

    @GetMapping(path = "/getByTag/{id}")
    ResponseEntity<List<Category>> getByTag(@PathVariable Integer id);

    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteCategory(@PathVariable Integer id);
}

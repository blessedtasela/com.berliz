package com.berliz.services;

import com.berliz.models.Category;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface CategoryService {
    ResponseEntity<String> addCategory(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<List<Category>> getCategories();

    ResponseEntity<List<Category>> getActiveCategories();

    ResponseEntity<String> updateCategory(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> deleteCategory(Integer id) throws JsonProcessingException;

    ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException;
    ResponseEntity<?> getCategory(Integer id);

    ResponseEntity<List<Category>> getByTag(Integer id);

    ResponseEntity<String> likeCategory(Integer id) throws JsonProcessingException;
}

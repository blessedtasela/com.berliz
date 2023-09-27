package com.berliz.services;

import com.berliz.models.Tag;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface TagService {
    ResponseEntity<String> addTag(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<List<Tag>> getAllTags();

    ResponseEntity<String> updateTag(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException;

    ResponseEntity<?> getTag(Integer id);

    ResponseEntity<String> deleteTag(Integer id) throws JsonProcessingException;
}

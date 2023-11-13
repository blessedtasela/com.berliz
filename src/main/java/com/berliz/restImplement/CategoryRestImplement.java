package com.berliz.restImplement;

import com.berliz.constants.BerlizConstants;
import com.berliz.models.Category;
import com.berliz.rest.CategoryRest;
import com.berliz.services.CategoryService;
import com.berliz.utils.BerlizUtilities;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class CategoryRestImplement implements CategoryRest {

    @Autowired
    CategoryService categoryService;

    @Override
    public ResponseEntity<String> addCategory(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            return categoryService.addCategory(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<Category>> getCategories() {
        try {
            return categoryService.getCategories();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Category>> getActiveCategories() {
        try {
            return categoryService.getActiveCategories();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateCategory(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            return categoryService.updateCategory(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteCategory(Integer id) throws JsonProcessingException {
        try {
            return categoryService.deleteCategory(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> likeCategory(Integer id) throws JsonProcessingException {
        try {
            return categoryService.likeCategory(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException {
        try {
            return categoryService.updateStatus(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<?> getCategory(Integer id) throws JsonProcessingException {
        try {
            return categoryService.getCategory(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<Category>> getByTag(Integer id) {
        try {
            return categoryService.getByTag(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}

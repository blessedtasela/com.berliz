package com.berliz.restImplement;

import com.berliz.constants.BerlizConstants;
import com.berliz.models.Testimonial;
import com.berliz.rest.TestimonialRest;
import com.berliz.services.TestimonialService;
import com.berliz.utils.BerlizUtilities;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestimonialRestImplement implements TestimonialRest {

    @Autowired
    TestimonialService testimonialService;

    @Override
    public ResponseEntity<String> addTestimonial(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            return testimonialService.addTestimonial(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<Testimonial>> getAllTestimonials() {
        try {
            return testimonialService.getAllTestimonials();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Testimonial>> getActiveTestimonials() {
        try {
            return testimonialService.getActiveTestimonials();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateTestimonial(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            return testimonialService.updateTestimonial(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> deleteTestimonial(Integer id) throws JsonProcessingException {
        try {
            return testimonialService.deleteTestimonial(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException {
        try {
            return testimonialService.updateStatus(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<Testimonial> getTestimonial(Integer id) {
        try {
            return testimonialService.getTestimonial(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Testimonial(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}

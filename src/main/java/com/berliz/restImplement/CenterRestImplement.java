package com.berliz.restImplement;

import com.berliz.DTO.CenterRequest;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.Center;
import com.berliz.models.CenterLike;
import com.berliz.rest.CenterRest;
import com.berliz.services.CenterService;
import com.berliz.utils.BerlizUtilities;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * REST API endpoints implementation for center-related operations.
 */
@RestController
public class CenterRestImplement implements CenterRest {

    @Autowired
    CenterService centerService;

    /**
     * Add a new center.
     *
     * @param centerRequest The request map containing center details.
     * @return ResponseEntity containing a response message.
     * @throws JsonProcessingException if there is an issue with JSON processing.
     */
    @Override
    public ResponseEntity<String> addCenter(CenterRequest centerRequest) throws JsonProcessingException {
        try {
            return centerService.addCenter(centerRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Get a list of all centers.
     *
     * @return ResponseEntity containing a list of centers.
     */
    @Override
    public ResponseEntity<List<Center>> getAllCenters() {
        try {
            return centerService.getAllCenters();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Center>> getActiveCenters() {
        try {
            return centerService.getActiveCenters();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);

    }

    /**
     * Update an existing center.
     *
     * @param requestMap The request map containing center details.
     * @return ResponseEntity containing a response message.
     * @throws JsonProcessingException if there is an issue with JSON processing.
     */
    @Override
    public ResponseEntity<String> updateCenter(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            return centerService.updateCenter(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Delete a center by its ID.
     *
     * @param id The ID of the center to be deleted.
     * @return ResponseEntity containing a response message.
     * @throws JsonProcessingException if there is an issue with JSON processing.
     */
    @Override
    public ResponseEntity<String> deleteCenter(Integer id) throws JsonProcessingException {
        try {
            return centerService.deleteCenter(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Update the status of a center.
     *
     * @param id The ID of the center to be updated.
     * @return ResponseEntity containing a response message.
     * @throws JsonProcessingException if there is an issue with JSON processing.
     */
    @Override
    public ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException {
        try {
            return centerService.updateStatus(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Get a center by user ID.
     *
     * @param id The ID of the user.
     * @return ResponseEntity containing a center object.
     */
    @Override
    public ResponseEntity<Center> getByUserId(Integer id) {
        try {
            // Delegate the center getByUserId operation to the service
            return centerService.getByUserId(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Center(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Get a center.
     *
     * @return ResponseEntity containing a center object.
     */
    @Override
    public ResponseEntity<Center> getCenter() {
        try {
            // Delegate the center getCenter operation to the service
            return centerService.getCenter();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Center(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Like a center by its ID.
     *
     * @param id The ID of the center to be liked.
     * @return ResponseEntity containing a response message.
     * @throws JsonProcessingException if there is an issue with JSON processing.
     */
    @Override
    public ResponseEntity<String> likeCenter(Integer id) throws JsonProcessingException {
        try {
            return centerService.likeCenter(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<List<CenterLike>> getCenterLikes() throws JsonProcessingException {
        try {
            return centerService.getCenterLikes();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updatePhoto(CenterRequest centerRequest) throws JsonProcessingException {
        try {
            return centerService.updatePhoto(centerRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

}

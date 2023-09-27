package com.berliz.restImplement;

import com.berliz.constants.BerlizConstants;
import com.berliz.models.Center;
import com.berliz.rest.CenterRest;
import com.berliz.services.CenterService;
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

    @Override
    public ResponseEntity<String> addCenter(Map<String, String> requestMap) {
        try {
            // Delegate the center addCenter operation to the service
            return centerService.addCenter(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Center>> getAllCenters() {
        try {
            // Delegate the center getAllCenters operation to the service
            return centerService.getAllCenters();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateCenter(Map<String, String> requestMap) {
        try {
            // Delegate the center updateCenter operation to the service
            return centerService.updateCenter(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updatePartnerId(Integer id, Integer newId) {
        try {
            // Delegate the center updatePartnerId operation to the service
            return centerService.updatePartnerId(id, newId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteCenter(Integer id) {
        try {
            // Delegate the center deleteCenter operation to the service
            return centerService.deleteCenter(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateStatus(Integer id) {
        try {
            // Delegate the center updateStatus operation to the service
            return centerService.updateStatus(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Center> getByPartnerId(Integer id) {
        try {
            // Delegate the center getByPartnerId operation to the service
            return centerService.getByPartnerId(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Center(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

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

    @Override
    public ResponseEntity<Center> getCenter(Integer id) {
        try {
            // Delegate the center getCenter operation to the service
            return centerService.getCenter(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Center(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Center>> getByCategoryId(Integer id) {
        try {
            // Delegate the center getByCategoryId operation to the service
            return centerService.getByCategoryId(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Center>> getByStatus(String status) {
        try {
            // Delegate the center getByStatus operation to the service
            return centerService.getByStatus(status);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

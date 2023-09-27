package com.berliz.restImplement;

import com.berliz.constants.BerlizConstants;
import com.berliz.models.Center;
import com.berliz.models.Store;
import com.berliz.rest.StoreRest;
import com.berliz.services.StoreService;
import com.berliz.utils.BerlizUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * REST API endpoints implementation for store-related operations.
 */
@RestController
public class StoreRestImplement implements StoreRest {

    @Autowired
    StoreService storeService;

    @Override
    public ResponseEntity<String> addStore(Map<String, String> requestMap) {
        try {
            // Delegate the store addStore operation to the service
            return storeService.addStore(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Store>> getAllStores() {
        try {
            // Delegate the store getAllStores operation to the service
            return storeService.getAllStores();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateStore(Map<String, String> requestMap) {
        try {
            // Delegate the store updateStore operation to the service
            return storeService.updateStore(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updatePartnerId(Integer id, Integer newId) {
        try {
            // Delegate the store updatePartnerId operation to the service
            return storeService.updatePartnerId(id, newId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteStore(Integer id) {
        try {
            // Delegate the store deleteStore operation to the service
            return storeService.deleteStore(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateStatus(Integer id) {
        try {
            // Delegate the store updateStatus operation to the service
            return storeService.updateStatus(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @Override
    public ResponseEntity<Store> getByPartnerId(Integer id) {
        try {
            // Delegate the store getByPartnerId operation to the service
            return storeService.getByPartnerId(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Store(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Store> getStore(Integer id) {
        try {
            // Delegate the store getStore operation to the service
            return storeService.getStore(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Store(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Store>> getByCategoryId(Integer id) {
        try {
            // Delegate the store getByCategoryId operation to the service
            return storeService.getByCategoryId(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Store>> getByStatus(String status) {
        try {
            // Delegate the store getByStatus operation to the service
            return storeService.getByStatus(status);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Store> getByUserId(Integer id) {
        try {
            // Delegate the store getByUserId operation to the service
            return storeService.getByUserId(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Store(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

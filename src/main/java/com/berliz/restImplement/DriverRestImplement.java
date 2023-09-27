package com.berliz.restImplement;

import com.berliz.constants.BerlizConstants;
import com.berliz.models.Driver;
import com.berliz.rest.DriverRest;
import com.berliz.services.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class DriverRestImplement implements DriverRest {

    @Autowired
    DriverService driverService;

    @Override
    public ResponseEntity<String> addDriver(Map<String, String> requestMap) {
        try {
            return driverService.addDriver(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Driver>> getAllDrivers() {
        try {
            return driverService.getAllDrivers();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateDriver(Map<String, String> requestMap) {
        try {
            return driverService.updateDriver(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updatePartnerId(Integer id, Integer newId) {
        try {
            return driverService.updatePartnerId(id, newId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteDriver(Integer id) {
        try {
            return driverService.deleteDriver(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateStatus(Integer id) {
        try {
            return driverService.updateStatus(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Driver> getByPartnerId(Integer id) {
        try {
            return driverService.getByPartnerId(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Driver(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Driver> getDriver(Integer id) {
        try {
            return driverService.getDriver(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Driver(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Driver>> getByStatus(String status) {
        try {
            return driverService.getByStatus(status);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Driver> getByUserId(Integer id) {
        try {
            return driverService.getByUserId(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Driver(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}

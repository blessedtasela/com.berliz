package com.berliz.services;

import com.berliz.models.Driver;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface DriverService {
    ResponseEntity<String> addDriver(Map<String, String> requestMap);

    ResponseEntity<List<Driver>> getAllDrivers();

    ResponseEntity<String> updateDriver(Map<String, String> requestMap);

    ResponseEntity<String> updatePartnerId(Integer id, Integer newId);

    ResponseEntity<String> deleteDriver(Integer id);

    ResponseEntity<String> updateStatus(Integer id);

    ResponseEntity<Driver> getByPartnerId(Integer id);

    ResponseEntity<Driver> getDriver(Integer id);

    ResponseEntity<List<Driver>> getByStatus(String status);

    ResponseEntity<Driver> getByUserId(Integer id);
}

package com.berliz.rest;

import com.berliz.models.Driver;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping(path = "/driver")
public interface DriverRest {

    @PostMapping(path = "/add")
    ResponseEntity<String> addDriver(@RequestBody Map<String, String> requestMap);

    @GetMapping(path = "/get")
    ResponseEntity<List<Driver>> getAllDrivers();

    @PutMapping(path = "/update")
    ResponseEntity<String> updateDriver(@RequestBody Map<String, String> requestMap);

    @PutMapping(path = "/updatePartnerId/{id}/{newId}")
    ResponseEntity<String> updatePartnerId(@PathVariable Integer id, @PathVariable Integer newId);

    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteDriver(@PathVariable Integer id);

    @PutMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@PathVariable Integer id);

    @GetMapping(path = "/getByPartnerId/{id}")
    ResponseEntity<Driver> getByPartnerId(@PathVariable Integer id);

    @GetMapping(path = "/getDriver/{id}")
    ResponseEntity<Driver> getDriver(@PathVariable Integer id);

    @GetMapping(path = "/getByStatus/{status}")
    ResponseEntity<List<Driver>> getByStatus(@PathVariable String status);

    /**
     * Get a center by its user ID.
     *
     * @param id The user ID associated with the center.
     * @return ResponseEntity containing the center with the specified user ID.
     */
    @GetMapping(path = "/getByUserId/{id}")
    ResponseEntity<Driver> getByUserId(@PathVariable Integer id);

}

package com.berliz.rest;

import com.berliz.models.Brand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping(path = "/brand")
public interface BrandRest {

    @PostMapping(path = "/add")
    ResponseEntity<String> addBrand(@RequestBody(required = true) Map<String, String> requestMap);

    @GetMapping(path = "/get")
    ResponseEntity<List<Brand>> getAllBrands(@RequestParam(required = false) String filterValue);

    @PutMapping(path = "/update")
    ResponseEntity<String> updateBrand(@RequestBody(required = true) Map<String, String> requestMap);

    @PutMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@PathVariable Integer id);

    @GetMapping(path = "/getBrand/{id}")
    ResponseEntity<?> getBrand(@PathVariable Integer id);

    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteBrand(@PathVariable Integer id);
}

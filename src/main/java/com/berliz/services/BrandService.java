package com.berliz.services;

import com.berliz.models.Brand;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface BrandService {
    ResponseEntity<String> addBrand(Map<String, String> requestMap);

    ResponseEntity<List<Brand>> getAllBrands(String filterValue);

    ResponseEntity<String> updateBrand(Map<String, String> requestMap);

    ResponseEntity<String> deleteBrand(Integer id);

    ResponseEntity<String> updateStatus(Integer id);

    ResponseEntity<?> getBrand(Integer id);
}

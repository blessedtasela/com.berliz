package com.berliz.services;

import com.berliz.models.Product;
import com.berliz.wrapper.ProductWrapper;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface ProductService {
    ResponseEntity<String> addProduct(Map<String, String> requestMap);

    ResponseEntity<List<Product>> getAllProducts();

    ResponseEntity<String> updateProduct(Map<String, String> requestMap);

    ResponseEntity<String> deleteProduct(Integer id);

    ResponseEntity<String> updateStatus(Integer id);

    ResponseEntity<Product> getProduct(Integer id);

    ResponseEntity<List<Product>> getByBrand(Integer id);

    ResponseEntity<List<Product>> getByStore(Integer id);

    ResponseEntity<List<Product>> getByStatus(String status);
}

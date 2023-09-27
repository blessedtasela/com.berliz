package com.berliz.rest;


import com.berliz.models.Product;
import com.berliz.wrapper.ProductWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping(path = "/product")
public interface ProductRest {

    @PostMapping(path = "/add")
    ResponseEntity<String> addProduct(@RequestBody Map<String, String> requestMap);

    @GetMapping(path = "/get")
    ResponseEntity<List<Product>> getAllProducts();

    @PutMapping(path = "/update")
    ResponseEntity<String> updateProduct(@RequestBody Map<String, String> requestMap);

    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteProduct(@PathVariable Integer id);

    @PutMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@PathVariable Integer id);

    @GetMapping(path = "/getByStatus/{status}")
    ResponseEntity<List<Product>> getByStatus(@PathVariable String status);


    @GetMapping(path = "/getProduct/{id}")
    ResponseEntity<Product> getProduct(@PathVariable Integer id);

    @GetMapping(path = "/getByBrand/{id}")
    ResponseEntity<List<Product>> getByBrand(@PathVariable Integer id);

    @GetMapping(path = "/getByStore/{id}")
    ResponseEntity<List<Product>> getByStore(@PathVariable Integer id);

}


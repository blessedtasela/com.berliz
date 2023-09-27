package com.berliz.rest;

import com.berliz.models.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping(path = "/order")
public interface OrderRest {

    @PostMapping(path = "/add")
    ResponseEntity<String> addOrder(@RequestBody Map<String, Object> requestMap);

    @GetMapping(path = "/get")
    ResponseEntity<List<Order>> getAllOrders();

    @PutMapping(path = "/update")
    ResponseEntity<String> updateOrder(@RequestBody Map<String, Object> requestMap);

    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteOrder(@PathVariable Integer id);

    @PutMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@PathVariable Integer id);

    @GetMapping(path = "/getByUser/{id}")
    ResponseEntity<List<Order>> getByUser(@PathVariable Integer id);

    @GetMapping(path = "/getOrder/{id}")
    ResponseEntity<Order> getOrder(@PathVariable Integer id);

    @GetMapping(path = "/getByStatus/{status}")
    ResponseEntity<List<Order>> getByStatus(@PathVariable String status);

    @GetMapping(path = "/generateBill/{orderId}")
    ResponseEntity<String> generateBill(@PathVariable Integer orderId);

}

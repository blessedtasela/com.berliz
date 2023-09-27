package com.berliz.rest;

import com.berliz.models.Bill;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(path = "/bill")
public interface BillRest {

    @PostMapping(name = "generateBill/{id}/{uuid}")
    ResponseEntity<String> generateBill(@PathVariable Integer id, @PathVariable String uuid);

    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteBill(@PathVariable Integer id);

    @PutMapping(path = "/getByUserId/{id}")
    ResponseEntity<List<Bill>> getByUserId(@PathVariable Integer id);

    @GetMapping(path = "/getByOrderId/{id}")
    ResponseEntity<Bill> getByOrderId(@PathVariable Integer id);

    @GetMapping(path = "/getBill/{id}")
    ResponseEntity<Bill> getBill(@PathVariable Integer id);
}

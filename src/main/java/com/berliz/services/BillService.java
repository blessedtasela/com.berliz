package com.berliz.services;

import com.berliz.models.Bill;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface BillService {
    ResponseEntity<String> generateBill(Integer id, String uuid);

    ResponseEntity<String> deleteBill(Integer id);

    ResponseEntity<List<Bill>> getByUserId(Integer id);

    ResponseEntity<Bill> getByOrderId(Integer id);

    ResponseEntity<Bill> getBill(Integer id);
}

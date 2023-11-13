package com.berliz.services;

import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface DashboardService {


    ResponseEntity<Map<String, Object>> getDetails();

    ResponseEntity<Map<String, Object>> getBerlizData();

    ResponseEntity<String> getPartnerDetails();
}

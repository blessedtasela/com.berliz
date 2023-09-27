package com.berliz.restImplement;

import com.berliz.constants.BerlizConstants;
import com.berliz.rest.DashboardRest;
import com.berliz.services.DashboardService;
import com.berliz.utils.BerlizUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DashboardRestImplement implements DashboardRest {

    @Autowired
    DashboardService dashboardService;

    @Override
    public ResponseEntity<Map<String, Object>> getDetails() {
        return dashboardService.getDetails();
    }

    @Override
    public ResponseEntity<Map<String, Object>> getBerlizData() {
        return dashboardService.getBerlizData();
    }
}

package com.berliz.serviceImplement;

import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.repository.*;
import com.berliz.services.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class DashboardServiceImplement implements DashboardService {

    @Autowired
    UserRepo userRepo;

    @Autowired
    PartnerRepo partnerRepo;

    @Autowired
    CategoryRepo categoryRepo;

    @Autowired
    TagRepo tagRepo;

    @Autowired
    OrderRepo orderRepo;

    @Autowired
    CenterRepo centerRepo;

    @Autowired
    DriverRepo driverRepo;

    @Autowired
    StoreRepo storeRepo;

    @Autowired
    TrainerRepo trainerRepo;

    @Autowired
    ProductRepo productRepo;

    @Autowired
    NewsletterRepo newsletterRepo;

    @Autowired
    ContactUsRepo contactUsRepo;

    @Autowired
    JWTFilter jwtFilter;

    @Override
    public ResponseEntity<Map<String, Object>> getDetails() {
        try {
            Integer userOrdersCount = orderRepo.countOrdersByEmail(jwtFilter.getCurrentUser());
            Integer partnerApplicationCount = partnerRepo.countPartnerByEmail(jwtFilter.getCurrentUser());
            Map<String, Object> map = new HashMap<>();
            if (jwtFilter.isAdmin()) {
                map.put("users", userRepo.count());
                map.put("partners", partnerRepo.count());
                map.put("products", productRepo.count());
                map.put("categories", categoryRepo.count());
                map.put("tags", tagRepo.count());
                map.put("orders", orderRepo.count());
                map.put("stores", storeRepo.count());
                map.put("trainers", trainerRepo.count());
                map.put("drivers", driverRepo.count());
                map.put("centers", centerRepo.count());
                map.put("newsletters", newsletterRepo.count());
                map.put("contact-us", contactUsRepo.count());

                return new ResponseEntity<>(map, HttpStatus.OK);
            } else if (jwtFilter.isUser()) {
                map.put("my-orders", userOrdersCount);
                map.put("partnership", partnerApplicationCount);

                return new ResponseEntity<>(map, HttpStatus.OK);
            } else if (jwtFilter.isTrainer()){
                map.put("my-orders", userOrdersCount);
                map.put("partnership", partnerApplicationCount);

                return new ResponseEntity<>(map, HttpStatus.OK);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Map<String, Object>> getBerlizData() {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("users", userRepo.count());
            map.put("partners", partnerRepo.count());
            map.put("products", productRepo.count());
            map.put("categories", categoryRepo.count());
            map.put("trainers", trainerRepo.count());
            map.put("centers", centerRepo.count());

            return new ResponseEntity<>(map, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

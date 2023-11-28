package com.berliz.serviceImplement;

import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.Center;
import com.berliz.models.Trainer;
import com.berliz.repository.*;
import com.berliz.services.DashboardService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    TodoListRepo todoListRepo;

    @Autowired
    TrainerLikeRepo trainerLikeRepo;

    @Autowired
    MuscleGroupRepo muscleGroupRepo;

    @Autowired
    ExerciseRepo exerciseRepo;

    @Autowired
    JWTFilter jwtFilter;

    @Override
    public ResponseEntity<Map<String, Object>> getDetails() {
        try {
            log.info("Inside getDetails");
            Integer userOrdersCount = orderRepo.countOrdersByEmail(jwtFilter.getCurrentUser());
            Integer partnerApplicationCount = partnerRepo.countPartnerByEmail(jwtFilter.getCurrentUser());
            Integer myTodoCount = todoListRepo.countMyTodosByEmail(jwtFilter.getCurrentUser());

            Map<String, Object> map = new HashMap<>();
            if (jwtFilter.isAdmin()) {
                map.put("users", userRepo.count());
                map.put("partners", partnerRepo.count());
                map.put("categories", categoryRepo.count());
                map.put("tags", tagRepo.count());
                map.put("trainers", trainerRepo.count());
                map.put("centers", centerRepo.count());
                map.put("newsletters", newsletterRepo.count());
                map.put("contact-us", contactUsRepo.count());
                map.put("todo-lists", todoListRepo.count());
                map.put("muscle-groups", muscleGroupRepo.count());
                map.put("exercises", exerciseRepo.count());
            }
            else if (jwtFilter.isUser()) {
            }
            else if (jwtFilter.isTrainer()) {
            }
            else if (jwtFilter.isCenter()) {
            }
            map.put("my-orders", userOrdersCount);
            map.put("partnership", partnerApplicationCount);
            map.put("my-todos", myTodoCount);
            return new ResponseEntity<>(map, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Override
    public ResponseEntity<String> getPartnerDetails() {
        try {
            log.info("Inside get partnerDetailsCount");
            Integer userId = jwtFilter.getCurrentUserId();
            Trainer trainer = trainerRepo.findByUserId(userId);
            Center center = centerRepo.findByUserId(userId);
            Integer trainerLikes = trainer.getLikes();
            Integer centerLikes = center.getLikes();
            Integer trainerCenterCount = trainerRepo.countTrainersByUserId(userId);
            Integer centerTrainerCount = centerRepo.countCentersUserById(userId);

            Map<String, Object> map = new HashMap<>();
            map.put("trainerLikes", trainerLikes);
            map.put("centerLikes", centerLikes);
            map.put("trainerCenterCount", trainerCenterCount);
            map.put("centerTrainerCount", centerTrainerCount);

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(map);

            return new ResponseEntity<>(json, HttpStatus.OK);
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

package com.berliz.serviceImplement;

import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.Center;
import com.berliz.models.Trainer;
import com.berliz.repositories.*;
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
    TrainerPricingRepo trainerPricingRepo;
    @Autowired
    MuscleGroupRepo muscleGroupRepo;

    @Autowired
    ExerciseRepo exerciseRepo;

    @Autowired
    TestimonialRepo testimonialRepo;

    @Autowired
    SubscriptionRepo subscriptionRepo;

    @Autowired
    PaymentRepo paymentRepo;

    @Autowired
    TaskRepo taskRepo;

    @Autowired
    SubTaskRepo subTaskRepo;

    @Autowired
    MemberRepo memberRepo;

    @Autowired
    ClientRepo clientRepo;

    @Autowired
    JWTFilter jwtFilter;

    @Override
    public ResponseEntity<Map<String, Object>> getDetails() {
        try {
            log.info("Inside getDetails");
            Integer userOrdersCount = orderRepo.countOrdersByEmail(jwtFilter.getCurrentUserEmail());
            Integer partnerApplicationCount = partnerRepo.countPartnerByEmail(jwtFilter.getCurrentUserEmail());
            Integer myTodoCount = todoListRepo.countMyTodosByEmail(jwtFilter.getCurrentUserEmail());
            Integer clientTaskCount = taskRepo.countClientTasksByEmail(jwtFilter.getCurrentUserEmail());
            Integer trainerTaskCount = taskRepo.countTrainerTasksByEmail(jwtFilter.getCurrentUserEmail());
            Integer clientSubscriptionCount = subscriptionRepo.countClientSubscriptionsByEmail(jwtFilter.getCurrentUserEmail());
            Integer trainerClientCount = clientRepo.countTrainerClientsByEmail(jwtFilter.getCurrentUserEmail());
            Integer centerMemberCount = memberRepo.countCenterMembersByEmail(jwtFilter.getCurrentUserEmail());
            Integer memberSubscriptionCount = subscriptionRepo.countMemberSubscriptionsByEmail(jwtFilter.getCurrentUserEmail());
            Integer userTestimonialCount = testimonialRepo.countUserTestimonialsByEmail(jwtFilter.getCurrentUserEmail());
            Integer centerTestimonialCount = testimonialRepo.countCenterTestimonialsByEmail(jwtFilter.getCurrentUserEmail());

            Map<String, Object> map = new HashMap<>();
            if (jwtFilter.isAdmin()) {
                map.put("users", userRepo.count());
                map.put("partners", partnerRepo.count());
                map.put("todo-lists", todoListRepo.count());
                map.put("exercises", exerciseRepo.count());
                map.put("clients", clientRepo.count());
                map.put("members", memberRepo.count());
                map.put("categories", categoryRepo.count());
                map.put("tags", tagRepo.count());
                map.put("trainers", trainerRepo.count());
                map.put("centers", centerRepo.count());
                map.put("newsletters", newsletterRepo.count());
                map.put("contact-us", contactUsRepo.count());
                map.put("testimonials", testimonialRepo.count());
                map.put("muscle-groups", muscleGroupRepo.count());
                map.put("payments", paymentRepo.count());
                map.put("subscriptions", subscriptionRepo.count());
                map.put("tasks", taskRepo.count());
                map.put("sub-tasks", subTaskRepo.count());
                map.put("trainer-pricing", trainerPricingRepo.count());
            }
            else if (jwtFilter.isUser()) {
            }
            else if (jwtFilter.isTrainer()) {
                map.put("clients-tasks", trainerTaskCount);
                map.put("my-clients", trainerClientCount);
            }
            else if (jwtFilter.isCenter()) {
                map.put("my-members", centerMemberCount);
                map.put("members-testimonial", centerTestimonialCount);
            }
            else if (jwtFilter.isMember()) {
                map.put("my-subscriptions", memberSubscriptionCount);
            }
            else if(jwtFilter.isClient()){
                map.put("my-tasks", clientTaskCount);
                map.put("my-subscriptions", clientSubscriptionCount);
            }
            map.put("my-testimonials", userTestimonialCount);
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

    @Override
    public ResponseEntity<Map<String, Object>> getProfileData() {
        try {
            Integer myTodoCount = todoListRepo.countMyTodosByEmail(jwtFilter.getCurrentUserEmail());
            Integer clientTaskCount = taskRepo.countClientTasksByEmail(jwtFilter.getCurrentUserEmail());
            Integer mySubscriptionCount = subscriptionRepo.countMemberSubscriptionsByEmail(jwtFilter.getCurrentUserEmail());

            Map<String, Object> map = new HashMap<>();
            map.put("tasks", clientTaskCount);
            map.put("subscriptions", mySubscriptionCount);
            map.put("todos", myTodoCount);

            return new ResponseEntity<>(map, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

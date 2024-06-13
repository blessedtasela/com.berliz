package com.berliz.serviceImplement;

import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.Brand;
import com.berliz.models.User;
import com.berliz.repositories.BrandRepo;
import com.berliz.repositories.UserRepo;
import com.berliz.services.BrandService;
import com.berliz.utils.BerlizUtilities;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class BrandServiceImplement implements BrandService {

    @Autowired
    JWTFilter jwtFilter;

    @Autowired
    BrandRepo brandRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public ResponseEntity<String> addBrand(Map<String, String> requestMap) {
        log.info("Inside addCategory {}", requestMap);
        try {
            if (jwtFilter.isAdmin()) {
                if (validateBrandMap(requestMap, false)) {
                    Brand brand = brandRepo.findByName(requestMap.get("name"));
                    if (brand == null) {
                        User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
                        getBrandFromMap(requestMap, user);
                        return BerlizUtilities.getResponseEntity("Brand added successfully", HttpStatus.OK);
                    } else {
                        return BerlizUtilities.getResponseEntity("Brand exists", HttpStatus.BAD_REQUEST);
                    }
                }
            } else {
                return BerlizUtilities.getResponseEntity(BerlizConstants.UNAUTHORIZED_REQUEST, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Override
    public ResponseEntity<List<Brand>> getAllBrands(String filterValue) {
        try {
            if (!Strings.isNullOrEmpty(filterValue) && filterValue.equalsIgnoreCase("true")) {
                log.info("inside if block for filterValue{}", filterValue);
                return new ResponseEntity<>(brandRepo.getAllBrands(), HttpStatus.OK);
            }
            return new ResponseEntity<>(brandRepo.findAll(), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateBrand(Map<String, String> requestMap) {
        try {
            log.info("inside updateBrand {}", requestMap);
            if (jwtFilter.isAdmin()) {
                boolean isValid = validateBrandMap(requestMap, true);
                log.info("Is request valid? {}", isValid);
                if (isValid) {
                    Optional<Brand> optional = brandRepo.findById(Integer.parseInt(requestMap.get("id")));
                    if (optional.isPresent()) {
                        User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
                        log.info("inside optional {}", requestMap);
                        brandRepo.updateBrand(
                                requestMap.get("name"),
                                requestMap.get("description"),
                                Float.parseFloat(requestMap.get("ratings")),
                                Integer.parseInt(requestMap.get("id"))
                        );
                        String adminNotificationMessage = "Brand with id: " + optional.get().getId() +
                                ", has been updated";
                        String notificationMessage = "YBrand information has been updated : " + optional.get().getId();
                        jwtFilter.sendNotifications("/topic/updateBrand", adminNotificationMessage,
                                user, notificationMessage, optional.get());
                        return BerlizUtilities.getResponseEntity("Brand updated successfully", HttpStatus.OK);
                    } else {
                        return BerlizUtilities.getResponseEntity("Brand id not found", HttpStatus.BAD_REQUEST);
                    }
                } else {
                    return BerlizUtilities.getResponseEntity(BerlizConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
                }
            } else {
                return BerlizUtilities.getResponseEntity(BerlizConstants.UNAUTHORIZED_REQUEST, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteBrand(Integer id) {
        try {
            log.info("inside deleteCategory {}", id);
            if (jwtFilter.isAdmin()) {
                Optional<Brand> optional = brandRepo.findById(id);
                if (optional.isPresent()) {
                    User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
                    log.info("inside optional {}", id);
                    brandRepo.deleteById(id);
                    String adminNotificationMessage = "Brand with id: " + optional.get().getId() + ", and name "
                            + optional.get().getName() + ", has been deleted";
                    String notificationMessage = "You have successfully deleted brand : " + optional.get().getName();
                    jwtFilter.sendNotifications("/topic/deleteBrand", adminNotificationMessage,
                            user, notificationMessage, optional.get());
                    return BerlizUtilities.getResponseEntity("Brand deleted successfully", HttpStatus.OK);
                } else {
                    return BerlizUtilities.getResponseEntity("Brand id not found", HttpStatus.BAD_REQUEST);
                }
            } else {
                return BerlizUtilities.getResponseEntity(BerlizConstants.UNAUTHORIZED_REQUEST, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateStatus(Integer id) {
        try {
            log.info("Inside updateStatus {}", id);
            String status;
            if (!jwtFilter.isAdmin()) {
                return BerlizUtilities.getResponseEntity(BerlizConstants.UNAUTHORIZED_REQUEST, HttpStatus.UNAUTHORIZED);
            }
            Optional<Brand> optional = brandRepo.findById(id);
            if (optional.isEmpty()) {
                return BerlizUtilities.getResponseEntity("Brand id not found", HttpStatus.BAD_REQUEST);
            }
            log.info("Inside optional {}", optional);
            status = optional.get().getStatus();
            Brand brand = optional.get();
            User user = userRepo.findByEmail(jwtFilter.getCurrentUserEmail());
            if (status.equalsIgnoreCase("true")) {
                status = "false";
                brandRepo.updateStatus(id, status);
                String adminNotificationMessage = "Brand with id: " + brand.getId() +
                        ", account status has been set to " + status;
                String notificationMessage = "You have successfully set your brand status to : " + status;
                jwtFilter.sendNotifications("/topic/updateUserStatus", adminNotificationMessage,
                        user, notificationMessage, brand);
                return BerlizUtilities.getResponseEntity("Brand Status updated successfully. Now DISABLED", HttpStatus.OK);
            } else {
                status = "true";
                brandRepo.updateStatus(id, status);
                String adminNotificationMessage = "Brand with id: " + brand.getId() +
                        ", account status has been set to " + status;
                String notificationMessage = "You have successfully set your brand status to : " + status;
                jwtFilter.sendNotifications("/topic/updateUserStatus", adminNotificationMessage,
                        user, notificationMessage, brand);
                return BerlizUtilities.getResponseEntity("Brand Status updated successfully. Now ACTIVE", HttpStatus.OK);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> getBrand(Integer id) {
        try {
            log.info("Inside getBrand {}", id);
            Optional<Brand> optional = brandRepo.findById(id);
            if (optional.isPresent()) {
                return ResponseEntity.ok(optional);
            } else {
                return ResponseEntity.badRequest().body("Brand id not found");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
    }

    private boolean validateBrandMap(Map<String, String> requestMap, boolean validId) {
        if (validId) {
            return requestMap.containsKey("id")
                    && requestMap.containsKey("name")
                    && requestMap.containsKey("description")
                    && requestMap.containsKey("ratings");
        } else {
            return requestMap.containsKey("name")
                    && requestMap.containsKey("description")
                    && requestMap.containsKey("ratings");
        }
    }

    private void getBrandFromMap(Map<String, String> requestMap, User user) {
        Brand brand = new Brand();
        Date currentDate = new Date();
        brand.setName(requestMap.get("name"));
        brand.setDescription(requestMap.get("description"));
        brand.setRatings(Float.parseFloat(requestMap.get("ratings")));
        brand.setStatus("true");
        brand.setLastUpdate(currentDate);
        brand.setDate(currentDate);
        Brand savedBrand = brandRepo.save(brand);
        String adminNotificationMessage = "A new brand with id: " + savedBrand.getId() + " and description: "
                + savedBrand.getDescription() + ", has been added for " + savedBrand.getName();
        String notificationMessage = "You have successfully added a new brand: " + savedBrand.getName();
        jwtFilter.sendNotifications("/topic/getBrandFromMap", adminNotificationMessage,
                user, notificationMessage, savedBrand);
    }
}

package com.berliz.serviceImplement;

import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.Category;
import com.berliz.models.Center;
import com.berliz.models.Partner;
import com.berliz.models.User;
import com.berliz.repository.CategoryRepo;
import com.berliz.repository.CenterRepo;
import com.berliz.repository.PartnerRepo;
import com.berliz.repository.UserRepo;
import com.berliz.services.CenterService;
import com.berliz.utils.BerlizUtilities;
import com.berliz.utils.EmailUtilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class CenterServiceImplement implements CenterService {

    @Autowired
    JWTFilter jwtFilter;

    @Autowired
    CenterRepo centerRepo;

    @Autowired
    PartnerRepo partnerRepo;

    @Autowired
    CategoryRepo categoryRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    EmailUtilities emailUtilities;

    /**
     * adds a center based on data provided
     *
     * @param requestMap The request body containing data
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<String> addCenter(Map<String, String> requestMap) {
        try {
            log.info("Inside addCenter {}", requestMap);
            boolean validRequest = validateCenterFromMap(requestMap, false);
            log.info("Is request valid? {}", validRequest);

            if (!validRequest) {
                return BerlizUtilities.getResponseEntity(BerlizConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }

            // Handle center addition based on user role
            if (jwtFilter.isAdmin()) {
                // Admins have additional checks when adding a center
                return handleCenterAdditionByAdmin(requestMap);
            } else {
                // Users without admin rights add centers with different criteria
                return handleCenterAdditionByUser(requestMap);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Return an error response if an exception occurred
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    /**
     * Updates a center based on data provided
     *
     * @param requestMap The request body containing data
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<String> updateCenter(Map<String, String> requestMap) {
        try {
            log.info("Inside updateCenter {}", requestMap);

            // Validate the incoming request
            boolean isValid = validateCenterFromMap(requestMap, true);
            log.info("Is request valid? {}", isValid);

            if (!isValid) {
                return BerlizUtilities.getResponseEntity(BerlizConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }

            Center validCenter = centerRepo.findByCenterId(Integer.valueOf(requestMap.get("id")));

            // Check if the center exists
            if (validCenter == null) {
                return BerlizUtilities.getResponseEntity("Center id not found", HttpStatus.BAD_REQUEST);
            }

            // Check user permissions and update center
            if (jwtFilter.isAdmin() || (jwtFilter.isCenter()
                    && jwtFilter.getCurrentUserId().equals(validCenter.getPartner().getUser().getId())
                    && validCenter.getStatus().equalsIgnoreCase("true"))) {
                updateCenterFromMap(requestMap);
                return BerlizUtilities.getResponseEntity("Center updated successfully", HttpStatus.OK);
            } else {
                return BerlizUtilities.getResponseEntity(BerlizConstants.UNAUTHORIZED_REQUEST, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    /**
     * Returns a list of centers
     *
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<List<Center>> getAllCenters() {
        try {
            log.info("Inside getAllCenters");

            // Check if the user is an admin
            if (jwtFilter.isAdmin()) {
                // Retrieve all centers from the repository
                List<Center> centers = centerRepo.findAll();
                return new ResponseEntity<>(centers, HttpStatus.OK);
            } else {
                // Return an unauthorized response for non-admin users
                return new ResponseEntity(BerlizConstants.UNAUTHORIZED_REQUEST, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Return an error response if an exception occurred
        return new ResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    /**
     * Updates a center partner ID based on the existing id and new id provided
     *
     * @param id    The existing partner id to be replaced.
     * @param newId The new partner id.
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<String> updatePartnerId(Integer id, Integer newId) {
        try {
            log.info("Inside updateCenterPartnerId {}", id);

            // Check if the user is an admin
            if (jwtFilter.isAdmin()) {
                // Retrieve the center with the given id
                Optional<Center> optional = centerRepo.findById(id);
                if (optional.isPresent()) {
                    log.info("Inside optional {}", optional);
                    Center center = optional.get();

                    // Check if the new partner id exists
                    Partner newPartner = partnerRepo.findById(newId).orElse(null);
                    if (newPartner == null) {
                        return BerlizUtilities.getResponseEntity("Invalid new partner id", HttpStatus.BAD_REQUEST);
                    }

                    // Check if the new partner id exists in the driver
                    Center partnerCenter = centerRepo.findById(newId).orElse(null);
                    if (partnerCenter != null) {
                        return BerlizUtilities.getResponseEntity("Partner id exists in driver", HttpStatus.BAD_REQUEST);
                    }

                    //Check if the new partner id is a valid user - i.e. it is active
                    String newPartnerStatus = newPartner.getUser().getStatus();
                    if (!newPartnerStatus.equalsIgnoreCase("true")) {
                        return BerlizUtilities.getResponseEntity("new partnerId must be approved by admin", HttpStatus.BAD_REQUEST);
                    }

                    //Check if the new partner id has a valid user role
                    String newPartnerRole = newPartner.getUser().getRole();
                    if (!newPartnerRole.equalsIgnoreCase("user")) {
                        return BerlizUtilities.getResponseEntity("new partnerId must have user role", HttpStatus.BAD_REQUEST);
                    }

                    // Check if the center status is false before updating partner id
                    if (center.getStatus().equalsIgnoreCase("false")) {
                        // Update the center's partner id
                        centerRepo.updatePartnerId(id, newId);
                        return BerlizUtilities.getResponseEntity("Center - partner id updated successfully. New id: " + newId, HttpStatus.OK);
                    } else {
                        return BerlizUtilities.getResponseEntity("Center status must be false to update partner id", HttpStatus.BAD_REQUEST);
                    }
                } else {
                    return BerlizUtilities.getResponseEntity("Center with id " + id + " not found", HttpStatus.BAD_REQUEST);
                }
            } else {
                return BerlizUtilities.getResponseEntity(BerlizConstants.UNAUTHORIZED_REQUEST, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes a center based on the provided center ID.
     *
     * @param id The ID of the center to be deleted
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<String> deleteCenter(Integer id) {
        try {
            // Check if the current user is an admin
            if (jwtFilter.isAdmin()) {
                // Retrieve the center by its ID
                Center center = centerRepo.findByCenterId(id);
                if (center != null) {
                    // Delete the retrieved center from the repository
                    centerRepo.delete(center);
                    return BerlizUtilities.getResponseEntity("Center deleted successfully", HttpStatus.OK);
                } else {
                    // Center with the provided ID was not found
                    return BerlizUtilities.getResponseEntity("Center id not found", HttpStatus.BAD_REQUEST);
                }
            } else {
                // Unauthorized access, user is not an admin
                return BerlizUtilities.getResponseEntity(BerlizConstants.UNAUTHORIZED_REQUEST, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // Internal server error occurred
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Updates a center status based on the provided center ID.
     *
     * @param id The ID of the center status to be updated
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<String> updateStatus(Integer id) {
        try {
            log.info("Inside updateStatus {}", id);

            // Retrieve the current user's ID
            Integer userId = jwtFilter.getCurrentUserId();

            // Retrieve the Center entity by ID
            Optional<Center> optional = centerRepo.findById(id);

            // Check if the Center exists
            if (optional.isPresent()) {
                log.info("Inside optional {}", optional);

                // Retrieve the ID and status of the user associated with the Center
                Integer validUser = optional.get().getPartner().getUser().getId();
                String validUserStatus = optional.get().getStatus();

                // Check if the user is an admin or the associated partner
                if (jwtFilter.isAdmin() || (validUser.equals(userId) && validUserStatus.equalsIgnoreCase("true"))) {
                    log.info("Is valid user? Admin: {}, ValidUser: {}, CurrentUser: {}", jwtFilter.isAdmin(), validUser, userId);

                    // Get the current status of the Center
                    String status = optional.get().getStatus();
                    String userEmail = optional.get().getPartner().getUser().getEmail();

                    // Toggle the status
                    status = status.equalsIgnoreCase("true") ? "false" : "true";

                    // Update the status in the repository
                    centerRepo.updateStatus(id, status);

                    // Update user role in user repository
                    if (optional.get().getPartner().getUser().getRole().equalsIgnoreCase("user") && status.equalsIgnoreCase("true")) {
                        userRepo.updateUserRole("center", validUser);
                    } else {
                        userRepo.updateUserRole("user", validUser);
                    }

                    // Send status update emails
                    emailUtilities.sendStatusMailToAdmins(status, userEmail, userRepo.getAllAdminsMail(), "Center");
                    emailUtilities.sendStatusMailToUser(status,"Center", userEmail);

                    // Return a success response
                    String responseMessage = status.equalsIgnoreCase("true") ?
                            "Center Status updated successfully. NOW ACTIVE" :
                            "Center Status updated successfully. NOW DISABLED";
                    return BerlizUtilities.getResponseEntity(responseMessage, HttpStatus.OK);
                } else {
                    // Return an unauthorized response
                    return BerlizUtilities.getResponseEntity(BerlizConstants.UNAUTHORIZED_REQUEST, HttpStatus.UNAUTHORIZED);
                }
            } else {
                // Return a response when Center ID is not found
                return BerlizUtilities.getResponseEntity("Center id not found", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Return an error response if an exception occurred
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Get a center based on the provided partner ID in the center.
     *
     * @param id The ID of the center to be fetched
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<Center> getByPartnerId(Integer id) {
        try {
            log.info("Inside getByPartnerId");

            // Check if the user is an admin
            if (jwtFilter.isAdmin()) {
                // Find the center by the partner's id
                Center center = centerRepo.findByPartnerId(id);

                if (center == null) {
                    return new ResponseEntity(BerlizConstants.UNAUTHORIZED_REQUEST, HttpStatus.UNAUTHORIZED);
                }

                // Get the actual center id and retrieve the center by that id
                id = center.getId();
                Center existingCenter = centerRepo.findById(id).orElse(null);

                if (existingCenter != null) {
                    // Return the center if found
                    return new ResponseEntity<>(existingCenter, HttpStatus.OK);
                } else {
                    // If center doesn't exist, return NOT_FOUND status
                    return new ResponseEntity("Center not found", HttpStatus.NOT_FOUND);
                }
            } else {
                // Return FORBIDDEN status if the user is not an admin
                return new ResponseEntity("Forbidden to make request", HttpStatus.FORBIDDEN);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            // Return INTERNAL_SERVER_ERROR status if an exception occurs
            return new ResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gets a center based on the provided center ID.
     *
     * @param id The ID of the center to be fetched
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<Center> getCenter(Integer id) {
        try {
            log.info("Inside getCenter");

            // Get the current user's ID
            Integer user = jwtFilter.getCurrentUserId();

            // Retrieve the partner associated with the current user
            Partner partnerByUserId = partnerRepo.findByUserId(user);

            // Retrieve the center by its ID
            Center center = centerRepo.findById(id).orElse(null);

            if (center == null) {
                // Center with the provided ID was not found
                return new ResponseEntity<>(new Center(), HttpStatus.BAD_REQUEST);
            }

            if (jwtFilter.isAdmin()) {
                // User is an admin, return the retrieved center
                return new ResponseEntity<>(center, HttpStatus.OK);
            } else if (partnerByUserId != null) {
                // Check if the logged-in user has a partner and the center matches
                Integer currentUser = partnerByUserId.getUser().getId();

                if (currentUser.equals(user) && center.getPartner().getId().equals(partnerByUserId.getId())) {
                    // Return the retrieved center
                    return new ResponseEntity<>(center, HttpStatus.OK);
                }
            }

            // Unauthorized access, user is not admin and doesn't have a valid center
            return new ResponseEntity<>(new Center(), HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Internal server error occurred
        return new ResponseEntity<>(new Center(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Deletes a center based on the provided category ID.
     *
     * @param id The ID of the centers to be fetched
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<List<Center>> getByCategoryId(Integer id) {
        try {
            log.info("Inside getByCategoryId {}", id);

            // Retrieve centers with the specified category ID
            List<Center> centers = centerRepo.getByCategoryId(id);

            if (!centers.isEmpty()) {
                // Centers with the specified category ID were found, return them
                return new ResponseEntity<>(centers, HttpStatus.OK);
            } else {
                // No centers found with the specified category ID
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Internal server error occurred
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Get a list of centers based on the status provided.
     *
     * @param status The status of the centers to be fetched
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<List<Center>> getByStatus(String status) {
        try {
            log.info("Inside getByStatus");

            // Check if the user is an admin
            if (jwtFilter.isAdmin()) {
                // Retrieve centers with the specified status
                List<Center> centers = centerRepo.findByStatus(status);

                if (centers != null) {
                    // Centers with the specified status were found, return them
                    return new ResponseEntity<>(centers, HttpStatus.OK);
                } else {
                    // No centers found with the specified status
                    return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
                }
            } else {
                // Unauthorized access, user is not an admin
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Internal server error occurred
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Gets a center based on the provided user ID.
     *
     * @param id The ID of the user related to the center to be fetched
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<Center> getByUserId(Integer id) {
        try {
            log.info("Inside getByUserId");

            // Retrieve the user's partner details using the provided user ID
            Partner partner = partnerRepo.findByUserId(id);
            if (partner == null) {
                return new ResponseEntity<>(new Center(), HttpStatus.BAD_REQUEST);
            }

            if (jwtFilter.isAdmin() && partner != null) {
                // Retrieve the center associated with the partner (if exists)
                Center center = centerRepo.findByPartnerId(partner.getId());

                if (center != null) {
                    // Return the retrieved center
                    return new ResponseEntity<>(center, HttpStatus.OK);
                } else {
                    // No center associated with the provided user ID
                    return new ResponseEntity<>(new Center(), HttpStatus.NOT_FOUND);
                }
            } else {
                // Unauthorized access, user is not an admin and doesn't have a valid center
                return new ResponseEntity<>(new Center(), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Internal server error occurred
        return new ResponseEntity<>(new Center(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    /**
     * Validates the data in the request map for center creation or update.
     *
     * @param requestMap The map containing the request data
     * @param validId    Flag indicating whether a valid ID is required for update
     * @return True if the request data is valid, otherwise false
     */
    private boolean validateCenterFromMap(Map<String, String> requestMap, boolean validId) {
        if (validId) {
            // For center update with valid ID, check the presence of all required fields
            return requestMap.containsKey("id")
                    && requestMap.containsKey("name")
                    && requestMap.containsKey("motto")
                    && requestMap.containsKey("address")
                    && requestMap.containsKey("introduction")
                    && requestMap.containsKey("experience")
                    && requestMap.containsKey("location")
                    && requestMap.containsKey("photo")
                    && requestMap.containsKey("likes")
                    && requestMap.containsKey("categoryIds");
        } else {
            // For center creation, check the presence of all required fields except ID
            return requestMap.containsKey("name")
                    && requestMap.containsKey("motto")
                    && requestMap.containsKey("address")
                    && requestMap.containsKey("introduction")
                    && requestMap.containsKey("experience")
                    && requestMap.containsKey("location")
                    && requestMap.containsKey("photo")
                    && requestMap.containsKey("categoryIds");
        }
    }

    /**
     * Constructs a Center object from the provided request map and saves it to the repository.
     *
     * @param requestMap The map containing the request data
     * @param isUser     Flag indicating whether the request is coming from a user
     * @return The constructed and saved Center object
     */
    private Center getCenterFromMap(Map<String, String> requestMap, Boolean isUser) {
        Center center = new Center();
        Partner partner = new Partner();

        // Parse categoryIds as a comma-separated string
        String categoryIdsString = requestMap.get("categoryIds");
        String[] categoryIdsArray = categoryIdsString.split(",");

        Set<Category> categorySet = new HashSet<>();
        for (String categoryIdString : categoryIdsArray) {
            // Remove leading and trailing spaces before parsing
            int categoryId = Integer.parseInt(categoryIdString.trim());

            // Retrieve the optional category by ID
            Optional<Category> optionalCategory = categoryRepo.findById(categoryId);

            if (!optionalCategory.isPresent()) {
                // Handle case where category with the specified ID was not found
                BerlizUtilities.getResponseEntity("Category with ID " + categoryId + " not found", HttpStatus.BAD_REQUEST);
            }

            categorySet.add(optionalCategory.get());
        }
        center.setCategorySet(categorySet);

        // Determine the partner ID based on whether the request is from a user or not
        Integer partnerId;
        if (isUser) {
            Integer userId = jwtFilter.getCurrentUserId();
            Partner partnerByUserId = partnerRepo.findByUserId(userId);
            partnerId = partnerByUserId.getId();
        } else {
            partnerId = Integer.valueOf(requestMap.get("partnerId"));
        }
        partner.setId(partnerId);

        // Populate the Center object with the provided data
        center.setPartner(partner);
        center.setName(requestMap.get("name"));
        center.setMotto(requestMap.get("motto"));
        center.setAddress(requestMap.get("address"));
        center.setIntroduction(requestMap.get("introduction"));
        center.setExperience(requestMap.get("experience"));
        center.setLocation(requestMap.get("location"));
        center.setPhoto(requestMap.get("photo"));
        center.setLikes(0); // Initializing likes
        center.setDate(new Date());
        center.setLastUpdate(new Date());
        center.setStatus("false"); // Initializing status

        // Save the constructed Center object to the repository
        centerRepo.save(center);

        return center;
    }

    /**
     * Updates a Center object from the provided request map and saves it to the repository.
     *
     * @param requestMap The map containing the request data
     * @return The constructed and saved Center object
     */
    private ResponseEntity<String> updateCenterFromMap(Map<String, String> requestMap) {
        try {
            // Get the current user's ID
            Integer currentUser = jwtFilter.getCurrentUserId();

            // Find the center by ID
            Optional<Center> optional = centerRepo.findById(Integer.valueOf(requestMap.get("id")));

            // Check if the center with the given ID exists
            if (optional.isEmpty()) {
                return BerlizUtilities.getResponseEntity("Center id does not exist", HttpStatus.BAD_REQUEST);
            }

            // Retrieve the existing center
            Center existingCenter = optional.get();

            // Check if the user has permission to update the center
            boolean validUser = jwtFilter.isAdmin() || currentUser.equals(existingCenter.getPartner().getUser().getId());

            if (!validUser) {
                return BerlizUtilities.getResponseEntity(BerlizConstants.UNAUTHORIZED_REQUEST, HttpStatus.UNAUTHORIZED);
            }

            // Parse categoryIds as a comma-separated string
            String categoryIdsString = requestMap.get("categoryIds");
            String[] categoryIdsArray = categoryIdsString.split(",");

            Set<Category> categorySet = new HashSet<>();
            for (String categoryIdString : categoryIdsArray) {
                // Remove leading and trailing spaces before parsing
                int categoryId = Integer.parseInt(categoryIdString.trim());

                // Check if the category with the given ID exists in the database
                Optional<Category> optionalCategory = categoryRepo.findById(categoryId);
                if (optionalCategory.isEmpty()) {
                    return BerlizUtilities.getResponseEntity("Category with ID " + categoryId + " not found", HttpStatus.BAD_REQUEST);
                }
                categorySet.add(optionalCategory.get());
            }

            // Update the center properties
            existingCenter.setCategorySet(categorySet);
            existingCenter.setName(requestMap.get("name"));
            existingCenter.setMotto(requestMap.get("motto"));
            existingCenter.setAddress(requestMap.get("address"));
            existingCenter.setIntroduction(requestMap.get("introduction"));
            existingCenter.setLocation(requestMap.get("location"));
            existingCenter.setExperience(requestMap.get("experience"));
            existingCenter.setPhoto(requestMap.get("photo"));
            existingCenter.setLikes(Integer.parseInt(requestMap.get("likes")));
            existingCenter.setLastUpdate(new Date());

            // Save the updated center
            centerRepo.save(existingCenter);

            return BerlizUtilities.getResponseEntity("Center updated successfully", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle use case of adding center by an admin.
     *
     * @param requestMap The map containing the request data
     * @return The constructed and saved Center object
     */
    private ResponseEntity<String> handleCenterAdditionByAdmin(Map<String, String> requestMap) {
        try {
            log.info("Handling center addition by admin");

            // Admin must provide partnerId
            if (!requestMap.containsKey("partnerId")) {
                return new ResponseEntity<>("Admin must provide partnerId", HttpStatus.BAD_REQUEST);
            }

            Integer partnerIdValue = Integer.valueOf(requestMap.get("partnerId"));

            // Check if partnerId already has an associated center
            Center existingCenterByPartnerId = centerRepo.findByPartnerId(partnerIdValue);
            if (existingCenterByPartnerId != null) {
                return BerlizUtilities.getResponseEntity("Partner id already exists", HttpStatus.BAD_REQUEST);
            }

            // Check if the current user is already associated with a center
            Partner partner = partnerRepo.findByPartnerId(partnerIdValue);

            // Return an error message if partner is null
            if (partner == null) {
                return BerlizUtilities.getResponseEntity("Partner id is null", HttpStatus.BAD_REQUEST);
            }

            // Check if the user is associated with trainer
            Integer userId = partner.getUser().getId();
            if (!isUserAssociatedWithCenter(userId)) {
                return BerlizUtilities.getResponseEntity("User is already associated with a center", HttpStatus.BAD_REQUEST);
            }

            // Check if the partner is a center
            if (!isValidRole(partnerIdValue, "center")) {
                return BerlizUtilities.getResponseEntity("Invalid partner role. Partner must be a center", HttpStatus.BAD_REQUEST);
            }

            // Check if the partner already exists
            if (!isValidPartner(partnerIdValue)) {
                return BerlizUtilities.getResponseEntity("Partner does not exist", HttpStatus.BAD_REQUEST);
            }
            // Check if partnerId already been approved by an admin
            if (!isApprovedCenterPartner(partnerIdValue)) {
                return BerlizUtilities.getResponseEntity("Please wait for admin approval", HttpStatus.BAD_REQUEST);
            }

            getCenterFromMap(requestMap, false);
            return new ResponseEntity<>("Center added successfully", HttpStatus.CREATED);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle use case of adding center by a valid user.
     *
     * @param requestMap The map containing the request data
     * @return The constructed and saved Center object
     */
    private ResponseEntity<String> handleCenterAdditionByUser(Map<String, String> requestMap) {
        try {
            log.info("Handling center addition by user");

            // Retrieve the current user's ID
            Integer userId = jwtFilter.getCurrentUserId();

            // Check if the current user is already associated with a center
            Partner partner = partnerRepo.findByUserId(userId);

            // Return an error message if partner is null
            if (partner == null) {
                return BerlizUtilities.getResponseEntity("Partner id is null", HttpStatus.BAD_REQUEST);
            }

            Integer partnerId = partner.getId();
            if (!isUserAssociatedWithCenter(userId)) {
                return BerlizUtilities.getResponseEntity("User is already associated with a center", HttpStatus.BAD_REQUEST);
            }

            // Check if partnerId already has an associated center
            Center existingCenterByPartnerId = centerRepo.findByPartnerId(partnerId);
            if (existingCenterByPartnerId != null) {
                return BerlizUtilities.getResponseEntity("Partner id already exists", HttpStatus.BAD_REQUEST);
            }

            // Check if the partner is a center
            if (!isValidRole(partnerId, "center")) {
                return BerlizUtilities.getResponseEntity("Invalid partner role. Partner must be a center", HttpStatus.BAD_REQUEST);
            }

            // Check if the partner already exists
            if (!isValidPartner(partnerId)) {
                return BerlizUtilities.getResponseEntity("Partner does not exist", HttpStatus.BAD_REQUEST);
            }

            // Check if partnerId already been approved by an admin
            if (!isApprovedCenterPartner(partnerId)) {
                return BerlizUtilities.getResponseEntity("Please wait for admin approval", HttpStatus.BAD_REQUEST);
            }

            // Check if the center name already exists
            if (isCenterNameAlreadyExists(requestMap.get("name"))) {
                return BerlizUtilities.getResponseEntity("Center name already exists", HttpStatus.BAD_REQUEST);
            }

            getCenterFromMap(requestMap, true);
            return new ResponseEntity<>("Center added successfully", HttpStatus.CREATED);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Validates partner.
     *
     * @param partnerId ID of the partner to be validated
     * @return The valid partner
     */
    private boolean isValidPartner(Integer partnerId) {
        Partner partner = partnerRepo.findByPartnerId(partnerId);
        return partner != null;
    }

    /**
     * Validates partner and role .
     *
     * @param partnerId    ID of the partner to be validated
     * @param requiredRole role required of the partner
     * @return The valid partner
     */
    private boolean isValidRole(Integer partnerId, String requiredRole) {
        Partner partner = partnerRepo.findByPartnerId(partnerId);
        return partner != null && partner.getRole().equalsIgnoreCase(requiredRole);
    }

    /**
     * Validates if center is an approved partner.
     *
     * @param partnerId ID of the partner to be approved
     * @return The valid partner
     */
    private boolean isApprovedCenterPartner(Integer partnerId) {
        Partner partner = partnerRepo.findByPartnerId(partnerId);
        return partner != null && partner.getStatus().equalsIgnoreCase("true");
    }

    /**
     * Checks if a user is linked to a center.
     *
     * @param userId ID of the center to be checked
     * @return The valid partner
     */
    private boolean isUserAssociatedWithCenter(Integer userId) {
        Partner currentUserPartner = partnerRepo.findByUserId(userId);
        return Optional.ofNullable(currentUserPartner)
                .map(Partner::getUser)
                .map(User::getId)
                .isPresent();
    }

    /**
     * Checks if a center name already exists.
     *
     * @param centerName name of center to be checked
     * @return The valid partner
     */
    private boolean isCenterNameAlreadyExists(String centerName) {
        Center centerByName = centerRepo.findByName(centerName);
        return centerByName != null;
    }
}

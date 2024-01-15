package com.berliz.serviceImplement;

import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.*;
import com.berliz.repositories.CategoryRepo;
import com.berliz.repositories.PartnerRepo;
import com.berliz.repositories.StoreRepo;
import com.berliz.repositories.UserRepo;
import com.berliz.services.StoreService;
import com.berliz.utils.BerlizUtilities;
import com.berliz.utils.EmailUtilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class StoreServiceImplement implements StoreService {

    @Autowired
    JWTFilter jwtFilter;

    @Autowired
    StoreRepo storeRepo;

    @Autowired
    UserServiceImplement userServiceImplement;

    @Autowired
    CategoryRepo categoryRepo;

    @Autowired
    PartnerRepo partnerRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    EmailUtilities emailUtilities;

    /**
     * adds a Store based on data provided
     *
     * @param requestMap The request body containing data
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<String> addStore(Map<String, String> requestMap) {
        try {
            log.info("Inside addStore {}", requestMap);

            // Validate the incoming request
            boolean validRequest = validateStoreFromMap(requestMap, false);
            log.info("Is request valid? {}", validRequest);

            // Check if the request is valid
            if (!validRequest) {
                return BerlizUtilities.getResponseEntity(BerlizConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }

            // Handle Store addition based on user role
            if (jwtFilter.isAdmin()) {
                // Admins have additional checks when adding a Store
                return handleStoreAdditionByAdmin(requestMap);
            } else {
                // Users without admin rights add Stores with different criteria
                return handleStoreAdditionByUser(requestMap);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Return an error response if an exception occurred
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    /**
     * Updates a Store based on data provided
     *
     * @param requestMap The request body containing data
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<String> updateStore(Map<String, String> requestMap) {
        try {
            log.info("Inside updateStore {}", requestMap);

            // Validate the incoming request
            boolean isValid = validateStoreFromMap(requestMap, true);
            log.info("Is request valid? {}", isValid);

            if (!isValid) {
                return BerlizUtilities.getResponseEntity(BerlizConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }

            Store validStore = storeRepo.findByStoreId(Integer.valueOf(requestMap.get("id")));

            // Check if the Store exists
            if (validStore == null) {
                return BerlizUtilities.getResponseEntity("Store id not found", HttpStatus.BAD_REQUEST);
            }

            // Check user permissions and update Store
            if (jwtFilter.isAdmin() || (jwtFilter.isStore()
                    && jwtFilter.getCurrentUserId().equals(validStore.getPartner().getUser().getId())
                    && validStore.getStatus().equalsIgnoreCase("true"))) {
                updateStoreFromMap(requestMap);
                return BerlizUtilities.getResponseEntity("Store updated successfully", HttpStatus.OK);
            } else {
                return BerlizUtilities.getResponseEntity(BerlizConstants.UNAUTHORIZED_REQUEST, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }



    /**
     * Returns a list of Stores
     *
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<List<Store>> getAllStores() {
        try {
            log.info("Inside getAllStore");

            // Check if the user is an admin
            if (jwtFilter.isAdmin()) {
                // Retrieve all Store from the repository
                List<Store> Store = storeRepo.findAll();
                return new ResponseEntity<>(Store, HttpStatus.OK);
            } else {
                // Return an unauthorized response for non-admin users
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Return an error response if an exception occurred
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Updates a Store partner ID based on the existing id and new id provided
     *
     * @param id    The existing partner id to be replaced.
     * @param newId The new partner id.
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<String> updatePartnerId(Integer id, Integer newId) {
        try {
            log.info("Inside updateStorePartnerId {}", id);

            // Check if the user is an admin
            if (jwtFilter.isAdmin()) {
                // Retrieve the Store with the given id
                Optional<Store> optional = storeRepo.findById(id);
                if (optional.isPresent()) {
                    log.info("Inside optional {}", optional);
                    Store Store = optional.get();

                    // Check if the new partner id exists
                    Partner newPartner = partnerRepo.findById(newId).orElse(null);
                    if (newPartner == null) {
                        return BerlizUtilities.getResponseEntity("Invalid new partner id", HttpStatus.BAD_REQUEST);
                    }

                    // Check if the new partner id exists in the driver
                    Store partnerStore = storeRepo.findById(newId).orElse(null);
                    if (partnerStore != null) {
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

                    // Check if the Store status is false before updating partner id
                    if (Store.getStatus().equalsIgnoreCase("false")) {
                        // Update the Store's partner id
                        storeRepo.updatePartnerId(id, newId);
                        return BerlizUtilities.getResponseEntity("Store - partner id updated successfully. New id: " + newId, HttpStatus.OK);
                    } else {
                        return BerlizUtilities.getResponseEntity("Store status must be false to update partner id", HttpStatus.BAD_REQUEST);
                    }
                } else {
                    return BerlizUtilities.getResponseEntity("Store with id " + id + " not found", HttpStatus.BAD_REQUEST);
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
     * Deletes a Store based on the provided Store ID.
     *
     * @param id The ID of the Store to be deleted
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<String> deleteStore(Integer id) {
        try {
            // Check if the current user is an admin
            if (jwtFilter.isAdmin()) {
                // Retrieve the Store by its ID
                Store Store = storeRepo.findByStoreId(id);
                if (Store != null) {
                    // Delete the retrieved Store from the repository
                    storeRepo.delete(Store);
                    return BerlizUtilities.getResponseEntity("Store deleted successfully", HttpStatus.OK);
                } else {
                    // Store with the provided ID was not found
                    return BerlizUtilities.getResponseEntity("Store id not found", HttpStatus.BAD_REQUEST);
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
     * Updates a Store status based on the provided Store ID.
     *
     * @param id The ID of the Store status to be updated
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<String> updateStatus(Integer id) {
        try {
            log.info("Inside updateStatus {}", id);

            // Retrieve the current user's ID
            Integer userId = jwtFilter.getCurrentUserId();

            // Retrieve the Store entity by ID
            Optional<Store> optional = storeRepo.findById(id);

            // Check if the Store exists
            if (optional.isPresent()) {
                log.info("Inside optional {}", optional);

                // Retrieve the ID and status of the user associated with the Store
                Integer validUser = optional.get().getPartner().getUser().getId();
                String validUserStatus = optional.get().getStatus();

                // Check if the user is an admin or the associated partner
                if (jwtFilter.isAdmin() || (validUser.equals(userId) && validUserStatus.equalsIgnoreCase("true"))) {
                    log.info("Is valid user? Admin: {}, ValidUser: {}, CurrentUser: {}", jwtFilter.isAdmin(), validUser, userId);

                    // Get the current status of the Store
                    String status = optional.get().getStatus();
                    String userEmail = optional.get().getPartner().getUser().getEmail();

                    // Toggle the status
                    status = status.equalsIgnoreCase("true") ? "false" : "true";

                    // Update the status in the repository
                    storeRepo.updateStatus(id, status);

                    // Update user role in user repository
                    if(optional.get().getPartner().getUser().getRole().equalsIgnoreCase("user") && status.equalsIgnoreCase("true")) {
                        userRepo.updateUserRole("store", validUser);
                    } else{
                        userRepo.updateUserRole("user", validUser);
                    }

                    // Send status update emails
                    emailUtilities.sendStatusMailToAdmins(status, userEmail, userRepo.getAllAdminsMail(), "Store");
                    emailUtilities.sendStatusMailToUser(status, "Store", userEmail);

                    // Return a success response
                    String responseMessage = status.equalsIgnoreCase("true") ?
                            "Store Status updated successfully. NOW ACTIVE" :
                            "Store Status updated successfully. NOW DISABLED";
                    return BerlizUtilities.getResponseEntity(responseMessage, HttpStatus.OK);
                } else {
                    // Return an unauthorized response
                    return BerlizUtilities.getResponseEntity(BerlizConstants.UNAUTHORIZED_REQUEST, HttpStatus.UNAUTHORIZED);
                }
            } else {
                // Return a response when Store ID is not found
                return BerlizUtilities.getResponseEntity("Store id not found", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Return an error response if an exception occurred
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Get a list of Stores based on the status provided.
     *
     * @param status The status of the Stores to be fetched
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<List<Store>> getByStatus(String status) {
        try {
            log.info("Inside getByStatus");

            // Check if the user is an admin
            if (jwtFilter.isAdmin()) {
                // Retrieve Stores with the specified status
                List<Store> Stores = storeRepo.findByStatus(status);

                if (Stores != null) {
                    // Stores with the specified status were found, return them
                    return new ResponseEntity<>(Stores, HttpStatus.OK);
                } else {
                    // No Stores found with the specified status
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
     * Get a Store based on the provided partner ID in the Store.
     *
     * @param id The ID of the Store to be fetched
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<Store> getByPartnerId(Integer id) {
        try {
            log.info("Inside getByPartnerId");

            // Check if the user is an admin
            if (jwtFilter.isAdmin()) {
                // Find the Store by the partner's id
                Store store = storeRepo.findByPartnerId(id);

                // Check if the Store associated with the partner id exists
                if (store == null) {
                    // If Store doesn't exist, return UNAUTHORIZED status
                    return new ResponseEntity<>(new Store(), HttpStatus.UNAUTHORIZED);
                }

                // Get the actual Store id and retrieve the Store by that id
                id = store.getId();
                Store existingStore = storeRepo.findById(id).orElse(null);

                if (existingStore != null) {
                    // Return the Store if found
                    return new ResponseEntity<>(existingStore, HttpStatus.OK);
                } else {
                    // If Store doesn't exist, return NOT_FOUND status
                    return new ResponseEntity<>(new Store(), HttpStatus.NOT_FOUND);
                }
            } else {
                // Return FORBIDDEN status if the user is not an admin
                return new ResponseEntity<>(new Store(), HttpStatus.FORBIDDEN);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            // Return INTERNAL_SERVER_ERROR status if an exception occurs
            return new ResponseEntity<>(new Store(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gets a Store based on the provided Store ID.
     *
     * @param id The ID of the Store to be fetched
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<Store> getStore(Integer id) {
        try {
            log.info("Inside getStore");

            // Get the current user's ID
            Integer user = jwtFilter.getCurrentUserId();

            // Retrieve the partner associated with the current user
            Partner partnerByUserId = partnerRepo.findByUserId(user);

            // Retrieve the Store by its ID
            Store Store = storeRepo.findById(id).orElse(null);

            if (Store == null) {
                // Store with the provided ID was not found
                return new ResponseEntity<>(new Store(), HttpStatus.BAD_REQUEST);
            }

            if (jwtFilter.isAdmin()) {
                // User is an admin, return the retrieved Store
                return new ResponseEntity<>(Store, HttpStatus.OK);
            } else if (partnerByUserId != null) {
                // Check if the logged-in user has a partner and the Store matches
                Integer currentUser = partnerByUserId.getUser().getId();

                if (currentUser.equals(user) && Store.getPartner().getId().equals(partnerByUserId.getId())) {
                    // Return the retrieved Store
                    return new ResponseEntity<>(Store, HttpStatus.OK);
                }
            }

            // Unauthorized access, user is not admin and doesn't have a valid Store
            return new ResponseEntity<>(new Store(), HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Internal server error occurred
        return new ResponseEntity<>(new Store(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Fetches list of Stores based on the provided category ID.
     *
     * @param id The ID of the Stores to be fetched
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<List<Store>> getByCategoryId(Integer id) {
        try {
            log.info("Inside getByCategoryId {}", id);

            // Retrieve Stores with the specified category ID
            List<Store> Stores = storeRepo.getByCategoryId(id);

            if (!Stores.isEmpty()) {
                // Stores with the specified category ID were found, return them
                return new ResponseEntity<>(Stores, HttpStatus.OK);
            } else {
                // No Stores found with the specified category ID
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Internal server error occurred
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Gets a Store based on the provided user ID.
     *
     * @param id The ID of the user related to the Store to be fetched
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<Store> getByUserId(Integer id) {
        try {
            log.info("Inside getByUserId");

            // Retrieve the user's partner details using the provided user ID
            Partner partner = partnerRepo.findByUserId(id);
            if (partner == null) {
                return new ResponseEntity<>(new Store(), HttpStatus.BAD_REQUEST);
            }

            if (jwtFilter.isAdmin() && partner != null) {
                // Retrieve the Store associated with the partner (if exists)
                Store Store = storeRepo.findByPartnerId(partner.getId());

                if (Store != null) {
                    // Return the retrieved Store
                    return new ResponseEntity<>(Store, HttpStatus.OK);
                } else {
                    // No Store associated with the provided user ID
                    return new ResponseEntity<>(new Store(), HttpStatus.NOT_FOUND);
                }
            } else {
                // Unauthorized access, user is not an admin and doesn't have a valid Store
                return new ResponseEntity<>(new Store(), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Internal server error occurred
        return new ResponseEntity<>(new Store(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Validates the provided request map based on the presence of specific keys.
     *
     * @param requestMap The map containing the request parameters
     * @param validId    Indicates whether a valid ID is required in the request map
     * @return True if the request map is valid, false otherwise
     */
    private boolean validateStoreFromMap(Map<String, String> requestMap, boolean validId) {
        if (validId) {
            // Check if all required keys are present when a valid ID is required
            return requestMap.containsKey("id")
                    && requestMap.containsKey("name")
                    && requestMap.containsKey("motto")
                    && requestMap.containsKey("phone")
                    && requestMap.containsKey("address")
                    && requestMap.containsKey("introduction")
                    && requestMap.containsKey("location")
                    && requestMap.containsKey("photo")
                    && requestMap.containsKey("likes")
                    && requestMap.containsKey("categoryIds");
        } else {
            // Check if all required keys are present when a valid ID is not required
            return requestMap.containsKey("name")
                    && requestMap.containsKey("motto")
                    && requestMap.containsKey("phone")
                    && requestMap.containsKey("address")
                    && requestMap.containsKey("introduction")
                    && requestMap.containsKey("location")
                    && requestMap.containsKey("photo")
                    && requestMap.containsKey("likes")
                    && requestMap.containsKey("categoryIds");
        }
    }

    /**
     * Constructs a Store object from the provided request map and saves it to the repository.
     *
     * @param requestMap The map containing the request data
     * @param isUser     Flag indicating whether the request is coming from a user
     * @return The constructed and saved Store object
     */
    private Store getStoreFromMap(Map<String, String> requestMap, Boolean isUser) {
        Store Store = new Store();
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
        Store.setCategorySet(categorySet);

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

        // Populate the Store object with the provided data
        Store.setPartner(partner);
        Store.setName(requestMap.get("name"));
        Store.setMotto(requestMap.get("motto"));
        Store.setAddress(requestMap.get("address"));
        Store.setIntroduction(requestMap.get("introduction"));
        Store.setLocation(requestMap.get("location"));
        Store.setPhoto(requestMap.get("photo"));
        Store.setLikes(0); // Initializing likes
        Store.setDate(new Date());
        Store.setLastUpdate(new Date());
        Store.setStatus("false"); // Initializing status

        // Save the constructed Store object to the repository
        storeRepo.save(Store);

        return Store;
    }

    /**
     * Updates a Store object from the provided request map and saves it to the repository.
     *
     * @param requestMap The map containing the request data
     * @return The constructed and saved Store object
     */
    private ResponseEntity<String> updateStoreFromMap(Map<String, String> requestMap) {
        try {
            // Get the current user's ID
            Integer currentUser = jwtFilter.getCurrentUserId();

            // Find the Store by ID
            Optional<Store> optional = storeRepo.findById(Integer.valueOf(requestMap.get("id")));

            // Check if the Store with the given ID exists
            if (optional.isEmpty()) {
                return BerlizUtilities.getResponseEntity("Store id does not exist", HttpStatus.BAD_REQUEST);
            }

            // Retrieve the existing Store
            Store existingStore = optional.get();

            // Check if the user has permission to update the Store
            boolean validUser = jwtFilter.isAdmin() || currentUser.equals(existingStore.getPartner().getUser().getId());

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

            // Update the Store properties
            existingStore.setCategorySet(categorySet);
            existingStore.setName(requestMap.get("name"));
            existingStore.setMotto(requestMap.get("motto"));
            existingStore.setAddress(requestMap.get("address"));
            existingStore.setIntroduction(requestMap.get("introduction"));
            existingStore.setLocation(requestMap.get("location"));
            existingStore.setPhoto(requestMap.get("photo"));
            existingStore.setLikes(Integer.parseInt(requestMap.get("likes")));
            existingStore.setLastUpdate(new Date());

            // Save the updated Store
            storeRepo.save(existingStore);

            return BerlizUtilities.getResponseEntity("Store updated successfully", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    /**
     * Handle use case of adding Store by an admin.
     *
     * @param requestMap The map containing the request data
     * @return The constructed and saved Store object
     */
    private ResponseEntity<String> handleStoreAdditionByAdmin(Map<String, String> requestMap) {
        try {
            log.info("Handling Store addition by admin");

            // Admin must provide partnerId
            if (!requestMap.containsKey("partnerId")) {
                return new ResponseEntity<>("Admin must provide partnerId", HttpStatus.BAD_REQUEST);
            }

            Integer partnerIdValue = Integer.valueOf(requestMap.get("partnerId"));

            // Check if partnerId already has an associated Store
            Store existingStoreByPartnerId = storeRepo.findByPartnerId(partnerIdValue);
            if (existingStoreByPartnerId != null) {
                return BerlizUtilities.getResponseEntity("Partner id already exists", HttpStatus.BAD_REQUEST);
            }

            // Check if the current user is already associated with a Store
            Partner partner = partnerRepo.findByPartnerId(partnerIdValue);

            // Return an error message if partner is null
            if(partner == null){
                return BerlizUtilities.getResponseEntity("Partner is null", HttpStatus.BAD_REQUEST);
            }

            // Check if the user is associated with trainer
            Integer userId = partner.getUser().getId();
            if (!isUserAssociatedWithStore(userId)) {
                return BerlizUtilities.getResponseEntity("User is already associated with a Store", HttpStatus.BAD_REQUEST);
            }

            // Check if the partner is a Store
            if (!isValidRole(partnerIdValue, "Store")) {
                return BerlizUtilities.getResponseEntity("Invalid partner role. Partner must be a store", HttpStatus.BAD_REQUEST);
            }

            // Check if the partner already exists
            if (!isValidPartner(partnerIdValue)) {
                return BerlizUtilities.getResponseEntity("Partner does not exist", HttpStatus.BAD_REQUEST);
            }

            // Check if partnerId already been approved by an admin
            if (!isApprovedStorePartner(partnerIdValue)) {
                return BerlizUtilities.getResponseEntity("Please wait for admin approval", HttpStatus.BAD_REQUEST);
            }

            getStoreFromMap(requestMap, false);
            return new ResponseEntity<>("Store added successfully", HttpStatus.CREATED);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle use case of adding Store by a valid user.
     *
     * @param requestMap The map containing the request data
     * @return The constructed and saved Store object
     */
    private ResponseEntity<String> handleStoreAdditionByUser(Map<String, String> requestMap) {
        try {
            log.info("Handling Store addition by user");

            // Retrieve the current user's ID
            Integer userId = jwtFilter.getCurrentUserId();

            // Check if the current user is already associated with a Store
            Partner partner = partnerRepo.findByUserId(userId);

            // Return an error message if partner is null
            if(partner == null){
                return BerlizUtilities.getResponseEntity("Partner id does not exist", HttpStatus.BAD_REQUEST);
            }

            Integer partnerId = partner.getId();
            if (!isUserAssociatedWithStore(userId)) {
                return BerlizUtilities.getResponseEntity("User is already associated with a Store", HttpStatus.BAD_REQUEST);
            }

            // Check if partnerId already has an associated Store
            Store existingStoreByPartnerId = storeRepo.findByPartnerId(partnerId);
            if (existingStoreByPartnerId != null) {
                return BerlizUtilities.getResponseEntity("Partner id is null", HttpStatus.BAD_REQUEST);
            }

            // Check if the partner is a Store
            if (!isValidRole(partnerId, "Store")) {
                return BerlizUtilities.getResponseEntity("Invalid partner role. Partner must be a store", HttpStatus.BAD_REQUEST);
            }

            // Check if the partner already exists
            if (!isValidPartner(partnerId)) {
                return BerlizUtilities.getResponseEntity("Partner does not exist", HttpStatus.BAD_REQUEST);
            }


            // Check if partnerId already been approved by an admin
            if (!isApprovedStorePartner(partnerId)) {
                return BerlizUtilities.getResponseEntity("Please wait for admin approval", HttpStatus.BAD_REQUEST);
            }

            // Check if the Store name already exists
            if (isStoreNameAlreadyExists(requestMap.get("name"))) {
                return BerlizUtilities.getResponseEntity("Store name already exists", HttpStatus.BAD_REQUEST);
            }

            getStoreFromMap(requestMap, true);
            return new ResponseEntity<>("Store added successfully", HttpStatus.CREATED);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Validates partner.
     *
     * @param partnerId    ID of the partner to be validated
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
     * Validates if Store is an approved partner.
     *
     * @param partnerId ID of the partner to be approved
     * @return The valid partner
     */
    private boolean isApprovedStorePartner(Integer partnerId) {
        Partner partner = partnerRepo.findByPartnerId(partnerId);
        return partner != null && partner.getStatus().equalsIgnoreCase("true");
    }

    /**
     * Checks if a user is linked to a Store.
     *
     * @param userId ID of the Store to be checked
     * @return The valid partner
     */
    private boolean isUserAssociatedWithStore(Integer userId) {
        Partner currentUserPartner = partnerRepo.findByUserId(userId);
        return Optional.ofNullable(currentUserPartner)
                .map(Partner::getUser)
                .map(User::getId)
                .isPresent();
    }

    /**
     * Checks if a Store name already exists.
     *
     * @param StoreName name of Store to be checked
     * @return The valid partner
     */
    private boolean isStoreNameAlreadyExists(String StoreName) {
        Store StoreByName = storeRepo.findByName(StoreName);
        return StoreByName != null;
    }

}

package com.berliz.serviceImplement;

import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.Driver;
import com.berliz.models.Partner;
import com.berliz.models.User;
import com.berliz.repositories.DriverRepo;
import com.berliz.repositories.PartnerRepo;
import com.berliz.repositories.UserRepo;
import com.berliz.services.DriverService;
import com.berliz.utils.BerlizUtilities;
import com.berliz.utils.EmailUtilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class DriverServiceImplement implements DriverService {

    @Autowired
    JWTFilter jwtFilter;

    @Autowired
    UserRepo userRepo;

    @Autowired
    DriverRepo driverRepo;

    @Autowired
    EmailUtilities emailUtilities;

    @Autowired
    PartnerRepo partnerRepo;

    /**
     * Adds a Driver based on data provided
     *
     * @param requestMap The request body containing data
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<String> addDriver(Map<String, String> requestMap) {
        try {
            log.info("Inside addDriver {}", requestMap);

            // Validate the incoming request
            boolean validRequest = validateDriverFromMap(requestMap, false);
            log.info("Is request valid? {}", validRequest);

            // Check if the request is valid
            if (!validRequest) {
                return BerlizUtilities.getResponseEntity(BerlizConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }

            // Handle Driver addition based on user role
            if (jwtFilter.isAdmin()) {
                // Admins have additional checks when adding a Driver
                return handleDriverAdditionByAdmin(requestMap);
            } else {
                // Users without admin rights add Drivers with different criteria
                return handleDriverAdditionByUser(requestMap);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Return an error response if an exception occurred
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Updates a Driver based on data provided
     *
     * @param requestMap The request body containing data
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<String> updateDriver(Map<String, String> requestMap) {
        try {
            log.info("Inside updateDriver {}", requestMap);

            // Validate the incoming request
            boolean isValid = validateDriverFromMap(requestMap, true);
            log.info("Is request valid? {}", isValid);

            if (!isValid) {
                return BerlizUtilities.getResponseEntity(BerlizConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }

            // Find the Driver by its ID
            Driver validDriver = driverRepo.findByDriverId(Integer.valueOf(requestMap.get("id")));

            // Check if the Driver exists
            if (validDriver == null) {
                return BerlizUtilities.getResponseEntity("Driver id not found", HttpStatus.BAD_REQUEST);
            }

            // Check user permissions and update Driver
            if (jwtFilter.isAdmin() || (jwtFilter.isDriver()
                    && jwtFilter.getCurrentUserId().equals(validDriver.getPartner().getUser().getId())
                    && validDriver.getStatus().equalsIgnoreCase("true"))) {
                updateDriverFromMap(requestMap);
                return BerlizUtilities.getResponseEntity("Driver updated successfully", HttpStatus.OK);
            } else {
                return BerlizUtilities.getResponseEntity(BerlizConstants.UNAUTHORIZED_REQUEST, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Return an error response if an exception occurred
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Returns a list of Drivers
     *
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<List<Driver>> getAllDrivers() {
        try {
            log.info("Inside getAllDrivers");

            // Check if the user is an admin
            if (jwtFilter.isAdmin()) {
                // Retrieve all Drivers from the repository
                List<Driver> drivers = driverRepo.findAll();
                return new ResponseEntity<>(drivers, HttpStatus.OK);
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
     * Updates a Driver partner ID based on the existing id and new id provided
     *
     * @param id    The existing partner id to be replaced.
     * @param newId The new partner id.
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<String> updatePartnerId(Integer id, Integer newId) {
        try {
            log.info("Inside updateDriverPartnerId {}", id);

            // Check if the user is an admin
            if (jwtFilter.isAdmin()) {
                // Retrieve the Driver with the given id
                Optional<Driver> optional = driverRepo.findById(id);

                if (optional.isPresent()) {
                    log.info("Inside optional {}", optional);
                    Driver driver = optional.get();

                    // Check if the new partner id exists
                    Partner newPartner = partnerRepo.findById(newId).orElse(null);
                    if (newPartner == null) {
                        return BerlizUtilities.getResponseEntity("Invalid new partner id", HttpStatus.BAD_REQUEST);
                    }

                    // Check if the new partner id exists in the driver
                    Driver partnerDriver = driverRepo.findById(newId).orElse(null);
                    if (partnerDriver != null) {
                        try {

                        } catch (DataIntegrityViolationException ex) {
                            // Handle foreign key constraint violation when deleting
                            ex.printStackTrace();
                            return BerlizUtilities.getResponseEntity("Partner id exists in driver. Foreign key constraint violation.", HttpStatus.BAD_REQUEST);
                        }
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

                    // Check if the Driver status is false before updating partner id
                    if (driver.getStatus().equalsIgnoreCase("false")) {
                        // Update the Driver's partner id
                        driverRepo.updatePartnerId(id, newId);
                        return BerlizUtilities.getResponseEntity("Driver - partner id updated successfully. New id: " + newId, HttpStatus.OK);
                    } else {
                        return BerlizUtilities.getResponseEntity("Driver status must be false to update partner id", HttpStatus.BAD_REQUEST);
                    }
                } else {
                    return BerlizUtilities.getResponseEntity("Driver with id " + id + " not found", HttpStatus.BAD_REQUEST);
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
     * Deletes a Driver based on the provided Driver ID.
     *
     * @param id The ID of the Driver to be deleted
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<String> deleteDriver(Integer id) {
        try {
            // Check if the current user is an admin
            if (jwtFilter.isAdmin()) {
                // Retrieve the Driver by its ID
                Driver driver = driverRepo.findByDriverId(id);
                if (driver != null) {
                    // Delete the retrieved Driver from the repository
                    driverRepo.delete(driver);
                    return BerlizUtilities.getResponseEntity("Driver deleted successfully", HttpStatus.OK);
                } else {
                    // Driver with the provided ID was not found
                    return BerlizUtilities.getResponseEntity("Driver id not found", HttpStatus.BAD_REQUEST);
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
     * Updates a Driver status based on the provided Driver ID.
     *
     * @param id The ID of the Driver status to be updated
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<String> updateStatus(Integer id) {
        try {
            log.info("Inside updateStatus {}", id);

            // Retrieve the current user's ID
            Integer userId = jwtFilter.getCurrentUserId();

            // Retrieve the Driver entity by ID
            Optional<Driver> optional = driverRepo.findById(id);

            // Check if the Driver exists
            if (optional.isPresent()) {
                log.info("Inside optional {}", optional);

                // Retrieve the ID of the user associated with the Driver
                Integer validUser = optional.get().getPartner().getUser().getId();

                // Check if the user is an admin or the associated partner
                if (jwtFilter.isAdmin() || validUser.equals(userId)) {
                    log.info("Is valid user? Admin: {}, ValidUser: {}, CurrentUser: {}", jwtFilter.isAdmin(), validUser, userId);

                    // Get the current status of the Driver
                    String status = optional.get().getStatus();
                    String userEmail = optional.get().getPartner().getUser().getEmail();

                    // Toggle the status
                    status = status.equalsIgnoreCase("true") ? "false" : "true";

                    // Update the status in the repository
                    driverRepo.updateStatus(id, status);

                    // Update user role in user repository
                    if (optional.get().getPartner().getUser().getRole().equalsIgnoreCase("user") && status.equalsIgnoreCase("true")) {
                        userRepo.updateUserRole("driver", validUser);
                    } else {
                        userRepo.updateUserRole("user", validUser);
                    }

                    // Send status update emails
                    emailUtilities.sendStatusMailToAdmins(status, userEmail, userRepo.getAllAdminsMail(), "Driver");
                    emailUtilities.sendStatusMailToUser(status, "Driver", userEmail);

                    // Return a success response
                    String responseMessage = status.equalsIgnoreCase("true") ?
                            "Driver Status updated successfully. NOW ACTIVE" :
                            "Driver Status updated successfully. NOW DISABLED";
                    return BerlizUtilities.getResponseEntity(responseMessage, HttpStatus.OK);
                } else {
                    // Return an unauthorized response
                    return BerlizUtilities.getResponseEntity(BerlizConstants.UNAUTHORIZED_REQUEST, HttpStatus.UNAUTHORIZED);
                }
            } else {
                // Return a response when Driver ID is not found
                return BerlizUtilities.getResponseEntity("Driver id not found", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Return an error response if an exception occurred
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Get a Driver based on the provided partner ID in the Driver.
     *
     * @param id The ID of the Driver to be fetched
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<Driver> getByPartnerId(Integer id) {
        try {
            log.info("Inside getByPartnerId");

            // Check if the user is an admin
            if (jwtFilter.isAdmin()) {
                // Find the driver by the partner's id
                Driver driver = driverRepo.findByPartnerId(id);

                // Check if the driver associated with the partner id exists
                if (driver == null) {
                    // If driver doesn't exist, return UNAUTHORIZED status
                    return new ResponseEntity<>(new Driver(), HttpStatus.UNAUTHORIZED);
                }

                // Get the actual driver id and retrieve the driver by that id
                id = driver.getId();
                Driver existingDriver = driverRepo.findById(id).orElse(null);

                if (existingDriver != null) {
                    // Return the driver if found
                    return new ResponseEntity<>(existingDriver, HttpStatus.OK);
                } else {
                    // If driver doesn't exist, return NOT_FOUND status
                    return new ResponseEntity<>(new Driver(), HttpStatus.NOT_FOUND);
                }
            } else {
                // Return FORBIDDEN status if the user is not an admin
                return new ResponseEntity<>(new Driver(), HttpStatus.FORBIDDEN);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            // Return INTERNAL_SERVER_ERROR status if an exception occurs
            return new ResponseEntity<>(new Driver(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gets a Driver based on the provided Driver ID.
     *
     * @param id The ID of the Driver to be fetched
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<Driver> getDriver(Integer id) {
        try {
            log.info("Inside getDriver");

            // Get the current user's ID
            Integer currentUser = jwtFilter.getCurrentUserId();

            // Retrieve the partner associated with the current user
            Partner loggedUserPartner = partnerRepo.findByUserId(currentUser);

            // Retrieve the driver by its ID
            Driver driver = driverRepo.findById(id).orElse(null);

            if (driver == null) {
                // Driver with the provided ID was not found
                return new ResponseEntity<>(new Driver(), HttpStatus.BAD_REQUEST);
            }

            // Check if the user is an admin or the logged-in user has a partner and a driver
            if (jwtFilter.isAdmin() || (loggedUserPartner != null && driver.getPartner() != null)) {
                // Return the retrieved driver
                return new ResponseEntity<>(driver, HttpStatus.OK);
            } else {
                // Unauthorized access, user is not admin and doesn't have a valid partner or driver
                return new ResponseEntity<>(new Driver(), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Internal server error occurred
        return new ResponseEntity<>(new Driver(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Get a list of Drivers based on the status provided.
     *
     * @param status The status of the Drivers to be fetched
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<List<Driver>> getByStatus(String status) {
        try {
            log.info("Inside getByStatus");

            // Check if the user is an admin
            if (jwtFilter.isAdmin()) {
                // Retrieve drivers with the specified status
                List<Driver> drivers = driverRepo.findByStatus(status);

                if (drivers != null) {
                    // Drivers with the specified status were found, return them
                    return new ResponseEntity<>(drivers, HttpStatus.OK);
                } else {
                    // No drivers found with the specified status
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
     * Gets a Driver based on the provided user ID.
     *
     * @param id The ID of the user related to the Driver to be fetched
     * @return ResponseEntity with a success message or an error message
     */
    @Override
    public ResponseEntity<Driver> getByUserId(Integer id) {
        try {
            log.info("Inside getByUserId");

            // Retrieve the user's partner details using the provided user ID
            Partner partner = partnerRepo.findByUserId(id);
            if (partner == null) {
                return new ResponseEntity<>(new Driver(), HttpStatus.BAD_REQUEST);
            }

            if (jwtFilter.isAdmin() && partner != null) {
                // Retrieve the Driver associated with the partner (if exists)
                Driver driver = driverRepo.findByPartnerId(partner.getId());

                if (driver != null) {
                    // Return the retrieved Driver
                    return new ResponseEntity<>(driver, HttpStatus.OK);
                } else {
                    // No Driver associated with the provided user ID
                    return new ResponseEntity<>(new Driver(), HttpStatus.NOT_FOUND);
                }
            } else {
                // Unauthorized access, user is not an admin and doesn't have a valid Driver
                return new ResponseEntity<>(new Driver(), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Internal server error occurred
        return new ResponseEntity<>(new Driver(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    /**
     * Constructs a new Driver object from the provided request map and saves it to the repository.
     *
     * @param requestMap The map containing the request data.
     * @param isUser     Flag indicating whether the request is coming from a user.
     * @return ResponseEntity indicating the result of the driver creation.
     */
    private ResponseEntity<String> getDriverFromMap(Map<String, String> requestMap, Boolean isUser) {
        try {
            // Create a new Driver object
            Driver driver = new Driver();
            Partner partner = new Partner();
            Integer partnerId;

            // Determine the partner ID based on whether the request is from a user or not
            if (isUser) {
                // Get the user ID from the JWT
                Integer userId = jwtFilter.getCurrentUserId();
                Partner partnerByUserId = partnerRepo.findByUserId(userId);
                partnerId = partnerByUserId.getId();
            } else {
                // Get the partner ID from the request map
                partnerId = Integer.valueOf(requestMap.get("partnerId"));
            }

            // Set the partner's ID
            partner.setId(partnerId);

            // Populate the Driver object with the provided data
            driver.setPartner(partner);
            driver.setUuid(requestMap.get("uuid"));
            driver.setName(requestMap.get("name"));
            driver.setIntroduction(requestMap.get("introduction"));
            driver.setVehicleType(requestMap.get("vehicleType"));
            driver.setVehicleModel(requestMap.get("vehicleModel"));
            driver.setLicensePlate(requestMap.get("licensePlate"));
            driver.setLikes(0); // Initializing likes
            driver.setLocation(requestMap.get("location"));
            driver.setAddress(requestMap.get("address"));
            driver.setDate(new Date()); // Setting the creation date
            driver.setLastUpdate(new Date()); // Setting the last update date
            driver.setStatus("false"); // Initializing status

            // Save the constructed Driver object to the repository
            driverRepo.save(driver);

            // Return a response indicating success (can be modified as needed)
            return BerlizUtilities.getResponseEntity("Driver created successfully", HttpStatus.CREATED);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Return an error response if an exception occurred
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Updates a driver based on the data provided in the request map.
     *
     * @param requestMap The request body containing the data to update the driver.
     * @return ResponseEntity with a success message or an error message.
     */
    private ResponseEntity<String> updateDriverFromMap(Map<String, String> requestMap) {
        try {
            // Get the current user's ID from the JWT
            Integer currentUser = jwtFilter.getCurrentUserId();

            // Find the existing driver by ID
            Driver existingDriver = driverRepo.findByDriverId(Integer.valueOf(requestMap.get("id")));

            // Check if the current user has valid permissions to update the driver
            boolean validUser;
            validUser = jwtFilter.isAdmin() || currentUser.equals(existingDriver.getPartner().getUser().getId());

            // If user is not valid, return unauthorized response
            if (!validUser) {
                return BerlizUtilities.getResponseEntity(BerlizConstants.UNAUTHORIZED_REQUEST, HttpStatus.UNAUTHORIZED);
            }

            // Check if the driver exists
            if (existingDriver == null) {
                return BerlizUtilities.getResponseEntity("Driver id does not exist", HttpStatus.BAD_REQUEST);
            }

            // Update driver properties from the request map
            existingDriver.setName(requestMap.get("name"));
            existingDriver.setLocation(requestMap.get("location"));
            existingDriver.setAddress(requestMap.get("address"));
            existingDriver.setIntroduction(requestMap.get("introduction"));
            existingDriver.setLocation(requestMap.get("location"));
            existingDriver.setLicensePlate(requestMap.get("licensePlate"));
            existingDriver.setVehicleType(requestMap.get("vehicleType"));
            existingDriver.setVehicleModel(requestMap.get("vehicleModel"));
            existingDriver.setLikes(Integer.parseInt(requestMap.get("likes")));
            existingDriver.setLastUpdate(new Date());

            // Save the updated driver to the repository
            driverRepo.save(existingDriver);

            return BerlizUtilities.getResponseEntity("Driver updated successfully", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Return an error response if an exception occurred
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    /**
     * Validates the driver data provided in the request map.
     *
     * @param requestMap The request body containing driver data
     * @param validId    Flag indicating if a valid driver ID is required
     * @return True if the driver data in the request map is valid, false otherwise
     */
    private boolean validateDriverFromMap(Map<String, String> requestMap, boolean validId) {
        if (validId) {
            return requestMap.containsKey("id")
                    && requestMap.containsKey("name")
                    && requestMap.containsKey("introduction")
                    && requestMap.containsKey("vehicleType")
                    && requestMap.containsKey("vehicleModel")
                    && requestMap.containsKey("licensePlate")
                    && requestMap.containsKey("address")
                    && requestMap.containsKey("location");
        } else {
            return requestMap.containsKey("name")
                    && requestMap.containsKey("introduction")
                    && requestMap.containsKey("vehicleType")
                    && requestMap.containsKey("vehicleModel")
                    && requestMap.containsKey("licensePlate")
                    && requestMap.containsKey("address")
                    && requestMap.containsKey("location");
        }
    }


    /**
     * Handle use case of adding Driver by an admin.
     *
     * @param requestMap The map containing the request data
     * @return The constructed and saved Driver object
     */
    private ResponseEntity<String> handleDriverAdditionByAdmin(Map<String, String> requestMap) {
        try {
            log.info("Handling Driver addition by admin");

            // Admin must provide partnerId
            if (!requestMap.containsKey("partnerId")) {
                return new ResponseEntity<>("Admin must provide partnerId", HttpStatus.BAD_REQUEST);
            }

            Integer partnerIdValue = Integer.valueOf(requestMap.get("partnerId"));

            // Check if partnerId already has an associated Driver
            Driver existingDriverByPartnerId = driverRepo.findByPartnerId(partnerIdValue);
            if (existingDriverByPartnerId != null) {
                return BerlizUtilities.getResponseEntity("Partner id already exists", HttpStatus.BAD_REQUEST);
            }

            // Check if the current user is already associated with a Driver
            Partner partner = partnerRepo.findByPartnerId(partnerIdValue);

            // Return an error message if partner is null
            if (partner == null) {
                return BerlizUtilities.getResponseEntity("Partner id is null", HttpStatus.BAD_REQUEST);
            }

            // Check if the user is associated with trainer
            Integer userId = partner.getUser().getId();
            if (!isUserAssociatedWithDriver(userId)) {
                return BerlizUtilities.getResponseEntity("User is already associated with a Driver", HttpStatus.BAD_REQUEST);
            }

            // Check if the partner is a driver
            if (!isValidRole(partnerIdValue, "driver")) {
                return BerlizUtilities.getResponseEntity("Invalid partner role. Partner must be a driver", HttpStatus.BAD_REQUEST);
            }

            // Check if the partner already exists
            if (!isValidPartner(partnerIdValue)) {
                return BerlizUtilities.getResponseEntity("Partner does not exist", HttpStatus.BAD_REQUEST);
            }

            // Check if partnerId already been approved by an admin
            if (!isApprovedDriverPartner(partnerIdValue)) {
                return BerlizUtilities.getResponseEntity("Please wait for admin approval", HttpStatus.BAD_REQUEST);
            }

            getDriverFromMap(requestMap, false);
            return new ResponseEntity<>("Driver added successfully", HttpStatus.CREATED);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle use case of adding Driver by a valid user.
     *
     * @param requestMap The map containing the request data
     * @return The constructed and saved Driver object
     */
    private ResponseEntity<String> handleDriverAdditionByUser(Map<String, String> requestMap) {
        try {
            log.info("Handling Driver addition by user");

            // Retrieve the current user's ID
            Integer userId = jwtFilter.getCurrentUserId();

            // Check if the current user is already associated with a Driver
            Partner partner = partnerRepo.findByUserId(userId);

            // Return an error message if partner is null
            if (partner == null) {
                return BerlizUtilities.getResponseEntity("Partner id is null", HttpStatus.BAD_REQUEST);
            }

            Integer partnerId = partner.getId();
            if (!isUserAssociatedWithDriver(userId)) {
                return BerlizUtilities.getResponseEntity("User is already associated with a Driver", HttpStatus.BAD_REQUEST);
            }

            // Check if partnerId already has an associated Driver
            Driver existingDriverByPartnerId = driverRepo.findByPartnerId(partnerId);
            if (existingDriverByPartnerId != null) {
                return BerlizUtilities.getResponseEntity("Partner id already exists", HttpStatus.BAD_REQUEST);
            }

            // Check if the partner is a driver
            if (!isValidRole(partnerId, "driver")) {
                return BerlizUtilities.getResponseEntity("Invalid partner role. Partner must be a driver", HttpStatus.BAD_REQUEST);
            }

            // Check if the partner already exists
            if (!isValidPartner(partnerId)) {
                return BerlizUtilities.getResponseEntity("Partner does not exist", HttpStatus.BAD_REQUEST);
            }

            // Check if partnerId already been approved by an admin
            if (!isApprovedDriverPartner(partnerId)) {
                return BerlizUtilities.getResponseEntity("Please wait for admin approval", HttpStatus.BAD_REQUEST);
            }

            // Check if the Driver name already exists
            if (isDriverNameAlreadyExists(requestMap.get("name"))) {
                return BerlizUtilities.getResponseEntity("Driver name already exists", HttpStatus.BAD_REQUEST);
            }

            getDriverFromMap(requestMap, true);
            return new ResponseEntity<>("Driver added successfully", HttpStatus.CREATED);
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
     * Validates if Driver is an approved partner.
     *
     * @param partnerId ID of the partner to be approved
     * @return The valid partner
     */
    private boolean isApprovedDriverPartner(Integer partnerId) {
        Partner partner = partnerRepo.findByPartnerId(partnerId);
        return partner != null && partner.getStatus().equalsIgnoreCase("true");
    }

    /**
     * Checks if a user is linked to a Driver.
     *
     * @param userId ID of the Driver to be checked
     * @return The valid partner
     */
    private boolean isUserAssociatedWithDriver(Integer userId) {
        Partner currentUserPartner = partnerRepo.findByUserId(userId);
        return Optional.ofNullable(currentUserPartner)
                .map(Partner::getUser)
                .map(User::getId)
                .isPresent();
    }

    /**
     * Checks if a driver name already exists.
     *
     * @param driverName name of Driver to be checked
     * @return The valid partner
     */
    private boolean isDriverNameAlreadyExists(String driverName) {
        Driver driverByName = driverRepo.findByName(driverName);
        return driverByName != null;
    }
}

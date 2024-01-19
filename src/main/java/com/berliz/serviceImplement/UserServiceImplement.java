package com.berliz.serviceImplement;

import com.berliz.DTO.ProfilePhotoRequest;
import com.berliz.DTO.SignupRequest;
import com.berliz.JWT.ClientUserDetailsService;
import com.berliz.JWT.JWTFilter;
import com.berliz.JWT.JWTUtility;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.User;
import com.berliz.repositories.UserRepo;
import com.berliz.services.UserService;
import com.berliz.utils.EmailUtilities;
import com.berliz.utils.FileUtilities;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class UserServiceImplement implements UserService {

    @Autowired
    UserRepo userRepo;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    ClientUserDetailsService clientUserDetailsService;

    @Autowired
    JWTUtility jwtUtility;

    @Autowired
    EmailUtilities emailUtilities;

    @Autowired
    JWTFilter jwtFilter;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    FileUtilities fileUtilities;

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    /**
     * Process a user's signup request by creating a new user account if the email is not already registered.
     *
     * @param request A SignupRequest object containing user signup information, including email and password.
     * @return A ResponseEntity<String> indicating the result of the signup process.
     * @throws JsonProcessingException If there is an issue processing JSON data.
     */
    @Override
    public ResponseEntity<String> signUp(SignupRequest request) throws JsonProcessingException {
        log.info("Inside signUp {}", request);
        try {
            boolean isValidRequest = request != null;
            log.info("is request valid? {}", isValidRequest);

            if (!isValidRequest) {
                return buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            User user = userRepo.findByEmail(request.getEmail());
            if (!Objects.isNull(user)) {
                return buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.EMAIL_EXISTS);
            }

            getUserFromMap(request);
            confirmAccount(request.getEmail());
            return buildResponse(HttpStatus.OK, BerlizConstants.SIGNUP_SUCCESS);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Confirm a user's account activation by sending a confirmation code to their email address.
     *
     * @param email The email address of the user whose account needs to be confirmed.
     * @throws JsonProcessingException If there is an issue processing JSON data.
     */
    public void confirmAccount(String email) throws JsonProcessingException {
        try {
            User user = userRepo.findByEmail(email);
            if (!Objects.isNull(user)
                    && !Strings.isNullOrEmpty(String.valueOf(user.getEmail().equalsIgnoreCase(email)))) {
                if ("true".equalsIgnoreCase(user.getStatus())) {
                    buildResponse(HttpStatus.BAD_REQUEST, "Account is already active");
                    return;
                }
                emailUtilities.validateSignupMail(user.getEmail(), "Activate Account");
                buildResponse(HttpStatus.OK, "A confirmation code has been sent to your email");
            } else {
                buildResponse(HttpStatus.NOT_FOUND, "Email not found");
            }
            return;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Activate a user's account based on the provided activation token.
     *
     * @param requestMap A Map containing the activation token as a key-value pair (e.g., {"token": "activationToken"}).
     * @return A ResponseEntity<String> indicating the result of the account activation process.
     * @throws JsonProcessingException If there is an issue processing JSON data.
     */
    @Override
    public ResponseEntity<String> activateAccount(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            User user = userRepo.findByToken(requestMap.get("token"));
            if (user != null) {
                user.setToken("");
                user.setStatus("true");
                userRepo.save(user);
                simpMessagingTemplate.convertAndSend("/topic/activateAccount", user);
                return buildResponse(HttpStatus.OK, "Your account has successfully been activated");
            } else {
                return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Attempt to authenticate a user based on the provided login credentials (email and password).
     *
     * @param requestMap A Map containing the user's login credentials as key-value pairs (e.g., {"email": "user@example.com", "password": "userPassword"}).
     * @return A ResponseEntity<String> indicating the result of the login attempt, including a JWT token on successful login.
     * @throws JsonProcessingException If there is an issue processing JSON data.
     */
    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) throws JsonProcessingException {
        log.info("Inside login {}", requestMap);
        try {
            User user = userRepo.findByEmail(requestMap.get("email"));
            if (user != null) {
                try {
                    Authentication auth = authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(requestMap.get("email"),
                                    requestMap.get("password"))
                    );
                    if (auth.isAuthenticated()) {
                        User userDetails = clientUserDetailsService.getUserDetails();

                        if (userDetails.getStatus().equalsIgnoreCase("true")) {
                            String refreshToken = jwtUtility.generateRefreshToken(
                                    userDetails.getEmail(), userDetails.getId());
                            String accessToken = jwtUtility.generateAccessToken(refreshToken);

                            Map<String, String> responseBody = new HashMap<>();
                            responseBody.put("refresh_token", refreshToken);
                            responseBody.put("access_token", accessToken);
                            responseBody.put("message", "Login successful.");

                            ObjectMapper objectMapper = new ObjectMapper();
                            String responseBodyJson = objectMapper.writeValueAsString(responseBody);

                            return ResponseEntity.status(HttpStatus.OK)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .body(responseBodyJson);
                        } else {
                            return buildResponse(HttpStatus.BAD_REQUEST, "Please activate your account to continue");
                        }
                    } else {
                        return buildResponse(HttpStatus.BAD_REQUEST, "Bad credentials");

                    }
                } catch (BadCredentialsException ex) {
                    ex.printStackTrace();
                    return buildResponse(HttpStatus.BAD_REQUEST, "Incorrect password");
                }
            } else {
                return buildResponse(HttpStatus.BAD_REQUEST, "Incorrect username");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Retrieve a list of all users. This method is accessible only to administrators.
     *
     * @return A ResponseEntity containing a List of User objects representing all users in the system on success,
     * or an empty list with HTTP status UNAUTHORIZED if the requesting user is not an administrator,
     * or an empty list with HTTP status INTERNAL_SERVER_ERROR if an error occurs.
     */
    @Override
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            if (jwtFilter.isAdmin()) {
                return new ResponseEntity<>(userRepo.findAll(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<User>> getActiveUsers() {
        try {
            if (jwtFilter.isAdmin()) {
                return new ResponseEntity<>(userRepo.getActiveUsers(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> quickAdd(Map<String, String> requestMap) throws JsonProcessingException {
        log.info("Inside quickAdd {}", requestMap);
        try {
            boolean isValidRequest = requestMap.containsKey("email") && requestMap.containsKey("password");
            log.info("is request valid? {}", isValidRequest);

            if (!isValidRequest) {
                return buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            User user = userRepo.findByEmail(requestMap.get("email"));
            if (!Objects.isNull(user)) {
                return buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.EMAIL_EXISTS);
            }
            user = new User();
            user.setEmail(requestMap.get("email"));
            user.setDate(new Date());
            user.setLastUpdate(new Date());
            user.setStatus("false");
            user.setRole("user");
            user.setPassword(passwordEncoder.encode(requestMap.get("password")));
            userRepo.save(user);
            confirmAccount(requestMap.get("email"));
            return buildResponse(HttpStatus.OK, BerlizConstants.SIGNUP_SUCCESS);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> sendActivationToken(String email) throws JsonProcessingException {
        log.info("Inside sendActivationToken {}", email);
        try {
            boolean isValidRequest = email != null;
            log.info("is request valid? {}", isValidRequest);

            if (!isValidRequest) {
                return buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            User user = userRepo.findByEmail(email);
            if (Objects.isNull(user)) {
                return buildResponse(HttpStatus.BAD_REQUEST, "Sorry, email does not exist");
            }

            if(user.getStatus().equalsIgnoreCase("true")){
                return buildResponse(HttpStatus.BAD_REQUEST, "Account is already active");

            }

            confirmAccount(email);
            return buildResponse(HttpStatus.OK, "Activation code sent successfully. Please check your mail");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }


    /**
     * Deactivate the user's account. This method is used to mark the user's account as inactive.
     *
     * @return A ResponseEntity with HTTP status OK and a success message if the deactivation is successful,
     * or a ResponseEntity with an error message and an HTTP status code (e.g., BAD_REQUEST or INTERNAL_SERVER_ERROR)
     * if an error occurs during the deactivation process.
     * The account deactivation only applies to the currently authenticated user.
     */
    @Override
    public ResponseEntity<String> deactivateAccount() throws JsonProcessingException {
        try {
            String currentUser = jwtFilter.getCurrentUser();
            User user = userRepo.findByEmail(currentUser);
            if (user == null) {
                return buildResponse(HttpStatus.NOT_FOUND, "User is null");
            }

            log.info("Inside deactivateAccount {}", jwtFilter.getCurrentUser());
            user.setStatus("false");
            userRepo.save(user);
            emailUtilities.sendStatusMailToUser("false", "User", user.getEmail());
            simpMessagingTemplate.convertAndSend("/topic/deactivateAccount", user);
            return buildResponse(HttpStatus.OK, "Your account has successfully been deactivated");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Update the status of a user account, either activating or deactivating it.
     *
     * @param id The unique identifier of the user whose account status needs to be updated.
     * @return A ResponseEntity with HTTP status OK and a success message if the status update is successful,
     * or a ResponseEntity with an error message and an HTTP status code (e.g., BAD_REQUEST or INTERNAL_SERVER_ERROR)
     * if an error occurs during the status update process.
     * The status update can only be performed by an admin user.
     */
    @Override
    public ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException {
        try {
            log.info("Inside updateStatus {}", id);
            String adminMail = jwtFilter.getCurrentUser();
            User admin = userRepo.findByEmail(adminMail);

            if (!jwtFilter.isAdmin()) {
                return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!admin.getRole().equalsIgnoreCase("admin") &&
                    admin.getEmail().equalsIgnoreCase(BerlizConstants.BERLIZ_SUPER_ADMIN)) {
                return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            Optional<User> optional = userRepo.findById(id);
            if (optional.isEmpty()) {
                return buildResponse(HttpStatus.NOT_FOUND, "User id not found");
            }

            String status;
            log.info("Inside optional {}", optional);
            User user = optional.get();
            status = user.getStatus();
            String responseMessage;

            if (status.equalsIgnoreCase("true")) {
                status = "false";
                responseMessage = "User: " + user.getEmail() + " has been deactivated successfully";
            } else {
                status = "true";
                responseMessage = "User: " + user.getEmail() + "  has been successfully activated";
            }

            user.setStatus(status);
            userRepo.save(user);
            emailUtilities.sendStatusMailToAdmins(status, optional.get().getEmail(), userRepo.getAllAdminsMail(), "User");
            emailUtilities.sendStatusMailToUser(status, "User", optional.get().getEmail());
            simpMessagingTemplate.convertAndSend("/topic/updateUserStatus", user);
            return buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Update the role of a user.
     *
     * @param requestMap A map containing request parameters.
     * @return A ResponseEntity with a status code and message.
     */
    @Override
    public ResponseEntity<String> updateRole(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            if (!jwtFilter.isAdmin()) {
                return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
            String role = requestMap.get("role");
            if (role == null) {
                return buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }
            Optional<User> optional = userRepo.findById(Integer.parseInt(requestMap.get("id")));
            if (optional.isEmpty()) {
                return buildResponse(HttpStatus.NOT_FOUND, "User id not found");
            }

            User adminUser = userRepo.findByEmail(jwtFilter.getCurrentUser());
            if (!adminUser.getEmail().equalsIgnoreCase("berlizworld@gmail.com")) {
                return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            if (!isValidRole(role)) {
                return buildResponse(HttpStatus.BAD_REQUEST, "Invalid role parameter");
            }

            User user = optional.get();
            user.setRole(role);
            userRepo.save(user);
            emailUtilities.sendRoleMailToAdmins(role, user.getEmail(), userRepo.getAllAdminsMail());
            emailUtilities.sendRoleMailToUser(role, user.getEmail());
            simpMessagingTemplate.convertAndSend("/topic/updateUserRole", user);
            return buildResponse(HttpStatus.OK, "User role updated successfully");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Delete a user account based on their unique identifier (id).
     *
     * @param id The unique identifier of the user account to be deleted.
     * @return A ResponseEntity with HTTP status OK and a success message if the user account is successfully deleted,
     * or a ResponseEntity with an error message and an HTTP status code (e.g., BAD_REQUEST or INTERNAL_SERVER_ERROR)
     * if an error occurs during the deletion process.
     * The deletion of user accounts can only be performed by an admin user, and certain accounts such as the one with
     * the email "berlizworld@gmail.com" cannot be deleted.
     */
    @Override
    public ResponseEntity<String> deleteUser(Integer id) throws JsonProcessingException {
        try {
            if (!jwtFilter.isAdmin()) {
                return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            User adminUser = userRepo.findByEmail(jwtFilter.getCurrentUser());
            if (!adminUser.getEmail().equalsIgnoreCase("berlizworld@gmail.com")) {
                return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            Optional<User> optional = userRepo.findById(id);
            if (optional.isEmpty()) {
                return buildResponse(HttpStatus.NOT_FOUND, "User id not found");
            }

            if (optional.get().getEmail().equalsIgnoreCase("berlizworld@gmail.com")) {
                return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            log.info("inside optional {}", optional);
            User user = optional.get();
            userRepo.delete(user);
            emailUtilities.sendAccountDeletedMail(optional.get().getEmail(), userRepo.getAllAdminsMail());
            simpMessagingTemplate.convertAndSend("/topic/deleteUser", user);
            return buildResponse(HttpStatus.OK, "User account deleted successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Retrieve the details of the currently authenticated user.
     *
     * @return A ResponseEntity with HTTP status OK and the user details in the response body if the user is found,
     * or a ResponseEntity with an error message and an HTTP status code (e.g., NOT_FOUND or INTERNAL_SERVER_ERROR)
     * if the user is not found or if an error occurs during the retrieval process.
     */
    @Override
    public ResponseEntity<?> getUser() throws JsonProcessingException {
        try {
            log.info("Inside getUser");
            Optional<User> optional = userRepo.findById(jwtFilter.getCurrentUserId());
            if (optional.isEmpty()) {
                return buildResponse(HttpStatus.NOT_FOUND, "User is null");

            }
            log.info("Inside optional {}", optional);
            User user = optional.get();
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(user);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Refreshes an access token based on the provided refresh token.
     *
     * @param requestMap The original access token with the "Bearer " prefix.
     * @return ResponseEntity containing the new access token and a message if the access token is created successfully.
     * @throws JsonProcessingException if there is an issue processing JSON data.
     */
    @Override
    public ResponseEntity<String> refreshToken(Map<String, String> requestMap) throws JsonProcessingException {
        log.info("Inside refreshToken {}", requestMap);
        String refreshToken = requestMap.get("token");
        String username = jwtUtility.extractUsername(refreshToken);
        Integer id = jwtUtility.extractUserId(refreshToken);
        UserDetails userDetails = clientUserDetailsService.loadUserByUsername(username);

        try {
            if (refreshToken == null || refreshToken.isEmpty()) {
                return buildResponse(HttpStatus.BAD_REQUEST, "Refresh token is required.");
            }

            if (!jwtUtility.isValidToken(refreshToken, userDetails)) {
                return buildResponse(HttpStatus.UNAUTHORIZED, "Refresh token is invalid");
            }

            String newAccessToken = jwtUtility.generateAccessToken(refreshToken);
            String newRefreshToken = jwtUtility.generateRefreshToken(username, id);

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("access_token", newAccessToken);
            responseBody.put("refresh_token", newRefreshToken);
            responseBody.put("message", "Access token created successfully.");

            ObjectMapper objectMapper = new ObjectMapper();
            String responseBodyJson = objectMapper.writeValueAsString(responseBody);

            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(responseBodyJson);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    @Override
    public ResponseEntity<String> updateBio(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside updateBio {}", requestMap);
            User user = userRepo.findByEmail(jwtFilter.getCurrentUser());
            if (user == null) {
                return buildResponse(HttpStatus.NOT_FOUND, "User is null");
            }

            if (requestMap.get("bio").isEmpty()) {
                return buildResponse(HttpStatus.BAD_REQUEST, "Bio cannot be empty");
            }

            user.setBio(requestMap.get("bio"));
            userRepo.save(user);
            simpMessagingTemplate.convertAndSend("/topic/updateUserBio", user);
            return buildResponse(HttpStatus.OK, "You have successfully updated your Bio");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }


    /**
     * Check if a user token is valid.
     *
     * @return ResponseEntity containing a JSON response indicating token validity.
     * @throws JsonProcessingException If there's an issue with JSON processing.
     */
    @Override
    public ResponseEntity<String> checkToken() throws JsonProcessingException {
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "true");

        ObjectMapper objectMapper = new ObjectMapper();
        String responseBodyJson = objectMapper.writeValueAsString(responseBody);

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseBodyJson);
    }

    /**
     * Change the password for the currently logged-in user.
     *
     * @param requestMap A map containing the "oldPassword" (current password) and "newPassword" (new password) for the user.
     * @return A ResponseEntity<String> indicating the result of the password change operation.
     * @throws JsonProcessingException If there is an issue processing JSON data.
     */
    @Override
    public ResponseEntity<String> changePassword(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside changePassword {}", requestMap);
            User user = userRepo.findByEmail(jwtFilter.getCurrentUser());

            if (user == null) {
                return buildResponse(HttpStatus.NOT_FOUND, "User is null");
            }

            if (!passwordEncoder.matches(requestMap.get("oldPassword"), user.getPassword())) {
                return buildResponse(HttpStatus.BAD_REQUEST, "Incorrect Password, please enter the correct old password");
            }

            user.setPassword(passwordEncoder.encode(requestMap.get("newPassword")));
            userRepo.save(user);
            return buildResponse(HttpStatus.OK, "Password changed successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Change the password for a user by an admin.
     *
     * @param requestMap A map containing the "id" (user ID) and "newPassword" (new password) for the user.
     * @return A ResponseEntity<String> indicating the result of the password change operation.
     * @throws JsonProcessingException If there is an issue processing JSON data.
     */
    @Override
    public ResponseEntity<String> changePasswordAdmin(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside changePasswordAdmin {}", requestMap);
            if (jwtFilter.isAdmin()) {
                if (!(requestMap.containsKey("id") && requestMap.containsKey("newPassword"))) {
                    return buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
                }

                User adminUser = userRepo.findByEmail(jwtFilter.getCurrentUser());
                if (!adminUser.getEmail().equalsIgnoreCase("berlizworld@gmail.com")) {
                    return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
                }

                int userId = Integer.parseInt(requestMap.get("id"));
                Optional<User> optional = userRepo.findById(userId);
                if (optional.isEmpty()) {
                    return buildResponse(HttpStatus.NOT_FOUND, "User not found");
                }
                User user = optional.get();
                user.setPassword(passwordEncoder.encode(requestMap.get("newPassword")));
                userRepo.save(user);
                return buildResponse(HttpStatus.OK, "You have successfully updated " + user.getFirstname() + "'s password");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Initiate the process for resetting a forgotten password by sending a reset password email.
     *
     * @param requestMap A map containing the user's "email" for password reset.
     * @return A ResponseEntity<String> indicating the result of the password reset initiation.
     * @throws JsonProcessingException If there is an issue processing JSON data.
     */
    @Override
    public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            User user = userRepo.findByEmail(requestMap.get("email"));
            if (!Objects.isNull(user)
                    && !Strings.isNullOrEmpty(String.valueOf(user.getEmail()
                    .equalsIgnoreCase(requestMap.get("email"))))) {
                if ("false".equalsIgnoreCase(user.getStatus())) {
                    return buildResponse(HttpStatus.BAD_REQUEST, "Account is inactive");
                }

                emailUtilities.resetPasswordMail(user.getEmail(), "Recover Password");
                return buildResponse(HttpStatus.OK, "Check your email for credentials");
            } else {
                return buildResponse(HttpStatus.NOT_FOUND, "Email not found");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Resets a forgotten password after receiving token via mail.
     *
     * @param requestMap A map containing the user's password credentials.
     * @return A ResponseEntity<String> indicating the result of the password reset.
     * @throws JsonProcessingException If there is an issue processing JSON data.
     */
    @Override
    public ResponseEntity<String> resetPassword(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            User user = userRepo.findByToken(requestMap.get("token"));
            if (user != null) {
                user.setPassword(passwordEncoder.encode(requestMap.get("password")));
                user.setToken("");
                userRepo.save(user);
                return buildResponse(HttpStatus.OK, "You have successfully reset your password");

            } else {
                return buildResponse(HttpStatus.UNAUTHORIZED, BerlizConstants.UNAUTHORIZED_REQUEST);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }


    /**
     * Update user information based on the provided request map.
     *
     * @param requestMap A map containing user information to be updated.
     * @return A ResponseEntity containing a status code and a message indicating the result of the update.
     * @throws JsonProcessingException If there is an issue with JSON processing.
     */
    @Override
    public ResponseEntity<String> updateUser(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside updateUser {}", requestMap);
            User user;
            String responseMessage;

            if (validateUpdateUserMap(requestMap)) {
                return buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            if (jwtFilter.isAdmin()) {
                log.info("Inside jwtFilter.isAdmin() {}", requestMap);
                Integer userId = Integer.valueOf(requestMap.get("id"));
                if (requestMap.containsKey("id")) {
                    Optional<User> optional = userRepo.findById(userId);
                    if (optional.isEmpty()) {
                        return buildResponse(HttpStatus.NOT_FOUND, "User id not found");
                    }

                    user = optional.get();
                    responseMessage = "You have successfully updated " + user.getEmail() + " account information";
                } else {
                    log.info("Inside adminUser {}", requestMap);
                    user = userRepo.findByEmail(jwtFilter.getCurrentUser());
                    if (user == null) {
                        return buildResponse(HttpStatus.NOT_FOUND, "User is null");
                    }
                    responseMessage = "Hello " + user.getEmail() + "you have successfully updated your account information";
                }
            } else {
                log.info("Inside user {}", requestMap);
                // Find the user based on the JWT token
                user = userRepo.findByEmail(jwtFilter.getCurrentUser());
                if (user == null) {
                    return buildResponse(HttpStatus.NOT_FOUND, "User is null");
                }
                responseMessage = "Hello " + user.getEmail() + "you have successfully updated your account information";
            }

            updateUserFromMap(user, requestMap);
            userRepo.save(user);
            simpMessagingTemplate.convertAndSend("/topic/updateUser", user);
            return buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Update user information by an admin based on the provided request map.
     *
     * @param requestMap A map containing user information to be updated.
     * @return A ResponseEntity containing a status code and a message indicating the result of the update.
     * @throws JsonProcessingException If there is an issue with JSON processing.
     */
    @Override
    public ResponseEntity<String> updateSuperUser(Map<String, String> requestMap) throws JsonProcessingException {
        try {
            log.info("Inside updateSuperUser {}", requestMap);
            User superUser = userRepo.findByEmail(jwtFilter.getCurrentUser());
            log.info("Inside updateUserAdmin {}", requestMap);
            if (validateUpdateUserMap(requestMap)) {
                return buildResponse(HttpStatus.BAD_REQUEST, BerlizConstants.INVALID_DATA);
            }

            if (!jwtFilter.isAdmin()) {
                return buildResponse(HttpStatus.NOT_FOUND, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            log.info("Inside jwtFilter.isAdmin() {}", requestMap);
            Integer userId = Integer.valueOf(requestMap.get("id"));
            Optional<User> optional = userRepo.findById(userId);

            if (optional.isEmpty()) {
                return buildResponse(HttpStatus.NOT_FOUND, "User id not found");
            }

            if (optional.get().getRole().equalsIgnoreCase("admin")
                    && !superUser.getEmail().equalsIgnoreCase(BerlizConstants.BERLIZ_SUPER_ADMIN)) {
                return buildResponse(HttpStatus.UNAUTHORIZED, "You are not authorized to update super user");
            }

            User user = optional.get();
            updateUserFromMap(user, requestMap);
            userRepo.save(user);
            simpMessagingTemplate.convertAndSend("/topic/updateUser", user);
            return buildResponse(HttpStatus.OK, user.getFirstname() + " information updated successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Update a user's profile photo by an admin.
     *
     * @param request The ProfilePhotoRequestAdmin object containing the user's ID and profile photo data.
     * @return A ResponseEntity containing a status code and a message indicating the result of the update.
     * @throws JsonProcessingException If there is an issue with JSON processing.
     */
    @Override
    public ResponseEntity<String> updateProfilePhotoByAdmin(ProfilePhotoRequest request) throws JsonProcessingException {
        try {
            log.info("Inside updateProfilePhoto{}", request);
            MultipartFile file = request.getProfilePhoto();
            if (file == null) {
                return buildResponse(HttpStatus.BAD_REQUEST, "No profile photo provided");
            }

            if (!fileUtilities.isValidImageType(file)) {
                return buildResponse(HttpStatus.BAD_REQUEST, "Invalid file type");
            }

            if (!fileUtilities.isValidImageSize(file)) {
                return buildResponse(HttpStatus.BAD_REQUEST, "Invalid file type");
            }

            if (!jwtFilter.isAdmin()) {
                return buildResponse(HttpStatus.NOT_FOUND, BerlizConstants.UNAUTHORIZED_REQUEST);
            }

            log.info("Inside jwtFilter.isAdmin() {}", jwtFilter.isAdmin());
            Integer userId = request.getId();
            Optional<User> optional = userRepo.findById(userId);
            if (optional.isEmpty()) {
                return buildResponse(HttpStatus.NOT_FOUND, "User id not found");
            }

            User user = optional.get();
            user.setProfilePhoto(file.getBytes());
            userRepo.save(user);
            simpMessagingTemplate.convertAndSend("/topic/updateProfilePhoto", user);
            return buildResponse(HttpStatus.OK, user.getFirstname() + "'s profile photo updated successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Update a user's profile photo.
     *
     * @param request The ProfilePhotoRequest object containing the user's profile photo data.
     * @return A ResponseEntity containing a status code and a message indicating the result of the update.
     * @throws JsonProcessingException If there is an issue with JSON processing.
     */
    @Override
    public ResponseEntity<String> updateProfilePhoto(ProfilePhotoRequest request) throws JsonProcessingException {
        try {
            log.info("Inside updateProfilePhoto{}", request);
            User user;
            if (jwtFilter.isAdmin()) {
                user = userRepo.findByUserId(request.getId());
            } else {
                user = userRepo.findByEmail(jwtFilter.getCurrentUser());
            }

            if (user == null) {
                return buildResponse(HttpStatus.NOT_FOUND, "User is null");
            }

            log.info("Inside currentUser {}", jwtFilter.getCurrentUser());
            MultipartFile file = request.getProfilePhoto();

            if (file == null) {
                return buildResponse(HttpStatus.BAD_REQUEST, "No profile photo provided");
            }

            if (!fileUtilities.isValidImageType(file)) {
                return buildResponse(HttpStatus.BAD_REQUEST, "Invalid file type");
            }

            if (!fileUtilities.isValidImageSize(file)) {
                return buildResponse(HttpStatus.BAD_REQUEST, "Invalid file type");
            }

            String responseMessage;
            user.setProfilePhoto(file.getBytes());
            userRepo.save(user);

            if (jwtFilter.isAdmin()) {
                responseMessage = user.getFirstname() + " profile photo has successfully " + "been updated";
            } else {
                responseMessage = "Hello " + user.getFirstname() + ", you have successfully " + "updated your profile photo";
            }

            simpMessagingTemplate.convertAndSend("/topic/updateProfilePhoto", user);
            return buildResponse(HttpStatus.OK, responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Create a User object from the provided SignupRequest data.
     *
     * @param request The SignupRequest object containing user registration data.
     * @throws IOException If there is an issue with reading the profile photo data.
     */
    private void getUserFromMap(SignupRequest request) throws IOException {
        User user = new User();
        byte[] image = request.getProfilePhoto().getBytes();

        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setPhone(request.getPhone());
        user.setDob(request.getDob());
        user.setGender(request.getGender());
        user.setCountry(request.getCountry());
        user.setState(request.getState());
        user.setCity(request.getCity());
        user.setAddress(request.getAddress());
        user.setPostalCode(request.getPostalCode());

        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("user");
        user.setProfilePhoto(image);
        user.setStatus("false");
        user.setDate(new Date());
        user.setLastUpdate(new Date());
        User savedUser = userRepo.save(user);
        simpMessagingTemplate.convertAndSend("/topic/getUserFromMap", savedUser);
    }

    /**
     * Validate the request map to ensure it contains all the required fields for user information update.
     *
     * @param requestMap The map containing user information to be validated.
     * @return True if the request map contains all required fields, false otherwise.
     */
    private boolean validateUpdateUserMap(Map<String, String> requestMap) {
        // Check if the requestMap contains all the required fields to make an update

        return !requestMap.containsKey("firstname")
                || !requestMap.containsKey("lastname")
                || !requestMap.containsKey("phone")
                || !requestMap.containsKey("dob")
                || !requestMap.containsKey("gender")
                || !requestMap.containsKey("country")
                || !requestMap.containsKey("state")
                || !requestMap.containsKey("city")
                || !requestMap.containsKey("address")
                || !requestMap.containsKey("bio")
                || !requestMap.containsKey("postalCode");
    }

    // Helper method to update user entity with data from the request map

    /**
     * Update a user entity with information from the given request map.
     *
     * @param user       The user entity to update.
     * @param requestMap The map containing updated user information.
     */
    private void updateUserFromMap(User user, Map<String, String> requestMap) {

        user.setFirstname(requestMap.get("firstname"));
        user.setLastname(requestMap.get("lastname"));
        user.setPhone(requestMap.get("phone"));
        user.setDob(requestMap.get("dob"));
        user.setGender(requestMap.get("gender"));
        user.setCountry(requestMap.get("country"));
        user.setState(requestMap.get("state"));
        user.setCity(requestMap.get("city"));
        user.setAddress(requestMap.get("address"));
        user.setBio(requestMap.get("bio"));
        user.setPostalCode(Integer.parseInt(requestMap.get("postalCode")));
        user.setLastUpdate(new Date());
        simpMessagingTemplate.convertAndSend("/topic/updateUserFromMap", user);
    }

    /**
     * Validate if the provided role is valid.
     *
     * @param role The role to validate.
     * @return True if the role is valid, false otherwise.
     */
    private boolean isValidRole(String role) {
        // Customize this validation logic as per your roles
        return role != null && (role.equalsIgnoreCase("admin") || role.equalsIgnoreCase("user")
                || role.equalsIgnoreCase("client") || role.equalsIgnoreCase("partner")
                || role.equalsIgnoreCase("center") || role.equalsIgnoreCase("trainer")
                || role.equalsIgnoreCase("store") || role.equalsIgnoreCase("driver"));
    }

    /**
     * Build a ResponseEntity with the given status code and message.
     *
     * @param status  The HTTP status code.
     * @param message The response message.
     * @return A ResponseEntity with the specified status and message.
     * @throws JsonProcessingException If there is an issue processing the JSON response.
     */
    private ResponseEntity<String> buildResponse(HttpStatus status, String message) throws JsonProcessingException {
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", message);

        ObjectMapper objectMapper = new ObjectMapper();
        String responseBodyJson = objectMapper.writeValueAsString(responseBody);

        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseBodyJson);
    }


}

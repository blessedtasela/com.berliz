package com.berliz.services;

import com.berliz.DTO.ProfilePhotoRequest;
import com.berliz.DTO.SignupRequest;
import com.berliz.models.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface UserService {

    ResponseEntity<String> signUp(SignupRequest request) throws JsonProcessingException;

    void confirmAccount(String email) throws JsonProcessingException;

    ResponseEntity<String> activateAccount(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> login(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<List<User>> getAllUsers();

    ResponseEntity<String> deactivateAccount() throws JsonProcessingException;

    ResponseEntity<String> updateStatus(Integer id) throws JsonProcessingException;

    ResponseEntity<String> checkToken() throws JsonProcessingException;

    ResponseEntity<String> changePassword(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> changePasswordAdmin(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> forgotPassword(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> resetPassword(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> updateUser(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> updateSuperUser(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> updateProfilePhotoByAdmin(ProfilePhotoRequest request) throws JsonProcessingException;

    ResponseEntity<String> removePhoto(Integer id) throws JsonProcessingException;

    ResponseEntity<String> updateProfilePhoto(ProfilePhotoRequest request) throws JsonProcessingException;

    ResponseEntity<String> updateRole(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> deleteUser(Integer id) throws JsonProcessingException;

    ResponseEntity<?> getUser() throws JsonProcessingException;

    ResponseEntity<String> refreshToken(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> updateBio(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> validateEmail(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> updateEmail(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<List<User>> getActiveUsers();

    ResponseEntity<String> quickAdd(Map<String, String> requestMap) throws JsonProcessingException;

    ResponseEntity<String> sendActivationToken(String email) throws JsonProcessingException;

    ResponseEntity<String> forcePasswordChange(Integer id, String password) throws JsonProcessingException;

}

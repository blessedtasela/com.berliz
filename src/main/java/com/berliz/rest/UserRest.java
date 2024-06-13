package com.berliz.rest;

import com.berliz.DTO.ProfilePhotoRequest;
import com.berliz.DTO.SignupRequest;
import com.berliz.models.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RequestMapping("/user")
public interface UserRest {

    @PostMapping(path = "/signup")
    ResponseEntity<String> signUp(@ModelAttribute SignupRequest request);

    @PostMapping(path = "/quickAdd")
    ResponseEntity<String> quickAdd(@RequestBody() Map<String, String> requestMap);

    @PostMapping(path = "/sendActivationToken/{email}")
    ResponseEntity<String> sendActivationToken(@PathVariable() String email);

    @PostMapping(path = "/login")
    ResponseEntity<String> login(@RequestBody() Map<String, String> requestMap);

    @GetMapping(path = "/get")
    ResponseEntity<List<User>> getAllUsers();

    @GetMapping(path = "/getActiveUsers")
    ResponseEntity<List<User>> getActiveUsers();

    @PutMapping(path = "/updateStatus/{id}")
    ResponseEntity<String> updateStatus(@PathVariable Integer id);

    @GetMapping(path = "/checkToken")
    ResponseEntity<String> checkToken();

    @PostMapping(path = "/refreshToken")
    ResponseEntity<String> refreshToken(@RequestBody Map<String, String> requestMap);

    @PutMapping(path = "/changePassword")
    ResponseEntity<String> changePassword(@RequestBody() Map<String, String> requestMap);

    @PutMapping(path = "/changePasswordAdmin")
    ResponseEntity<String> changePasswordAdmin(@RequestBody() Map<String, String> requestMap);

    @PostMapping(path = "/forgotPassword")
    ResponseEntity<String> forgotPassword(@RequestBody() Map<String, String> requestMap);

    @PutMapping(path = "/resetPassword")
    ResponseEntity<String> resetPassword(@RequestBody() Map<String, String> requestMap);

    @PutMapping(path = "/activateAccount")
    ResponseEntity<String> activateAccount(@RequestBody() Map<String, String> requestMap);

    @PutMapping(path = "/deactivateAccount")
    ResponseEntity<String> deactivateAccount();

    @PutMapping(path = "/update")
    ResponseEntity<String> updateUser(@RequestBody() Map<String, String> requestMap);

    @PutMapping(path = "/updateSuperUser")
    ResponseEntity<String> updateSuperUser(@RequestBody Map<String, String> requestMap);

    @PutMapping(path = "/updateBio")
    ResponseEntity<String> updateBio(@RequestBody Map<String, String> requestMap);

    @PutMapping(path = "/validateEmail")
    ResponseEntity<String> validateEmail(@RequestBody Map<String, String> requestMap);

    @PutMapping(path = "/updateEmail")
    ResponseEntity<String> updateEmail(@RequestBody Map<String, String> requestMap);

    @PutMapping(path = "/updateProfilePhoto")
    ResponseEntity<String> updateProfilePhoto(@ModelAttribute ProfilePhotoRequest request);

    @PutMapping(path = "/removePhoto/{id}")
    ResponseEntity<String> removePhoto(@PathVariable Integer id);

    @PutMapping(path = "/updateProfilePhotoAdmin")
    ResponseEntity<String> updateProfilePhotoByAdmin(@ModelAttribute ProfilePhotoRequest request);

    @PutMapping(path = "/updateRole")
    ResponseEntity<String> updateRole(@RequestBody() Map<String, String> requestMap);

    @GetMapping(path = "/getUser")
    ResponseEntity<?> getUser();

    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteUser(@PathVariable Integer id);

    @PutMapping(path = "/forcePasswordChange/{id}/{password}")
    ResponseEntity<String> forcePasswordChange(@PathVariable Integer id, @PathVariable String password);

}

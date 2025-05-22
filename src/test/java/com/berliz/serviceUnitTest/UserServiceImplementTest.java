package com.berliz.serviceUnitTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.berliz.DTO.SignupRequest;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.User;
import com.berliz.repositories.UserRepo;
import com.berliz.serviceImplement.UserServiceImplement;
import com.berliz.utils.EmailUtilities;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)  // Enables Mockito JUnit 5 support
public class UserServiceImplementTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private EmailUtilities emailUtilities;

    @InjectMocks
    private UserServiceImplement userService;

    private SignupRequest request;

    @BeforeEach
    void setUp() {
        request = new SignupRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
    }

    @Test
    void signUp_ShouldReturnSuccess_WhenUserDoesNotExist() throws JsonProcessingException {
        // Arrange
        when(userRepo.findByEmail(request.getEmail())).thenReturn(null);

        // Act
        ResponseEntity<String> response = userService.signUp(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains(BerlizConstants.SIGNUP_SUCCESS));
    }

    @Test
    void signUp_ShouldReturnBadRequest_WhenUserAlreadyExists() throws JsonProcessingException {
        // Arrange
        User existingUser = new User();
        existingUser.setEmail(request.getEmail());
        when(userRepo.findByEmail(request.getEmail())).thenReturn(existingUser);

        // Act
        ResponseEntity<String> response = userService.signUp(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains(BerlizConstants.EMAIL_EXISTS));
    }
}

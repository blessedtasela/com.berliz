package com.berliz.serviceIntegrationTest;

import com.berliz.DTO.SignupRequest;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.User;
import com.berliz.repositories.UserRepo;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class UserServiceImplementTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    User user = new User();
    SignupRequest signupRequest = new SignupRequest();
    String token;

    @BeforeEach
    public void setUp() {
//        userRepository.deleteAll(); // Ensure clean DB state

        signupRequest = new SignupRequest();
        signupRequest.setFirstname("John");
        signupRequest.setLastname("Doe");
        signupRequest.setPhone("123-456-7890");
        signupRequest.setDob("1990-01-01");
        signupRequest.setGender("Male");
        signupRequest.setCountry("USA");
        signupRequest.setState("California");
        signupRequest.setCity("Los Angeles");
        signupRequest.setPostalCode(90001);
        signupRequest.setAddress("123 Main St, Los Angeles, CA");
        signupRequest.setEmail("john.doe@example.com");
        signupRequest.setPassword("password123");

        // Mock profile photo
        byte[] profilePhotoBytes = new byte[]{1, 2, 3};
        MockMultipartFile profilePhoto = new MockMultipartFile("profilePhoto", "photo.jpg", "image/jpeg", profilePhotoBytes);
        signupRequest.setProfilePhoto(profilePhoto);

        // Assign user instance
        this.user = getUser();
        userRepository.save(user);
    }


    @Test
    public void testSignup() throws Exception {
        // Ensure the user doesn't already exist in the database
        if (userRepository.findByEmail(signupRequest.getEmail()) == null) {
            // If email doesn't exist, perform the signup
            mockMvc.perform(multipart("/user/signup")
                            .file("profilePhoto", signupRequest.getProfilePhoto().getBytes()) // Pass the file data correctly
                            .param("firstname", signupRequest.getFirstname())
                            .param("lastname", signupRequest.getLastname())
                            .param("phone", signupRequest.getPhone())
                            .param("dob", signupRequest.getDob())
                            .param("gender", signupRequest.getGender())
                            .param("country", signupRequest.getCountry())
                            .param("state", signupRequest.getState())
                            .param("city", signupRequest.getCity())
                            .param("postalCode", String.valueOf(signupRequest.getPostalCode()))
                            .param("address", signupRequest.getAddress())
                            .param("email", signupRequest.getEmail())
                            .param("password", signupRequest.getPassword())
                            .param("role", "admin")
                            .param("status", "true")
                            .param("date", String.valueOf(new Date()))
                            .contentType(MediaType.MULTIPART_FORM_DATA)) // Set the content type
                    .andExpect(status().isCreated()) // Expect 201 for successful signup
                    .andExpect(jsonPath("$.message").value(BerlizConstants.SIGNUP_SUCCESS));
        } else {
            // If email exists, return bad request
            mockMvc.perform(multipart("/user/signup")
                            .file("profilePhoto", signupRequest.getProfilePhoto().getBytes()) // Pass the file data correctly
                            .param("firstname", signupRequest.getFirstname())
                            .param("lastname", signupRequest.getLastname())
                            .param("phone", signupRequest.getPhone())
                            .param("dob", signupRequest.getDob())
                            .param("gender", signupRequest.getGender())
                            .param("country", signupRequest.getCountry())
                            .param("state", signupRequest.getState())
                            .param("city", signupRequest.getCity())
                            .param("postalCode", String.valueOf(signupRequest.getPostalCode()))
                            .param("address", signupRequest.getAddress())
                            .param("email", signupRequest.getEmail())
                            .param("password", signupRequest.getPassword())
                            .param("role", "admin")
                            .param("status", "true")
                            .param("date", String.valueOf(new Date()))
                            .contentType(MediaType.MULTIPART_FORM_DATA)) // Set the content type
                    .andExpect(status().isBadRequest()) // Expect 400 if email exists
                    .andExpect(jsonPath("$.message").value(BerlizConstants.EMAIL_EXISTS));
        }
    }

    @Test
    public void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/users/" + user.getId()))
                .andExpect(status().isNoContent());

        assertFalse(userRepository.findById(user.getId()).isPresent());
    }

    @Test
    public void testLogin() throws Exception {
        // Create the login request body as a JSON string
        String loginRequestBody = "{ \"email\": \"" + signupRequest.getEmail() + "\", \"password\": \"" + signupRequest.getPassword() + "\" }";

        // Perform the login request and extract the JWT token from the response header
        MvcResult result = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestBody)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String tkn = result.getResponse().getHeader("Authorization");

        if (tkn == null || tkn.isEmpty()) {
            System.out.println("Authorization header is missing in response!");
            throw new IllegalStateException("JWT token is missing or empty.");
        }


        token = tkn;
    }

    @Test
    public void testGetAllUsers() throws Exception {
        System.out.println("Received Token: " + token);
        // Perform the request with the Authorization header
        mockMvc.perform(get("/user/get")
                        .header("Authorization", "Bearer " + token))  // Include token in Authorization header
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstname").value(signupRequest.getFirstname()))
                .andExpect(jsonPath("$[0].lastname").value(signupRequest.getLastname()));
    }

    @Test
    public void testGetUserById() throws Exception {

        // Perform the request with the Authorization header
        mockMvc.perform(get("/users/" + getUser().getId())
                        .header("Authorization", "Bearer " + token))  // Include token in Authorization header
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(getUser().getId()))
                .andExpect(jsonPath("$.firstname").value(signupRequest.getFirstname()))
                .andExpect(jsonPath("$.lastname").value("Doe"));
    }

    @Test
    public void testUpdateUser() throws Exception {
        // Update user details
        user.setFirstname("Eve");
        user.setLastname("Updated");
        user.setEmail("eve.updated@example.com");

        mockMvc.perform(put("/users/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname").value("Eve"))
                .andExpect(jsonPath("$.lastname").value("Updated"));
    }


    private User getUser() {
        User user = new User();
        user.setFirstname(signupRequest.getFirstname());
        user.setLastname(signupRequest.getLastname());
        user.setPhone(signupRequest.getPhone());
        user.setDob(signupRequest.getDob());
        user.setGender(signupRequest.getGender());
        user.setCountry(signupRequest.getCountry());
        user.setState(signupRequest.getState());
        user.setCity(signupRequest.getCity());
        user.setPostalCode(signupRequest.getPostalCode());
        user.setAddress(signupRequest.getAddress());
        user.setEmail(signupRequest.getEmail());

        // Encode password before storing
        String encodedPassword = bCryptPasswordEncoder.encode(signupRequest.getPassword());
        user.setPassword(encodedPassword);
        user.setRole("admin");
        user.setStatus("true");
        user.setDate(new Date());
        return user;
    }
}

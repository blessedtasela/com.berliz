package com.berliz.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Data
public class SignupRequest {

    private String firstname;
    private String lastname;
    private String phone;
    private String  dob;
    private String gender;
    private String country;
    private String state;
    private String city;
    private Integer postalCode;
    private String address;
    private String email;
    private String password;
    private MultipartFile profilePhoto;
}

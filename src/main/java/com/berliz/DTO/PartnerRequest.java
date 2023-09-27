package com.berliz.DTO;

import com.berliz.models.User;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PartnerRequest {
    private Integer id;
    private String email;
    private MultipartFile certificate;
    private String motivation;
    private MultipartFile  cv;
    private String facebookUrl;
    private String instagramUrl;
    private String youtubeUrl;
    private String role;
}

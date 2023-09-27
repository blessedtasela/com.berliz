package com.berliz.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProfilePhotoRequest {
    private Integer id;
    private MultipartFile profilePhoto;

}

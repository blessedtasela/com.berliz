package com.berliz.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class TrainerRequest {
    private Integer id;
    private Integer partnerId;
    private String name;
    private String motto;
    private String  address;
    private String  experience;
    private MultipartFile photo;
    private String categoryIds;
}




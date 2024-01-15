package com.berliz.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class MuscleGroupRequest {
    private Integer id;
    private String name;
    private String bodyPart;
    private String description;
    private String exercises;
    private MultipartFile image;
}

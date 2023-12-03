package com.berliz.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ExerciseRequest {
    private String name;
    private String description;
    private String muscleGroups;
    private String categories;
    private MultipartFile demo;
}

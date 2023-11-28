package com.berliz.DTO;

import com.berliz.models.Exercise;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Data
public class MuscleGroupRequest {
    private String name;
    private String bodyPart;
    private String description;
    private String exercises;
    private MultipartFile image;
}

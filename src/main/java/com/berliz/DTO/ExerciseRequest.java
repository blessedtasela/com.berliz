package com.berliz.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ExerciseRequest {
    private Integer id;
    private String name;
    private String description;
    private String muscleGroups;
    private String categories;
    private MultipartFile demo;

    public boolean isValidRequest(boolean checkId) {
        if (checkId) {
            // If id is required, check if it is not null
            if (id == null) {
                return false;
            }
        }

        // Check other fields for null or empty values
        return name != null
                && description != null
                && muscleGroups != null
                && categories != null
                && demo != null;
    }
}

package com.berliz.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class AnnouncementRequest {
    private Integer id;
    private Integer centerId;
    private MultipartFile icon;
    private String status;
    private String announcement;

    public boolean isValidRequest(boolean checkId) {
        if (checkId) {
            // If id is required, check if it is not null
            if (id == null) {
                return false;
            }
        }

        // Check other fields for null or empty values
        return centerId != null
                && icon != null
                && status != null
                && announcement != null;
    }
}

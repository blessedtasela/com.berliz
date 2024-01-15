package com.berliz.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class EquipmentRequest {

    private Integer id;
    private Integer centerId;
    private MultipartFile image;
    private MultipartFile sideView;
    private MultipartFile rearView;
    private MultipartFile frontView;
    private String name;
    private Integer stockNumber;
    private String description;
    private String categoryIds;

    public boolean isValidRequest(boolean checkId) {
        if (checkId) {
            // If id is required, check if it is not null
            if (id == null) {
                return false;
            }
        }

        // Check other fields for null or empty values
        return centerId != null
                && image != null
                && sideView != null
                && rearView != null
                && frontView != null
                && name != null
                && stockNumber != null
                && description != null
                && categoryIds != null;
    }
}

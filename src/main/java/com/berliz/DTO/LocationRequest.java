package com.berliz.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class LocationRequest {
    private Integer id;
    private Integer centerId;
    private String subName;
    private String locationUrl;
    private String address;
    private MultipartFile coverPhoto;

    public boolean isValidRequest(boolean checkId) {
        if (checkId) {
            // If id is required, check if it is not null
            if (id == null) {
                return false;
            }
        }

        // Check other fields for null or empty values
        return subName != null
                && locationUrl != null
                && address != null
                && coverPhoto != null;
    }
}

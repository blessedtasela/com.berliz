package com.berliz.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CenterRequest {
    private Integer id;
    private Integer partnerId;
    private String name;
    private String motto;
    private String  address;
    private String  experience;
    private MultipartFile photo;
    private String categoryIds;
    private String location;

    public boolean isValidRequest(boolean checkId) {
        if (checkId) {
            // If id is required, check if it is not null
            if (id == null) {
                return false;
            }
        }

        // Check other fields for null or empty values
        return partnerId != null
                && name != null
                && motto != null
                && address != null
                && experience != null
                && photo != null
                && categoryIds != null
                && location != null;
    }
}

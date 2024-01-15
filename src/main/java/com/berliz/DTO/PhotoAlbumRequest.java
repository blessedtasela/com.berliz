package com.berliz.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PhotoAlbumRequest {
    private Integer id;
    private Integer centerId;
    private Integer trainerId;
    private MultipartFile photo;
    private String comment;

    public boolean isValidRequest(boolean checkId) {
        if (checkId) {
            // If id is required, check if it is not null
            if (id == null) {
                return false;
            }
        }

        // Check other fields for null or empty values
        return photo != null
                && comment != null;
    }
}

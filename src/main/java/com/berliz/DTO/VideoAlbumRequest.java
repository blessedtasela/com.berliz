package com.berliz.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class VideoAlbumRequest {
    private Integer id;
    private Integer centerId;
    private Integer trainerId;
    private MultipartFile video;
    private String comment;

    public boolean isValidRequest(boolean checkId) {
        if (checkId) {
            // If id is required, check if it is not null
            if (id == null) {
                return false;
            }
        }

        // Check other fields for null or empty values
        return video != null
                && comment != null;
    }
}

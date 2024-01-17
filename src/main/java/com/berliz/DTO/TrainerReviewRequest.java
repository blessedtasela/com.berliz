package com.berliz.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class TrainerReviewRequest {
    Integer id;
    Integer trainerId;
    Integer clientId;
    MultipartFile frontBefore;
    MultipartFile frontAfter;
    MultipartFile sideBefore;
    MultipartFile sideAfter;
    MultipartFile backBefore;
    MultipartFile backAfter;
    String review;
    Integer likes;

    public boolean isValidRequest(boolean checkId) {
        if (checkId) {
            // If id is required, check if it is not null
            if (id == null) {
                return false;
            }
        }

        // Check other fields for null or empty values
        return trainerId != null
                && clientId != null
                && frontBefore != null
                && frontAfter != null
                && sideBefore != null
                && sideAfter != null
                && backBefore != null
                && backAfter != null
                && review != null
                && likes != null;
    }
}

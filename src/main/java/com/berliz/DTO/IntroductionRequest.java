package com.berliz.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class IntroductionRequest {
    private Integer id;
    private Integer centerId;
    private Integer trainerId;
    private MultipartFile coverPhoto;
    private String introduction;

    public boolean isValidRequest(boolean checkId) {
        // If id is required, check if it is not null
        if (checkId && id == null) {
            return false;
        }

        // Check other fields for null or empty values
        return coverPhoto != null
                && introduction != null;
    }
}

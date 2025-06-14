package com.berliz.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class TrainerPhotoAlbumRequest {
    private Integer id;
    private Integer trainerId;
    private List<MultipartFile> photos;
    private String comment;
    
    public boolean isValidRequest() {
        return trainerId != null && comment != null && photos != null && photos.size() >= 9 && photos.size() <= 15;
    }
}

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
}

package com.berliz.DTO;

import org.springframework.web.multipart.MultipartFile;

public class FeatureVideo {

    private Integer id;
    private Integer centerId;
    private Integer trainerId;
    private MultipartFile video;
    private String motivation;
}

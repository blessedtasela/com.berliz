package com.berliz.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class Introduction {
    private Integer id;
    private Integer centerId;
    private Integer trainerId;
    private MultipartFile coverPhoto;
    private String introduction;
}

package com.berliz.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ClientReview {

    Integer id;
    Integer trainerId;
    Integer clientId;
    MultipartFile frontBefore;
    MultipartFile frontAfter;
    MultipartFile sideBefore;
    MultipartFile sideAfter;
    MultipartFile backBefore;
    MultipartFile backAfter;
    MultipartFile review;
}

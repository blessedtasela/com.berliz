package com.berliz.DTO;

import lombok.Data;

@Data
public class StrapiPhotoMetadata {
    private Integer id;
    private String photoUrl;
    private String name;
    private String mimeType;
    private Long byteSize;
    private String caption;
}
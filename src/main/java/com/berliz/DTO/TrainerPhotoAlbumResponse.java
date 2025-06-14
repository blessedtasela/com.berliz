package com.berliz.DTO;

import com.berliz.models.Photo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainerPhotoAlbumResponse {
    private Integer id;
    private String comment;
    private Date date;
    private Date lastUpdate;

    private TrainerSummary trainer;
    private List<Photo> photos;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TrainerSummary {
        private Integer id;
        private String name;
    }

    public static class PhotoMetadata {
        private String photoUrl;
        private String name;
        private String caption;
        private String mimeType;
        private Long byteSize;
    }
}

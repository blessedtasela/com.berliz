package com.berliz.utils;

import com.berliz.DTO.TrainerPhotoAlbumResponse;
import com.berliz.models.Photo;
import com.berliz.models.Trainer;
import com.berliz.models.TrainerPhotoAlbum;

import java.util.List;

public class TrainerUtilities {

    // Static utility method to convert an entity to a DTO
    public static TrainerPhotoAlbumResponse trainerPhotoAlbumResponseWithPhotos(TrainerPhotoAlbum album, List<Photo> photos) {
        Trainer trainer = album.getTrainer();

        TrainerPhotoAlbumResponse.TrainerSummary trainerSummary =
                new TrainerPhotoAlbumResponse.TrainerSummary(
                        trainer.getId(),
                        trainer.getName()
                );

        return new TrainerPhotoAlbumResponse(
                album.getId(),
                album.getComment(),
                album.getDate(),
                album.getLastUpdate(),
                trainerSummary,
                photos
        );
    }
}

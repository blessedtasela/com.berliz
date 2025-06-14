package com.berliz.repositories;

import com.berliz.models.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PhotoRepo extends JpaRepository<Photo, Integer> {

    @Query(name = "Photo.getPhotosByTrainerPhotoAlbum")
    List<Photo> getPhotosByTrainerPhotoAlbum(@Param("id") Integer albumId);

    @Query(name = "Photo.getAllTrainerPhotoAlbumPhotos")
    List<Photo> getAllTrainerPhotoAlbumPhotos(@Param("owner") String ownerType);

    List<Photo> findByOwnerTypeAndOwnerId(String ownerType, Integer ownerId);

    void deleteByOwnerTypeAndOwnerId(String ownerType, Integer ownerId);


}

package com.berliz.repository;

import com.berliz.models.Center;
import com.berliz.models.CenterPhotoAlbum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CenterPhotoAlbumRepo extends JpaRepository<CenterPhotoAlbum, Integer> {

    CenterPhotoAlbum findByUuid(String uuid);

    List<CenterPhotoAlbum> findByCenter(Center center);
}

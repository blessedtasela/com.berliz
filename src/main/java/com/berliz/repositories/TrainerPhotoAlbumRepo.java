package com.berliz.repositories;

import com.berliz.models.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainerPhotoAlbumRepo extends JpaRepository<TrainerPhotoAlbum, Integer> {

    TrainerPhotoAlbum findByUuid(String uuid);

    List<TrainerPhotoAlbum> findByTrainer(Trainer trainer);
}

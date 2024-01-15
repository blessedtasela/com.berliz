package com.berliz.repositories;

import com.berliz.models.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainerVideoAlbumRepo extends JpaRepository<TrainerVideoAlbum, Integer> {

    TrainerVideoAlbum findByUuid(String uuid);

    List<TrainerVideoAlbum> findByTrainer(Trainer trainer);
}

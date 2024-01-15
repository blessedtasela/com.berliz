package com.berliz.repositories;

import com.berliz.models.Center;
import com.berliz.models.CenterVideoAlbum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CenterVideoAlbumRepo extends JpaRepository<CenterVideoAlbum, Integer> {

    CenterVideoAlbum findByUuid(String uuid);

    List<CenterVideoAlbum> findByCenter(Center center);
}

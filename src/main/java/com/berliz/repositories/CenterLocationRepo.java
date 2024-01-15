package com.berliz.repositories;

import com.berliz.models.Center;
import com.berliz.models.CenterLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CenterLocationRepo extends JpaRepository<CenterLocation, Integer> {

    CenterLocation findBySubName(String subName);

    List<CenterLocation> findByCenter(Center center);
}

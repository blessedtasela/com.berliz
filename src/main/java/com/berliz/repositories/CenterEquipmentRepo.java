package com.berliz.repositories;

import com.berliz.models.Center;
import com.berliz.models.CenterEquipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CenterEquipmentRepo extends JpaRepository<CenterEquipment, Integer> {

    CenterEquipment findByName(String name);

    List<CenterEquipment> findByCenter(Center center);
}

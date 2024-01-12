package com.berliz.repository;

import com.berliz.models.Center;
import com.berliz.models.CenterIntroduction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CenterIntroductionRepo extends JpaRepository<CenterIntroduction, Integer> {

    CenterIntroduction findByIntroduction(String introduction);

    List<CenterIntroduction> findByCenter(Center center);
}

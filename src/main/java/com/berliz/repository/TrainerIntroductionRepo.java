package com.berliz.repository;

import com.berliz.models.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainerIntroductionRepo extends JpaRepository<TrainerIntroduction, Integer> {

    TrainerIntroduction findByIntroduction(String introduction);

    TrainerIntroduction findByTrainer(Trainer trainer);
}

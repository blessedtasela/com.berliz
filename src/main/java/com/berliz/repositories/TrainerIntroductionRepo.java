package com.berliz.repositories;

import com.berliz.models.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerIntroductionRepo extends JpaRepository<TrainerIntroduction, Integer> {

    TrainerIntroduction findByIntroduction(String introduction);

    TrainerIntroduction findByTrainer(Trainer trainer);
}

package com.berliz.repositories;

import com.berliz.models.Trainer;
import com.berliz.models.TrainerFeatureVideo;
import com.berliz.models.TrainerIntroduction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainerFeatureVideoRepo extends JpaRepository<TrainerFeatureVideo, Integer> {

    TrainerIntroduction findByMotivation(String motivation);

    TrainerFeatureVideo findByTrainer(Trainer trainer);

}

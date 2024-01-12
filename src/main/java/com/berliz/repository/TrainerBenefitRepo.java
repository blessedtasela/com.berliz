package com.berliz.repository;

import com.berliz.models.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainerBenefitRepo extends JpaRepository<TrainerBenefit, Integer> {

    TrainerBenefit findByBenefit(String benefit);

    List<TrainerBenefit> findByTrainer(Trainer trainer);
}

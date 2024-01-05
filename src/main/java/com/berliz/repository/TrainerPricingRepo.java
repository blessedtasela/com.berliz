package com.berliz.repository;

import com.berliz.models.Trainer;
import com.berliz.models.TrainerLike;
import com.berliz.models.TrainerPricing;
import com.berliz.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainerPricingRepo extends JpaRepository<TrainerPricing, Integer> {

    TrainerPricing findByTrainer(Trainer trainer);

}

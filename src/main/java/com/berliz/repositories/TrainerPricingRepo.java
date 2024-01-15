package com.berliz.repositories;

import com.berliz.models.Trainer;
import com.berliz.models.TrainerPricing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerPricingRepo extends JpaRepository<TrainerPricing, Integer> {

    TrainerPricing findByTrainer(Trainer trainer);

}

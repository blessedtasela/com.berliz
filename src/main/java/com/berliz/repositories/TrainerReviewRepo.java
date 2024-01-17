package com.berliz.repositories;

import com.berliz.models.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainerReviewRepo extends JpaRepository<TrainerReview, Integer> {

    TrainerReview findByReview(String review);

    List<TrainerReview> findByTrainer(Trainer trainer);

    List<TrainerReview> findByClient(Client client);

    List<TrainerReview> getActiveTrainerReviewsByTrainer(Trainer trainer);

}
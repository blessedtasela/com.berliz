package com.berliz.repository;

import com.berliz.models.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainerClientReviewRepo extends JpaRepository<TrainerClientReview, Integer> {

    TrainerIntroduction findByReview(String review);

    List<TrainerClientReview> findByTrainer(Trainer trainer);

    List<TrainerClientReview> findByClient(Client client);

    List<TrainerClientReview> getActiveTrainerClientReviews();

}
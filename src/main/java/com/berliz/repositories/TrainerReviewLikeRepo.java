package com.berliz.repositories;

import com.berliz.models.TrainerReview;
import com.berliz.models.TrainerReviewLike;
import com.berliz.models.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainerReviewLikeRepo extends JpaRepository<TrainerReviewLike, Integer> {

    TrainerReviewLike findByTrainerReviewId(Integer id);

    List<TrainerReviewLike> getByUser(User user);

    @Transactional
    void deleteByUserAndTrainerReview(User user, TrainerReview trainerReview);

    boolean existsByUserAndTrainerReview(User user, TrainerReview trainerReview);
}


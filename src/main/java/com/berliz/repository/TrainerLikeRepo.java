package com.berliz.repository;

import com.berliz.models.Trainer;
import com.berliz.models.TrainerLike;
import com.berliz.models.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainerLikeRepo extends JpaRepository<TrainerLike, Integer> {

    TrainerLike findByTrainerId(Integer id);

    List<TrainerLike> getByUser(User user);

    @Transactional
    void deleteByUserAndTrainer(User user, Trainer trainer);

    boolean existsByUserAndTrainer(User user, Trainer trainer);
}

package com.berliz.repository;

import com.berliz.models.Trainer;
import com.berliz.models.TrainerLike;
import com.berliz.models.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainerLikeRepo extends JpaRepository<TrainerLike, Integer> {

    /**
     * Find a TrainerLike entity by the ID of the associated trainer.
     *
     * @param id The ID of the trainer.
     * @return The TrainerLike entity associated with the trainer ID.
     */
    TrainerLike findByTrainerId(Integer id);

    /**
     * Get a list of trainers liked by a specific user.
     *
     * @param user The user for which to retrieve liked trainers.
     * @return List of trainers liked by the user.
     */
    List<Trainer> getByUser(User user);

    /**
     * Delete trainer likes associated with a specific user and trainer.
     *
     * @param user    The user for which to delete likes.
     * @param trainer The trainer for which to delete likes.
     */
    @Transactional
    void deleteByUserAndTrainer(User user, Trainer trainer);

    /**
     * Check if a specific user has liked a specific trainer.
     *
     * @param user    The user for which to check the like.
     * @param trainer The trainer for which to check the like.
     * @return True if the user has liked the trainer; otherwise, false.
     */
    boolean existsByUserAndTrainer(User user, Trainer trainer);
}

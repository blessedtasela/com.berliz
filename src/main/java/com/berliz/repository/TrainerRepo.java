package com.berliz.repository;

import com.berliz.models.Trainer;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Repository interface for Trainer entities.
 */
public interface TrainerRepo extends JpaRepository<Trainer, Integer> {

    /**
     * Find a trainer by name.
     *
     * @param name The name of the trainer
     * @return The found trainer or null if not found
     */
    Trainer findByName(@Param("name") String name);

    /**
     * Find trainers by status.
     *
     * @param status The status of trainers to find
     * @return The list of trainers with the specified status
     */
    List<Trainer> findByStatus(@Param("status") String status);

    /**
     * Update the status of a trainer.
     *
     * @param id     The ID of the trainer to update
     * @param status The new status to set
     * @return The number of affected rows
     */
    @Transactional
    @Modifying
    Integer updateStatus(@PathVariable("id") Integer id, @PathVariable("status") String status);

    /**
     * Update the partner ID of a trainer.
     *
     * @param id        The ID of the trainer to update
     * @param trainerId The new trainer ID to associate
     * @return The number of affected rows
     */
    @Transactional
    @Modifying
    Integer updatePartnerId(@PathVariable("id") Integer id, @PathVariable("trainerId") Integer trainerId);

    /**
     * Find a trainer by trainer ID.
     *
     * @param id The trainer ID
     * @return The found trainer or null if not found
     */
    Trainer findByTrainerId(Integer id);

    /**
     * Find a trainer by partner ID.
     *
     * @param id The partner ID
     * @return The found trainer or null if not found
     */
    Trainer findByPartnerId(Integer id);

}

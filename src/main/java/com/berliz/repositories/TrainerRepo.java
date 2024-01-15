package com.berliz.repositories;

import com.berliz.models.Trainer;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Repository interface for Trainer entities.
 */
public interface TrainerRepo extends JpaRepository<Trainer, Integer> {

    Trainer findByName(@Param("name") String name);

    Trainer findByUserId(Integer id);

    @Query()
    Integer countTrainersByUserId(Integer id);

    List<Trainer> findByStatus(@Param("status") String status);

    @Transactional
    @Modifying
    Integer updateStatus(@PathVariable("id") Integer id, @PathVariable("status") String status);

    @Transactional
    @Modifying
    Integer updatePartnerId(@PathVariable("id") Integer id, @PathVariable("trainerId") Integer trainerId);

    Trainer findByTrainerId(Integer id);

    Trainer findByPartnerId(Integer id);

    List<Trainer>getActiveTrainers();
}

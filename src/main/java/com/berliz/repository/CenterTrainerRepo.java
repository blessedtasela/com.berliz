package com.berliz.repository;

import com.berliz.models.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CenterTrainerRepo extends JpaRepository<CenterTrainer, Integer> {

    List<CenterTrainer> findByTrainer(Trainer trainer);

    List<CenterTrainer> findByCenter(Center center);
}

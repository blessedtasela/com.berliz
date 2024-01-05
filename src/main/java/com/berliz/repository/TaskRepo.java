
package com.berliz.repository;

import com.berliz.models.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepo extends JpaRepository<Task, Integer> {

    Task findByDescription(String description);

    List<Task> findByUser(User user);

    List<Task> findByTrainer(Trainer trainer);

    Task findActiveTaskByUser(User user);

    List<Task> findBySubTasks(SubTask subTask);

    List<Task> getActiveTasks();

    Integer countClientTasksByEmail(String email);

    Integer countTrainerTasksByEmail(String email);
}
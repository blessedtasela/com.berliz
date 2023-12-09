
package com.berliz.repository;

import com.berliz.models.SubTask;
import com.berliz.models.Task;
import com.berliz.models.Trainer;
import com.berliz.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepo extends JpaRepository<Task, Integer> {

    /**
     * Find a task by description.
     *
     * @param description The description of the task to search for.
     * @return The found task or null if not found.
     */
    Task findByDescription(String description);

    /**
     * Find tasks by user.
     *
     * @param user The user associated with the tasks to search for.
     * @return List of tasks matching the user.
     */
    List<Task> findByUser(User user);

    /**
     * Find tasks by user.
     *
     * @param trainer The trainer associated with the tasks to search for.
     * @return List of tasks matching the trainer.
     */
    List<Task> findByTrainer(Trainer trainer);

    /**
     * Find task by user.
     *
     * @param user The user associated with the task to search for.
     * @return task matching the user.
     */
    Task findActiveTaskByUser(User user);

    /**
     * Find tasks by subtask.
     *
     * @param subTask The subtask associated with the tasks to search for.
     * @return List of tasks matching the subtask.
     */
    List<Task> findBySubTasks(SubTask subTask);

    /**
     * Get a list of all active tasks.
     *
     * @return List of tasks whose status is true.
     */
    List<Task> getActiveTasks();

    Integer countClientTasksByEmail(String email);

    Integer countTrainerTasksByEmail(String email);
}

package com.berliz.repositories;

import com.berliz.models.Exercise;
import com.berliz.models.SubTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubTaskRepo extends JpaRepository<SubTask, Integer> {

    /**
     * Find a subtask by name.
     *
     * @param name The name of the subtask to search for.
     * @return The found subtask or null if not found.
     */
    SubTask findByName(String name);


    /**
     * Find subtasks by exercise.
     *
     * @param exercise The exercise associated with the subtasks to search for.
     * @return List of subtasks matching the exercise.
     */
    List<SubTask> findByExercise(Exercise exercise);

}

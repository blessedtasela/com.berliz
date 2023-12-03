
package com.berliz.repository;

import com.berliz.models.Category;
import com.berliz.models.Exercise;
import com.berliz.models.MuscleGroup;
import com.berliz.models.SubTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseRepo extends JpaRepository<Exercise, Integer> {

    /**
     * Find an exercise by name.
     *
     * @param name The name of the exercise to search for.
     * @return The found exercise or null if not found.
     */
    Exercise findByName(String name);

    /**
     * Find exercises by category.
     *
     * @param category The category of the exercises to search for.
     * @return List of exercises matching the category.
     */
    List<Exercise> findByCategories(Category category);

    /**
     * Find exercises by muscle group.
     *
     * @param muscleGroup The muscle group of the exercises to search for.
     * @return List of exercises matching the muscle group.
     */
    List<Exercise> findByMuscleGroups(MuscleGroup muscleGroup);

    /**
     * Get a list of all active exercises.
     *
     * @return List of exercises whose status is true.
     */
    List<Exercise> getActiveExercises();
}

package com.berliz.repository;

import com.berliz.models.Exercise;
import com.berliz.models.MuscleGroup;
import com.berliz.models.Newsletter;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface MuscleGroupRepo extends JpaRepository<MuscleGroup, Integer> {

    /**
     * Find a muscle group by name.
     *
     * @param name The name of the muscle group to search for.
     * @return The found muscle group or null if not found.
     */
    MuscleGroup findByName(String name);

    /**
     * Find a muscle group by it's exercise.
     *
     * @param exercise The exercise linked to the muscle group to search for.
     * @return The found muscle group or null if not found.
     */
    MuscleGroup findByExercises(Exercise exercise);

    /**
     * Get a list of all active muscle groups.
     *
     * @return List of muscle groups whose status is true.
     */
    List<MuscleGroup> getActiveMuscleGroups();
}

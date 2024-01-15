package com.berliz.repositories;

import com.berliz.models.MuscleGroup;
import org.springframework.data.jpa.repository.JpaRepository;

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
     * Get a list of all active muscle groups.
     *
     * @return List of muscle groups whose status is true.
     */
    List<MuscleGroup> getActiveMuscleGroups();
}

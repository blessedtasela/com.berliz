package com.berliz.repository;

import com.berliz.models.*;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CenterLikeRepo extends JpaRepository<CenterLike, Integer> {

    /**
     * Find a CenterLike entity by the ID of the associated center.
     *
     * @param id The ID of the center.
     * @return The CenterLike entity associated with the center ID.
     */
    CenterLike findByCenterId(Integer id);

    /**
     * Get a list of centers liked by a specific user.
     *
     * @param user The user for which to retrieve liked centers.
     * @return List of centers liked by the user.
     */
    List<Center> getByUser(User user);

    /**
     * Delete center likes associated with a specific user and center.
     *
     * @param user   The user for which to delete likes.
     * @param center The center for which to delete likes.
     */
    @Transactional
    void deleteByUserAndCenter(User user, Center center);

    /**
     * Check if a specific user has liked a specific center.
     *
     * @param user   The user for which to check the like.
     * @param center The center for which to check the like.
     * @return True if the user has liked the center; otherwise, false.
     */
    boolean existsByUserAndCenter(User user, Center center);
}

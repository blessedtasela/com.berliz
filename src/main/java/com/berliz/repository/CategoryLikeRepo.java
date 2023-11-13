package com.berliz.repository;

import com.berliz.models.*;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryLikeRepo extends JpaRepository<CategoryLike, Integer> {

    /**
     * Find a CategoryLike entity by the ID of the associated center.
     *
     * @param id The ID of the category.
     * @return The CenterLike entity associated with the center ID.
     */
    CategoryLike findByCategoryId(Integer id);

    /**
     * Get a list of categories liked by a specific user.
     *
     * @param user The user for which to retrieve liked categories.
     * @return List of categories liked by the user.
     */
    List<Category> getByUser(User user);

    /**
     * Delete center likes associated with a specific user and center.
     *
     * @param user   The user for which to delete likes.
     * @param category The category for which to delete likes.
     */
    @Transactional
    void deleteByUserAndCategory(User user, Category category);

    /**
     * Check if a specific user has liked a specific center.
     *
     * @param user   The user for which to check the like.
     * @param category The category for which to check the like.
     * @return True if the user has liked the center; otherwise, false.
     */
    boolean existsByUserAndCategory(User user, Category category);
}

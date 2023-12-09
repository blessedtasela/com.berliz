package com.berliz.repository;

import com.berliz.models.Center;
import com.berliz.models.Trainer;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Repository interface for managing Center entities.
 */
public interface CenterRepo extends JpaRepository<Center, Integer> {

    /**
     * Find a center by its name.
     *
     * @param name The name of the center to find.
     * @return The center with the specified name.
     */
    Center findByName(@Param("name") String name);

    /**
     * Find a center by user id.
     *
     * @param id The user id of the partner of the center
     * @return The found center or null if not found
     */
    Center findByUserId(Integer id);

    @Query()
    Integer countCentersUserById(Integer id);

    /**
     * Find centers by their status.
     *
     * @param status The status of the centers to find.
     * @return The list of centers with the specified status.
     */
    List<Center> findByStatus(@Param("status") String status);

    /**
     * Update the status of a center by its ID.
     *
     * @param id     The ID of the center to update.
     * @param status The new status to set for the center.
     * @return The number of affected rows.
     */
    @Transactional
    @Modifying
    Integer updateStatus(@PathVariable("id") Integer id, @PathVariable("status") String status);

    /**
     * Update the partner ID associated with a center by its ID.
     *
     * @param id    The ID of the center to update.
     * @param newId The new partner ID to associate with the center.
     * @return The number of affected rows.
     */
    @Transactional
    @Modifying
    Integer updatePartnerId(@PathVariable("id") Integer id, @PathVariable("newId") Integer newId);

    /**
     * Find a center by its center ID.
     *
     * @param id The ID of the center to find.
     * @return The center with the specified center ID.
     */
    Center findByCenterId(Integer id);

    /**
     * Find a center by the partner's ID.
     *
     * @param id The ID of the partner associated with the center.
     * @return The center with the specified partner ID.
     */
    Center findByPartnerId(Integer id);

    /**
     * Get centers by their category ID.
     *
     * @param id The ID of the category associated with the centers.
     * @return The list of centers with the specified category ID.
     */
    List<Center> getByCategoryId(@Param("id") Integer id);

    /**
     * Get the lists of centers whose status are true
     *
     * @return The list of center or null if not found
     */
    List<Center>getActiveCenters();

}

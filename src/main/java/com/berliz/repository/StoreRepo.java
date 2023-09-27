package com.berliz.repository;

import com.berliz.models.Store;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Repository interface for managing Store entities.
 */
public interface StoreRepo extends JpaRepository<Store, Integer> {

    /**
     * Find a store by its name.
     *
     * @param name The name of the store to find.
     * @return The store with the specified name.
     */
    Store findByName(@Param("name") String name);

    /**
     * Find stores by their status.
     *
     * @param status The status of the stores to find.
     * @return The list of stores with the specified status.
     */
    List<Store> findByStatus(@Param("status") String status);

    /**
     * Update the status of a store by its ID.
     *
     * @param id     The ID of the store to update.
     * @param status The new status to set for the store.
     * @return The number of affected rows.
     */
    @Transactional
    @Modifying
    Integer updateStatus(@PathVariable("id") Integer id, @PathVariable("status") String status);

    /**
     * Update the partner ID associated with a store by its ID.
     *
     * @param id        The ID of the store to update.
     * @param partnerId The new partner ID to associate with the store.
     * @return The number of affected rows.
     */
    @Transactional
    @Modifying
    Integer updatePartnerId(@PathVariable("id") Integer id, @PathVariable("partnerId") Integer partnerId);

    /**
     * Find a store by its store ID.
     *
     * @param id The ID of the store to find.
     * @return The store with the specified store ID.
     */
    Store findByStoreId(Integer id);

    /**
     * Find a store by its associated partner ID.
     *
     * @param id The ID of the partner associated with the store.
     * @return The store with the specified partner ID.
     */
    Store findByPartnerId(Integer id);

    /**
     * Get stores by their category ID.
     *
     * @param id The ID of the category associated with the stores.
     * @return The list of stores with the specified category ID.
     */
    List<Store> getByCategoryId(@Param("id") Integer id);
}

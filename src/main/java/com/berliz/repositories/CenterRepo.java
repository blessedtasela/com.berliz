package com.berliz.repositories;

import com.berliz.models.Center;
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

    Center findByName(@Param("name") String name);

    Center findByUserId(Integer id);

    @Query()
    Integer countCentersUserById(Integer id);

    List<Center> findByStatus(@Param("status") String status);

    @Transactional
    @Modifying
    Integer updateStatus(@PathVariable("id") Integer id, @PathVariable("status") String status);

    @Transactional
    @Modifying
    Integer updatePartnerId(@PathVariable("id") Integer id, @PathVariable("newId") Integer newId);

    Center findByCenterId(Integer id);

    Center findByPartnerId(Integer id);

    List<Center> getByCategoryId(@Param("id") Integer id);

    List<Center>getActiveCenters();

}

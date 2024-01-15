package com.berliz.repositories;

import com.berliz.models.Driver;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface DriverRepo extends JpaRepository<Driver, Integer> {

    Driver findByName(@Param("name") String name);

    List<Driver> findByStatus(@Param("status")String status);

    @Transactional
    @Modifying
    Integer updateStatus(@PathVariable("id") Integer id, @PathVariable("status") String status);

    @Transactional
    @Modifying
    Integer updatePartnerId(@PathVariable("id") Integer id, @PathVariable("newId") Integer newId);

    Driver findByDriverId(Integer id);

    Driver findByPartnerId(Integer id);
}

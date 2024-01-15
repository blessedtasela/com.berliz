package com.berliz.repositories;

import com.berliz.models.Brand;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface BrandRepo extends JpaRepository<Brand, Integer> {

    Brand findByName(@Param("name") String name);

    List<Brand>getAllBrands();

    @Transactional
    @Modifying
    Integer updateStatus(@PathVariable("id") Integer id, @PathVariable("status") String status);

    @Transactional
    @Modifying
    void updateBrand(@Param("name") String name, @Param("description") String description,
                    @Param("ratings") float ratings, @Param("id") Integer id);
}

package com.berliz.repository;

import com.berliz.models.Category;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface CategoryRepo  extends JpaRepository<Category, Integer> {

    Category findByName(@Param("name") String name);

    List<Category> getActiveCategories();

    @Transactional
    @Modifying
    Integer updateStatus(@PathVariable("id") Integer id, @PathVariable("status") String status);

    @Transactional
    @Modifying
    void updateCategory(@Param("name") String name, @Param("description") String description,
                      @Param("id") Integer id);

    List<Category> getByTag(@Param("id") Integer id);
}

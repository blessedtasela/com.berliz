package com.berliz.repositories;

import com.berliz.models.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface ProductRepo extends JpaRepository<Product, Integer> {

    Product findByName(@Param("name") String name);

    List<Product> findByStatus(String name);

    Product findByUuid(@Param("uuid") String name);


    @Transactional
    @Modifying
    Integer updateStatus(@PathVariable("id") Integer id, @PathVariable("status") String status);

    List<Product> findByStoreId(Integer id);

    List<Product> findByBrandId(Integer id);

    Product findByProductId(Integer id);
}

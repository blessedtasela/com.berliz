package com.berliz.repository;

import com.berliz.models.Order;
import com.berliz.models.OrderDetails;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface OrderRepo extends JpaRepository<Order, Integer> {

    Order findByUuid(@Param("uuid") String uuid);

    Order findByOrderId(Integer id);


    @Transactional
    @Modifying
    Integer updateStatus(@PathVariable("id") Integer id, @PathVariable("status") String status);

    List<Order> findByUserId(@Param("id") Integer id);

    List<Order> findByStatus(@Param("status") String status);

    @Query()
    Integer countOrdersByEmail(@Param("email") String email);

}

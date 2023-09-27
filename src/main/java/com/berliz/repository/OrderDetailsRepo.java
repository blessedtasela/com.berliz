package com.berliz.repository;

import com.berliz.models.Order;
import com.berliz.models.OrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface OrderDetailsRepo extends JpaRepository<OrderDetails, Integer> {

    OrderDetails findByOrderDetailsId(@Param("orderDetailsId") Object id);

}

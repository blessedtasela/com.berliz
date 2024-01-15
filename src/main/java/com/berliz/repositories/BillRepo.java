package com.berliz.repositories;

import com.berliz.models.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BillRepo extends JpaRepository<Bill, Integer> {

    Bill findByUuid(@Param("name") String uuid);

    Bill findByBillId(@Param("id") Integer id);

    Bill findByOrderId(@Param("id") Integer id);

    List<Bill> findByUserId(@Param("id") String creator);

}

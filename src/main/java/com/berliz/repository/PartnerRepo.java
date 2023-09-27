package com.berliz.repository;

import com.berliz.models.Partner;
import com.berliz.models.Store;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface PartnerRepo extends JpaRepository<Partner, Integer> {

    List<Partner> findByStatus(@Param("status")String status);

    @Transactional
    @Modifying
    Integer updateStatus(@PathVariable("id") Integer id, @PathVariable("status") String status);

    @Transactional
    @Modifying
    Integer updateUserId(@PathVariable("id") Integer id, @PathVariable("userId") Integer userId);

    Partner findByPartnerId(Integer id);

    Partner findByUserId(Integer id);

    List<Partner> findByRole(@Param("role") String role);

    Integer countPartnerByEmail(@Param("email") String email);
}

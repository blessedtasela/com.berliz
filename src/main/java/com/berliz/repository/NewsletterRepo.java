package com.berliz.repository;

import com.berliz.models.Newsletter;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface NewsletterRepo extends JpaRepository<Newsletter, Integer> {

    Newsletter findByEmail(@Param("email") String email);

    List<Newsletter> getAllNewsletters();

    @Transactional
    @Modifying
    Integer updateStatus(@PathVariable("id") Integer id, @PathVariable("status") String status);

    @Transactional
    @Modifying
    void updateNewsletter(@Param("email") String name, @Param("id") Integer id);
}

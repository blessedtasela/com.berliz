package com.berliz.repositories;

import com.berliz.models.ContactUs;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.web.bind.annotation.PathVariable;

public interface ContactUsRepo extends JpaRepository<ContactUs, Integer> {

    ContactUs findByEmail(String email);

    ContactUs findByName(String name);

    @Transactional
    @Modifying
    Integer updateStatus(@PathVariable("id") Integer id, @PathVariable("status") String status);
}


package com.berliz.repositories;

import com.berliz.models.ContactUsMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactUsMessageRepo extends JpaRepository<ContactUsMessage, Integer> {

    ContactUsMessage findByMessage(String message);
}

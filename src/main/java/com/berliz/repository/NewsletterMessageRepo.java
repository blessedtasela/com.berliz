package com.berliz.repository;

import com.berliz.models.ContactUsMessage;
import com.berliz.models.NewsletterMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterMessageRepo extends JpaRepository<NewsletterMessage, Integer> {

    NewsletterMessage findByMessage(String message);
}
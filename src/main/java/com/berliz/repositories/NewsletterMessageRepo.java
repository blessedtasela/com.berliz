package com.berliz.repositories;

import com.berliz.models.NewsletterMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterMessageRepo extends JpaRepository<NewsletterMessage, Integer> {

    NewsletterMessage findByMessage(String message);
}
package com.berliz.repositories;

import com.berliz.models.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientReviewRepo extends JpaRepository<ClientReview, Integer> {

    ClientReview findByReview(String review);

    List<ClientReview> findByTrainer(Trainer trainer);

    List<ClientReview> findByClient(Client client);

    List<ClientReview> getActiveClientReviews(Trainer trainer);

}
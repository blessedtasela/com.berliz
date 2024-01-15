package com.berliz.repositories;

import com.berliz.models.ClientReview;
import com.berliz.models.ClientReviewLike;
import com.berliz.models.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientReviewLikeRepo extends JpaRepository<ClientReviewLike, Integer> {

    ClientReviewLike findByClientReviewId(Integer id);

    List<ClientReviewLike> getByUser(User user);

    @Transactional
    void deleteByUserAndClientReview(User user, ClientReview clientReview);

    boolean existsByUserAndClientReview(User user, ClientReview clientReview);
}


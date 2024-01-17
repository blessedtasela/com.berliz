package com.berliz.repositories;

import com.berliz.models.*;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CenterReviewLikeRepo extends JpaRepository<CenterReviewLike, Integer> {

    CenterReviewLike findByCenterReviewId(Integer id);

    List<CenterReviewLike> getByUser(User user);

    @Transactional
    void deleteByUserAndCenterReview(User user, CenterReview centerReview);

    boolean existsByUserAndCenterReview(User user, CenterReview centerReview);
}

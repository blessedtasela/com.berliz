package com.berliz.repositories;

import com.berliz.models.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CenterReviewRepo extends JpaRepository<CenterReview, Integer> {

    CenterReview findByComment(String comment);

    List<CenterReview> findByCenter(Center center);

    List<CenterReview> findByMember(Member member);

    List<CenterReview> getActiveCenterReviewsByCenter(Center center);

}
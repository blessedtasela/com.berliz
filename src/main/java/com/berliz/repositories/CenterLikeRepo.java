package com.berliz.repositories;

import com.berliz.models.*;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CenterLikeRepo extends JpaRepository<CenterLike, Integer> {

    CenterLike findByCenterId(Integer id);

    List<Center> getByUser(User user);

    @Transactional
    void deleteByUserAndCenter(User user, Center center);

    boolean existsByUserAndCenter(User user, Center center);
}

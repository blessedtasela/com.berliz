
package com.berliz.repositories;

import com.berliz.models.*;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubscriptionRepo extends JpaRepository<Subscription, Integer> {

    List<Subscription> findByTrainer(Trainer trainer);

    List<Subscription> findByUser(User user);

    Subscription findActiveSubscriptionByUser(User user);

    List<Subscription> findByCenter(Center center);

    List<Client> findByCategories(Category category);

    List<Subscription> getActiveSubscriptions();

    Integer countClientSubscriptionsByEmail(String email);

    Integer countMemberSubscriptionsByEmail(String email);

    Boolean existsByUser(User user);

    @Transactional
    @Modifying
    int bulkUpdateStatusByIds(@Param("ids") List<Integer> ids, @Param("status") String status);

    @Transactional
    @Modifying
    int bulkDeleteByIds(@Param("ids") List<Integer> ids);
}
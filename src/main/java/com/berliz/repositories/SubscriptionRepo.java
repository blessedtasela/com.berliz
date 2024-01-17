
package com.berliz.repositories;

import com.berliz.models.*;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
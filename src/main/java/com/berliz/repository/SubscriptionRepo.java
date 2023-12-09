
package com.berliz.repository;

import com.berliz.models.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionRepo extends JpaRepository<Subscription, Integer> {


    /**
     * Find subscriptions by trainer.
     *
     * @param trainer The trainer associated with the subscriptions to search for.
     * @return List of subscriptions associated with the trainer.
     */
    List<Subscription> findByTrainer(Trainer trainer);

    /**
     * Find subscriptions by user.
     *
     * @param user The user associated with the subscriptions to search for.
     * @return List of subscriptions associated with the user.
     */
    List<Subscription> findByUser(User user);

    /**
     * Find subscription by user.
     *
     * @param user The user associated with the subscription to search for.
     * @return subscription associated with the user.
     */
    Subscription findActiveSubscriptionByUser(User user);

    /**
     * Find subscriptions by center.
     *
     * @param center The center associated with the subscriptions to search for.
     * @return List of subscriptions associated with the center.
     */
    List<Subscription> findByCenter(Center center);

    /**
     * Find subscriptions by payment.
     *
     * @param payment The payment associated with the subscriptions to search for.
     * @return List of subscriptions associated with the payment.
     */
    List<Subscription> findByPayment(Payment payment);

    /**
     * Get a list of all active subscriptions.
     *
     * @return List of subscriptions whose status is true.
     */
    List<Subscription> getActiveSubscriptions();

    Integer countClientSubscriptionsByEmail(String email);

    Integer countMemberSubscriptionsByEmail(String email);
}

package com.berliz.repositories;

import com.berliz.models.Payment;
import com.berliz.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepo extends JpaRepository<Payment, Integer> {

    /**
     * Find payments by payer (user).
     *
     * @param user The user who made the payments to search for.
     * @return List of payments made by the user.
     */
    List<Payment> findByPayer(User user);

    /**
     * Find payments by user.
     *
     * @param user The user associated with the payments to search for.
     * @return List of payments associated with the user.
     */
    List<Payment> findByUser(User user);

    /**
     * Find payment by user.
     *
     * @param user The user associated with the payments to search for.
     * @return payment associated with the user.
     */
    Payment findActivePaymentByUser(User user);

    /**
     * Find payments by amount.
     *
     * @param amount The amount of the payments to search for.
     * @return List of payments with the specified amount.
     */
    List<Payment> findByAmount(float amount);

    /**
     * Get a list of all active payments.
     *
     * @return List of payments whose status is true.
     */
    List<Payment> getActivePayments();
}

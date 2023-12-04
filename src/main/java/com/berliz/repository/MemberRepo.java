
package com.berliz.repository;

import com.berliz.models.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepo extends JpaRepository<Member, Integer> {

    /**
     * Find members by category.
     *
     * @param category The category of the members to search for.
     * @return List of members matching the category.
     */
    List<Member> findByCategories(Category category);

    /**
     * Find members by center.
     *
     * @param center The center of the members to search for.
     * @return List of members matching the center.
     */
    List<Member> findByCenter(Center center);

    /**
     * Find a member by user.
     *
     * @param user The user associated with the member to search for.
     * @return member associated with the user.
     */
    Member findByUser(User user);

    /**
     * Find members by trainer.
     *
     * @param trainer The trainer associated with the members to search for.
     * @return List of members associated with the trainer.
     */
    List<Member> findByTrainer(Trainer trainer);

    /**
     * Get a list of all active members.
     *
     * @return List of members whose status is true.
     */
    List<Member> getActiveMembers();
}


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
     * Find a member by user.
     *
     * @param user The user associated with the member to search for.
     * @return member associated with the user.
     */
    Member findByUser(User user);

    /**
     * Get a list of all active members.
     *
     * @return List of members whose status is true.
     */
    List<Member> getActiveMembers();

    Integer countCenterMembersByEmail(String email);
}

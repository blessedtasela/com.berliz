package com.berliz.repositories;

import com.berliz.models.Newsletter;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface NewsletterRepo extends JpaRepository<Newsletter, Integer> {

    /**
     * Find a newsletter by email.
     *
     * @param email The email to search for.
     * @return The found newsletter or null if not found.
     */
    Newsletter findByEmail(@Param("email") String email);

    /**
     * Get a list of all newsletters.
     *
     * @return List of newsletters.
     */
    List<Newsletter> getAllNewsletters();

    /**
     * Get a list of all newsletters.
     *
     * @return List of newsletters whose status are true.
     */
    List<Newsletter> getActiveNewsletters();

    /**
     * Get a list of all active email addresses.
     *
     * @return List of active email addresses.
     */
    List<String> getAllActiveEMails();

    /**
     * Update the status of a newsletter by ID.
     *
     * @param id     The ID of the newsletter to update.
     * @param status The status to set.
     * @return The number of records updated.
     */
    @Transactional
    @Modifying
    Integer updateStatus(@PathVariable("id") Integer id, @PathVariable("status") String status);

    /**
     * Update a newsletter by email and ID.
     *
     * @param email The email of the newsletter to update.
     * @param id    The ID of the newsletter to update.
     */
    @Transactional
    @Modifying
    void updateNewsletter(@Param("email") String email, @Param("id") Integer id);
}

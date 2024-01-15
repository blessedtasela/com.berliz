
package com.berliz.repositories;

import com.berliz.models.Center;
import com.berliz.models.Testimonial;
import com.berliz.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository interface for managing testimonial-related operations.
 */
public interface TestimonialRepo extends JpaRepository<Testimonial, Integer> {

    /**
     * Find a testimonial by name.
     *
     * @param testimonial The testimonial to search for.
     * @return The found testimonial or null if not found.
     */
    Testimonial findByTestimonial(String testimonial);

    /**
     * Find testimonials by center.
     *
     * @param center The center associated with the testimonials to search for.
     * @return List of testimonials associated with the center.
     */
    List<Testimonial> findByCenter(Center center);

    /**
     * Find testimonial by user.
     *
     * @param user The user associated with the testimonial to search for.
     * @return testimonial associated with the user.
     */
    Testimonial findByUser(User user);

    /**
     * Get a list of all active testimonials.
     *
     * @return List of testimonials whose status is true.
     */
    List<Testimonial> getActiveTestimonials();

    Integer countUserTestimonialsByEmail(String email);

    Integer countCenterTestimonialsByEmail(String email);
}
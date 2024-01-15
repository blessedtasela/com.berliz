package com.berliz.repositories;

import com.berliz.models.Tag;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface TagRepo extends JpaRepository<Tag, Integer> {

    Tag findByName(@Param("name") String name);

    List<Tag> getAllTags();

    @Transactional
    @Modifying
    Integer updateStatus(@PathVariable("id") Integer id, @PathVariable("status") String status);

    /**
     * Get the lists of tags whose status are true
     *
     * @return The list of tags or null if not found
     */
    List<Tag>getActiveTags();
}

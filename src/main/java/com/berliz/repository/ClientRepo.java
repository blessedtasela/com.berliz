
package com.berliz.repository;

import com.berliz.models.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientRepo extends JpaRepository<Client, Integer> {

    /**
     * Find a client by name.
     *
     * @param user The user of the client to search for.
     * @return The found client or null if not found.
     */
    Client findByUser(User user);

    /**
     * Find clients by category.
     *
     * @param category The category of the clients to search for.
     * @return List of clients matching the category.
     */
    List<Client> findByCategories(Category category);

    /**
     * Get a list of all active clients.
     *
     * @return List of clients whose status is true.
     */
    List<Client> getActiveClients();
}
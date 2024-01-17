
package com.berliz.repositories;

import com.berliz.models.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientRepo extends JpaRepository<Client, Integer> {

    Client findByUser(User user);

    Client findByUserId(Integer id);

    List<Client> getActiveClients();

    List<Client> getMyClientsByTrainer(Trainer trainer);

    List<Client>getMyActiveClientsByTrainer(Trainer trainer);

    Integer countTrainerClientsByEmail(String email);
}
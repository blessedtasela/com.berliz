package com.berliz.repository;

import com.berliz.models.User;
import com.berliz.wrapper.UserWrapper;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Integer> {

    User findByToken(String token);

    User findByUserId(Integer id);

    List<String> getAllAdminsMail();

    List<User> getActiveUsers();

    List<User> findAllAdmins();

    @Transactional
    @Modifying
    Integer updateStatus(@PathVariable("id") Integer id, @PathVariable("status") String status);


    @Transactional
    @Modifying
    Integer updateUserRole(@Param("role") String role, @Param("id") Integer id);

    User findByEmail(String email);

}

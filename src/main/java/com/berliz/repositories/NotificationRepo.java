package com.berliz.repositories;

import com.berliz.models.Notification;
import com.berliz.models.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepo extends JpaRepository<Notification, Integer> {

    List<Notification> findByUser(User user);

    Notification findByNotification(String notification);

    Boolean existsByUser(User user);

    @Transactional
    @Modifying
    int bulkDeleteByIds(@Param("ids") List<Integer> ids);

    @Transactional
    @Modifying
    int bulkReadByIds(@Param("ids") List<Integer> ids);

    @Transactional
    @Modifying
    int bulkUnreadByIds(@Param("ids") List<Integer> ids);
}

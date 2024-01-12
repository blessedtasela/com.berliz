package com.berliz.repository;

import com.berliz.models.Center;
import com.berliz.models.CenterAnnouncement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CenterAnnouncementRepo extends JpaRepository<CenterAnnouncement, Integer> {

    CenterAnnouncement findByAnnouncement(String announcement);

    List<CenterAnnouncement> findByCenter(Center center);
}

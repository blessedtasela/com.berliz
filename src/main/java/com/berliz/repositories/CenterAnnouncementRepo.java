package com.berliz.repositories;

import com.berliz.models.Center;
import com.berliz.models.CenterAnnouncement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CenterAnnouncementRepo extends JpaRepository<CenterAnnouncement, Integer> {

    CenterAnnouncement findByAnnouncement(String announcement);

    List<CenterAnnouncement> findByCenter(Center center);

    List<CenterAnnouncement> getActiveCenterAnnouncements(Center center);
}

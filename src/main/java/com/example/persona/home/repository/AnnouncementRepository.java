package com.example.persona.home.repository;

import com.example.persona.home.model.Announcement;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByEffectiveFromBeforeAndEffectiveToAfter(
            LocalDateTime effectiveFrom, LocalDateTime effectiveTo);
}

package com.example.persona.schedule.repository;

import com.example.persona.schedule.model.UserScheduleHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserScheduleHistoryRepository extends JpaRepository<UserScheduleHistory, Long> {
    Page<UserScheduleHistory> findByUserScheduleIdAndCustomerKey(
            Long scheduleId, String customerKey, Pageable pageable);
}

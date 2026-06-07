package com.example.persona.schedule.repository;

import com.example.persona.enums.StatusType;
import com.example.persona.schedule.enums.ScheduleStatus;
import com.example.persona.schedule.model.UserSchedule;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserScheduleRepository extends JpaRepository<UserSchedule, Long> {
    Integer countByCustomerKeyAndStatus(String customerKey, StatusType status);

    long countByCustomerKeyAndServiceTypeAndStatus(String customerKey, String serviceType, StatusType status);

    Optional<UserSchedule> findByIdAndCustomerKeyAndStatus(Long id, String customerKey, StatusType status);

    Page<UserSchedule> findByCustomerKeyAndStatus(String customerKey, StatusType status, Pageable pageable);

    Page<UserSchedule> findByCustomerKeyAndServiceTypeAndStatus(
            String customerKey, String serviceType, StatusType status, Pageable pageable);

    Optional<UserSchedule> findByIdAndCustomerKey(Long scheduleId, String customerKey);

    Optional<UserSchedule> findByCustomerKey(String customerKey);

    Page<UserSchedule> findByCustomerKeyAndScheduleStatus(String customerKey, ScheduleStatus status, Pageable pageable);

    List<UserSchedule> findByCustomerKeyAndStatusOrderByDisplayOrder(String customerKey, StatusType statusType);

    Optional<UserSchedule> findByCustomerKeyAndServiceTypeAndServiceCode(
            String customerKey, String serviceType, String serviceCode);
}

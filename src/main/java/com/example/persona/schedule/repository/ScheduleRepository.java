package com.example.persona.schedule.repository;

import com.example.persona.enums.StatusType;
import com.example.persona.schedule.model.Schedule;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByServiceType(String serviceType);

    List<Schedule> findByServiceCode(String serviceCode);

    List<Schedule> findByScheduleType(String scheduleType);

    Optional<Schedule> findByServiceTypeAndServiceCode(String serviceType, String serviceCode);

    boolean existsByServiceTypeAndServiceCode(String serviceType, String serviceCode);

    List<Schedule> findByServiceTypeAndStatus(String serviceType, StatusType statusType);
}

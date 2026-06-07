package com.example.persona.session.repository;

import com.example.persona.session.model.UserSession;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findByCustomerKey(String customerKey);

    Optional<UserSession> findByCustomerKeyAndDeviceId(String customerKey, String deviceId);
}

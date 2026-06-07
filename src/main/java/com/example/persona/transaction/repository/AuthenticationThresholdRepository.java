package com.example.persona.transaction.repository;

import com.example.persona.transaction.model.AuthenticationMethod;
import com.example.persona.transaction.model.AuthenticationThreshold;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthenticationThresholdRepository extends JpaRepository<AuthenticationThreshold, Long> {
    Optional<AuthenticationThreshold> findByAuthenticationMethod(AuthenticationMethod authenticationMethod);
}

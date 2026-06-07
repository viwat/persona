package com.example.persona.theme.repository;

import com.example.persona.theme.model.UserPersona;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPersonaRepository extends JpaRepository<UserPersona, Long> {
    Optional<UserPersona> findByCustomerNo(String customerNo);

    Optional<UserPersona> findByCustomerKey(String customerKey);
}

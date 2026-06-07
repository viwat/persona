package com.example.persona.pinless.repository;

import com.example.persona.pinless.model.entity.PinlessConfig;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PinlessConfigRepository extends JpaRepository<PinlessConfig, Long> {

    Optional<PinlessConfig> findByCustomerNo(String customerNo);

    boolean existsByCustomerNo(String customerNo);
}

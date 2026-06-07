package com.example.persona.profile.repository;

import com.example.persona.profile.model.CustomerProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {
    Optional<CustomerProfile> findByCustomerNo(String customerNo);
}

package com.example.persona.profile.repository;

import com.example.persona.enums.StatusType;
import com.example.persona.profile.model.CustomerAppProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerAppProfileRepository extends JpaRepository<CustomerAppProfile, Long> {

    Optional<CustomerAppProfile> findByVersionKeyAndStatus(String versionKey, StatusType status);

    Optional<CustomerAppProfile> findByCustomerNoAndStatus(String customerNo, StatusType status);

    Optional<CustomerAppProfile> findByCustomerKeyAndStatus(String customerKey, StatusType status);

    Optional<CustomerAppProfile> findFirstByCustomerNoAndStatusOrderByCreatedDateDescIdDesc(
            String customerNo, StatusType status);

    Optional<CustomerAppProfile> findFirstByAccountNoAndStatusOrderByCreatedDateDescIdDesc(
            String accountNo, StatusType status);

    List<CustomerAppProfile> findByVersionKeyOrderByVersionDesc(String versionKey);
}

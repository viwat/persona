package com.example.persona.profile.repository;

import com.example.persona.enums.StatusType;
import com.example.persona.profile.model.CardAppProfile;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardAppProfileRepository extends JpaRepository<CardAppProfile, Long> {

    Optional<CardAppProfile> findByVersionKeyAndStatus(String versionKey, StatusType status);

    List<CardAppProfile> findByVersionKeyInAndStatus(Collection<String> versionKeys, StatusType status);

    List<CardAppProfile> findByCustomerNoAndStatusOrderByDisplayOrderAsc(String customerNo, StatusType status);

    List<CardAppProfile> findByCustomerNoAndIsHiddenFalseAndStatusOrderByDisplayOrderAsc(
            String customerNo, StatusType status);

    List<CardAppProfile> findByCustomerNoAndAccountNoAndStatusOrderByDisplayOrderAsc(
            String customerNo, String accountNo, StatusType status);
}

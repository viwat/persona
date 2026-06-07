package com.example.persona.profile.repository;

import com.example.persona.enums.StatusType;
import com.example.persona.profile.model.AccountAppProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountAppProfileRepository extends JpaRepository<AccountAppProfile, Long> {

    Optional<AccountAppProfile> findByVersionKeyAndStatus(String versionKey, StatusType status);

    Optional<AccountAppProfile> findByCustomerNoAndAccountNoAndStatus(
            String customerNo, String accountNo, StatusType status);

    Optional<AccountAppProfile> findByAccountHashAndStatus(String accountHash, StatusType status);

    List<AccountAppProfile> findByCustomerNoAndStatusOrderByDisplayOrderAsc(String customerNo, StatusType status);

    List<AccountAppProfile> findByCustomerNoAndIsHiddenFalseAndStatusOrderByDisplayOrderAsc(
            String customerNo, StatusType status);
}

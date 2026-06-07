package com.example.persona.profile.repository;

import com.example.persona.profile.model.AccountProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountProfileRepository extends JpaRepository<AccountProfile, Long> {

    @Query("""
			SELECT ap FROM AccountProfile ap
			WHERE (:accountNo = ap.accountNo OR ap.accountNo = 'ANY')
			AND (:serviceType = ap.serviceType OR ap.serviceType = 'ANY')
			AND (:entryType = ap.entryType OR ap.entryType = 'ANY')
			ORDER BY CASE
			    WHEN ap.accountNo = :accountNo AND ap.serviceType = :serviceType AND ap.entryType = :entryType THEN 1
			    WHEN ap.accountNo = :accountNo AND ap.serviceType = :serviceType AND ap.entryType = 'ANY' THEN 2
			    WHEN ap.accountNo = :accountNo AND ap.serviceType = 'ANY' AND ap.entryType = 'ANY' THEN 3
			    WHEN ap.accountNo = 'ANY' AND ap.serviceType = 'ANY' AND ap.entryType = 'ANY' THEN 4
			    ELSE 5
			END
			LIMIT 1
			""")
    Optional<AccountProfile> findMatchingConfig(
            @Param("accountNo") String accountNo,
            @Param("serviceType") String serviceType,
            @Param("entryType") String entryType);

    Optional<AccountProfile> findByCustomerNoAndAccountNo(String customerNo, String accountNo);

    List<AccountProfile> findByCustomerNoOrderByDisplayOrderAsc(String customerNo);

    List<AccountProfile> findByCustomerNoAndIsHiddenFalseOrderByDisplayOrderAsc(String customerNo);
}

package com.example.persona.migration.repository.oracle;

import com.example.persona.migration.model.oracle.DigiMasterAccountView;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DigiMasterAccountViewRepository extends JpaRepository<DigiMasterAccountView, String> {

    Optional<DigiMasterAccountView> findByMasterAccId(String masterAccId);

    Optional<DigiMasterAccountView> findByLoginIdAndApplicationId(String loginId, String applicationId);

    Optional<DigiMasterAccountView> findByPhoneNumber(String phoneNumber);

    List<DigiMasterAccountView> findByStatus(String status);

    @Query("SELECT d FROM DigiMasterAccountView d WHERE d.loginId = :loginId OR d.phoneNumber = :phoneNumber")
    List<DigiMasterAccountView> findByLoginIdOrPhoneNumber(
            @Param("loginId") String loginId, @Param("phoneNumber") String phoneNumber);
}

package com.example.persona.i18n.repository;

import com.example.persona.enums.StatusType;
import com.example.persona.i18n.model.Translation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TranslationRepository extends JpaRepository<Translation, Long> {
    List<Translation> findAllByOrderByCodeAsc();

    List<Translation> findAllByAppCodeAndStatus(String appCode, StatusType status);

    default List<Translation> findAllActiveByAppCode(String appCode) {
        return findAllByAppCodeAndStatus(appCode, StatusType.ACTIVE);
    }

    Translation findByCodeAndAppCode(String code, String appCode);

    Optional<Translation> findByCode(String code);
}

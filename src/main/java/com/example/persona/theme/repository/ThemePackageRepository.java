package com.example.persona.theme.repository;

import com.example.persona.enums.StatusType;
import com.example.persona.theme.model.ThemePackage;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThemePackageRepository extends JpaRepository<@NonNull ThemePackage, @NonNull Long> {
    Optional<ThemePackage> findByBackgroundType(String backgroundType);

    Optional<ThemePackage> findByIdAndStatus(Long id, StatusType status);

    List<ThemePackage> findByStatusAndIdIn(StatusType statusType, List<Long> ids);
}

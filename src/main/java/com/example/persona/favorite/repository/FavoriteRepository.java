package com.example.persona.favorite.repository;

import com.example.persona.favorite.model.Favorite;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteRepository extends JpaRepository<@NonNull Favorite, @NonNull Long> {
    List<Favorite> findByServiceTypeAndAppVersion(String serviceType, String appVersion);

    Optional<Favorite> findByServiceTypeAndServiceCode(String serviceType, String serviceCode);

    List<Favorite> findAll(Specification<@NonNull Favorite> spec, Sort and);
}

package com.example.persona.pinless.repository;

import com.example.persona.pinless.config.PinlessCacheConfig;
import com.example.persona.pinless.model.entity.PinlessAuthPolicy;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PinlessAuthPolicyRepository extends JpaRepository<PinlessAuthPolicy, Long> {

    Optional<PinlessAuthPolicy> findByTierAndActiveTrue(String tier);

    @Cacheable(
            cacheNames = PinlessCacheConfig.CACHE_AUTH_POLICY,
            cacheManager = PinlessCacheConfig.CACHE_MANAGER,
            key = "#tier")
    default PinlessAuthPolicy findPolicyForTier(String tier) {
        return findByTierAndActiveTrue(tier)
                .or(() -> findByTierAndActiveTrue("DEFAULT"))
                .orElseThrow(() -> new IllegalStateException(
                        "No active pinless auth policy found for tier=" + tier + " and no DEFAULT policy exists"));
    }
}

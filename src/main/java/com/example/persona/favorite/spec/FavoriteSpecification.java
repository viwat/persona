package com.example.persona.favorite.spec;

import com.example.persona.favorite.dto.FavoriteFilterRequest;
import com.example.persona.favorite.model.Favorite;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.domain.Specification;

public class FavoriteSpecification {

    public static Specification<@NonNull Favorite> filter(FavoriteFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getServiceType() != null && !filter.getServiceType().isBlank()) {
                predicates.add(cb.equal(root.get("serviceType"), filter.getServiceType()));
            }

            if (filter.getServiceCode() != null && !filter.getServiceCode().isBlank()) {
                predicates.add(cb.equal(root.get("serviceCode"), filter.getServiceCode()));
            }

            if (filter.getAppVersion() != null && !filter.getAppVersion().isBlank()) {
                predicates.add(cb.equal(root.get("appVersion"), filter.getAppVersion()));
            }

            if (filter.getContext() != null && !filter.getContext().isBlank()) {
                predicates.add(cb.equal(root.get("context"), filter.getContext()));
            }

            // keyword search (display_name LIKE %keyword%)
            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("displayName")),
                        "%" + filter.getKeyword().toLowerCase() + "%"));
            }

            // BaseModel.active
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            // created date range
            if (filter.getCreatedFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdDate"), filter.getCreatedFrom()));
            }

            if (filter.getCreatedTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdDate"), filter.getCreatedTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

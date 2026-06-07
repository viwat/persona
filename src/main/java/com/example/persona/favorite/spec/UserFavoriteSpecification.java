package com.example.persona.favorite.spec;

import com.example.persona.favorite.dto.UserFavoriteFilterRequest;
import com.example.persona.favorite.model.UserFavorite;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.domain.Specification;

public class UserFavoriteSpecification {

    public static Specification<@NonNull UserFavorite> filter(UserFavoriteFilterRequest filterRequest) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filterRequest.getCustomerKey() != null) {
                predicates.add(criteriaBuilder.equal(root.get("customerKey"), filterRequest.getCustomerKey()));
            }
            if (filterRequest.getServiceType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("serviceType"), filterRequest.getServiceType()));
            }
            if (filterRequest.getServiceCode() != null) {
                predicates.add(criteriaBuilder.equal(root.get("serviceCode"), filterRequest.getServiceCode()));
            }
            if (filterRequest.getIsPinned() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isPinned"), filterRequest.getIsPinned()));
            }
            if (filterRequest.getName() != null) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + filterRequest.getName().toLowerCase() + "%"));
            }
            if (filterRequest.getReferenceId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("referenceId"), filterRequest.getReferenceId()));
            }
            if (filterRequest.getContext() != null) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("context")),
                        "%" + filterRequest.getContext().toLowerCase() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

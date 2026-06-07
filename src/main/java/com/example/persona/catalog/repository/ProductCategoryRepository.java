package com.example.persona.catalog.repository;

import com.example.persona.catalog.model.ProductCategory;
import com.example.persona.enums.StatusType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    Optional<ProductCategory> findByCode(String code);

    List<ProductCategory> findAllByStatus(StatusType status);

    List<ProductCategory> findAllByStatusOrderByModifiedDateDesc(StatusType status);

    default List<ProductCategory> findAllActive() {
        return findAllByStatus(StatusType.ACTIVE);
    }

    default List<ProductCategory> findAllActiveOrderBySort() {
        return findAllByStatusOrderBySort(StatusType.ACTIVE);
    }

    List<ProductCategory> findAllByStatusOrderBySort(StatusType status);

    default List<ProductCategory> findAllMaintenance() {
        return findAllByStatus(StatusType.MAINTENANCE);
    }

    Optional<ProductCategory> findTopByOrderByModifiedDateDesc();
}

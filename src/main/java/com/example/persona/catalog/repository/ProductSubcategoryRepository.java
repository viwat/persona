package com.example.persona.catalog.repository;

import com.example.persona.catalog.model.ProductSubcategory;
import com.example.persona.enums.StatusType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductSubcategoryRepository extends JpaRepository<ProductSubcategory, Long> {
    List<ProductSubcategory> findAllByStatus(StatusType status);

    List<ProductSubcategory> findAllByCategoryIdAndStatus(Long categoryId, StatusType status);

    default List<ProductSubcategory> findAllActive() {
        return findAllByStatus(StatusType.ACTIVE);
    }

    default List<ProductSubcategory> findAllActiveByCategoryId(Long categoryId) {
        return findAllByCategoryIdAndStatus(categoryId, StatusType.ACTIVE);
    }
}

package com.example.persona.catalog.repository;

import com.example.persona.catalog.model.Product;
import com.example.persona.catalog.model.ProductSubcategory;
import com.example.persona.enums.StatusType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findBySubcategory(ProductSubcategory subcategory);

    List<Product> findAllByStatus(StatusType status);

    default List<Product> findAllActiveOrderByDisplayOrder() {
        return findAllByStatusOrderByDisplayOrder(StatusType.ACTIVE);
    }

    List<Product> findAllBySubcategoryIdAndStatusOrderByDisplayOrder(Long subcategoryId, StatusType status);

    default List<Product> findAllActiveBySubcategoryId(Long subcategoryId) {
        return findAllBySubcategoryIdAndStatusOrderByDisplayOrder(subcategoryId, StatusType.ACTIVE);
    }

    List<Product> findAllByStatusOrderByDisplayOrder(StatusType status);
}

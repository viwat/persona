package com.example.persona.transaction.repository;

import com.example.persona.enums.StatusType;
import com.example.persona.transaction.model.LimitThreshold;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LimitThresholdRepository extends JpaRepository<LimitThreshold, Long> {

    List<LimitThreshold> findByServiceTypeAndStatus(String serviceType, StatusType status);

    default List<LimitThreshold> findActiveByServiceType(String serviceType) {
        return findByServiceTypeAndStatus(serviceType, StatusType.ACTIVE);
    }

    List<LimitThreshold> findByMinAmountTypeAndStatus(String minAmountType, StatusType status);

    default List<LimitThreshold> findActiveByMinAmountType(String minAmountType) {
        return findByMinAmountTypeAndStatus(minAmountType, StatusType.ACTIVE);
    }

    List<LimitThreshold> findByMaxAmountTypeAndStatus(String maxAmountType, StatusType status);

    default List<LimitThreshold> findActiveByMaxAmountType(String maxAmountType) {
        return findByMaxAmountTypeAndStatus(maxAmountType, StatusType.ACTIVE);
    }

    List<LimitThreshold> findByMinAmountTypeAndMaxAmountTypeAndStatus(
            String minAmountType, String maxAmountType, StatusType status);

    default List<LimitThreshold> findActiveByAmountTypes(String minAmountType, String maxAmountType) {
        return findByMinAmountTypeAndMaxAmountTypeAndStatus(minAmountType, maxAmountType, StatusType.ACTIVE);
    }

    List<LimitThreshold> findByServiceTypeAndMinAmountTypeAndMaxAmountTypeAndStatus(
            String serviceType, String minAmountType, String maxAmountType, StatusType status);

    default List<LimitThreshold> findActiveByServiceTypeAndAmountTypes(
            String serviceType, String minAmountType, String maxAmountType) {
        return findByServiceTypeAndMinAmountTypeAndMaxAmountTypeAndStatus(
                serviceType, minAmountType, maxAmountType, StatusType.ACTIVE);
    }

    @Query(
            "SELECT t FROM LimitThreshold t WHERE t.status = :status AND t.minAmountType = :minAmountType AND t.maxAmountType = :maxAmountType AND (t.serviceType = :serviceType OR t.serviceType IS NULL)")
    List<LimitThreshold> findActiveByServiceTypeOrNullAndAmountTypes(
            @Param("serviceType") String serviceType,
            @Param("minAmountType") String minAmountType,
            @Param("maxAmountType") String maxAmountType,
            @Param("status") StatusType status);

    default List<LimitThreshold> findActiveByServiceTypeOrNullAndAmountTypes(
            String serviceType, String minAmountType, String maxAmountType) {
        return findActiveByServiceTypeOrNullAndAmountTypes(
                serviceType, minAmountType, maxAmountType, StatusType.ACTIVE);
    }
}

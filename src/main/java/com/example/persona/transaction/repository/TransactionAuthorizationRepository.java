package com.example.persona.transaction.repository;

import com.example.persona.enums.StatusType;
import com.example.persona.transaction.model.TransactionAuthorization;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionAuthorizationRepository extends JpaRepository<TransactionAuthorization, Long> {

    List<TransactionAuthorization> findByCustomerKeyAndStatus(String customerKey, StatusType status);

    default List<TransactionAuthorization> findActiveByCustomerKey(String customerKey) {
        return findByCustomerKeyAndStatus(customerKey, StatusType.ACTIVE);
    }

    Optional<TransactionAuthorization> findByCustomerKeyAndServiceTypeAndStatus(
            String customerKey, String serviceType, StatusType status);

    default Optional<TransactionAuthorization> findActiveByCustomerKeyAndServiceType(
            String customerKey, String serviceType) {
        return findByCustomerKeyAndServiceTypeAndStatus(customerKey, serviceType, StatusType.ACTIVE);
    }
}

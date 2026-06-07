package com.example.persona.transaction.repository;

import com.example.persona.enums.StatusType;
import com.example.persona.transaction.model.TransactionLimit;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionLimitRepository extends JpaRepository<TransactionLimit, Long> {

    Optional<TransactionLimit> findByAccountNoAndServiceTypeAndStatus(
            String accountNo, String serviceType, StatusType status);

    default Optional<TransactionLimit> findActiveByAccountNoAndServiceType(String accountNo, String serviceType) {
        return findByAccountNoAndServiceTypeAndStatus(accountNo, serviceType, StatusType.ACTIVE);
    }

    List<TransactionLimit> findByAccountNoAndStatus(String accountNo, StatusType status);

    default List<TransactionLimit> findActiveByAccountNo(String accountNo) {
        return findByAccountNoAndStatus(accountNo, StatusType.ACTIVE);
    }

    Optional<TransactionLimit> findByCustomerKeyAndServiceTypeAndStatus(
            String customerKey, String serviceType, StatusType status);

    default Optional<TransactionLimit> findActiveByCustomerKeyAndServiceType(String customerKey, String serviceType) {
        return findByCustomerKeyAndServiceTypeAndStatus(customerKey, serviceType, StatusType.ACTIVE);
    }

    List<TransactionLimit> findByCustomerKeyAndStatus(String customerKey, StatusType status);

    default List<TransactionLimit> findActiveByCustomerKey(String customerKey) {
        return findByCustomerKeyAndStatus(customerKey, StatusType.ACTIVE);
    }

    List<TransactionLimit> findByMasterAccountNoAndStatus(String masterAccountNo, StatusType status);

    default List<TransactionLimit> findActiveByMasterAccountNo(String masterAccountNo) {
        return findByMasterAccountNoAndStatus(masterAccountNo, StatusType.ACTIVE);
    }

    List<TransactionLimit> findByCustomerNoAndStatus(String customerNo, StatusType status);

    default List<TransactionLimit> findActiveByCustomerNo(String customerNo) {
        return findByCustomerNoAndStatus(customerNo, StatusType.ACTIVE);
    }
}

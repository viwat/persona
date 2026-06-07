package com.example.persona.transaction.repository;

import com.example.persona.transaction.model.TransactionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionDetailRepository extends JpaRepository<TransactionDetail, Long> {
    boolean existsByTransactionReference(String transactionReference);
}

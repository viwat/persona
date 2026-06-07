package com.example.persona.transaction.service;

import com.example.persona.transaction.model.TransactionDetail;
import java.util.List;
import java.util.Optional;

public interface TransactionDetailService {
    TransactionDetail save(TransactionDetail transactionDetail);

    Optional<TransactionDetail> findById(Long id);

    List<TransactionDetail> findAll();

    void delete(Long id);

    List<TransactionDetail> findByCustomerNo(String customerNo);

    List<TransactionDetail> findByAccountNo(String accountNo);

    boolean existsByTransactionReference(String transactionReference);
}

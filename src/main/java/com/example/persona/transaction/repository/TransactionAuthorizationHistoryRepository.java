package com.example.persona.transaction.repository;

import com.example.persona.transaction.model.TransactionAuthorizationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionAuthorizationHistoryRepository
        extends JpaRepository<TransactionAuthorizationHistory, Long> {}

package com.example.persona.transaction.repository;

import com.example.persona.transaction.model.TransactionLimitHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionLimitHistoryRepository extends JpaRepository<TransactionLimitHistory, Long> {}

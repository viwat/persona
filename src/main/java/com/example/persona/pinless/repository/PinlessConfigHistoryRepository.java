package com.example.persona.pinless.repository;

import com.example.persona.pinless.model.entity.PinlessConfigHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PinlessConfigHistoryRepository extends JpaRepository<PinlessConfigHistory, Long> {

    Page<PinlessConfigHistory> findByCustomerNo(String customerNo, Pageable pageable);
}

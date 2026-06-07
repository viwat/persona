package com.example.persona.pinless.service;

import com.example.persona.pinless.model.entity.PinlessTxnCounter;
import com.example.persona.pinless.repository.PinlessTxnCounterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PinlessAuditService {

    private final PinlessTxnCounterRepository txnCounterRepository;

    @Async
    public void saveRecord(PinlessTxnCounter pinlessTxnCounter) {
        try {
            txnCounterRepository.save(pinlessTxnCounter);
        } catch (Exception ex) {
            log.error(
                    "[PINLESS] Async audit write failed for customer={}: {}",
                    pinlessTxnCounter.getCustomerNo(),
                    ex.getMessage(),
                    ex);
        }
    }
}

package com.example.persona.service;

import com.example.persona.dto.ServiceOrderUpdateRequest;
import com.example.persona.exception.BusinessException;
import com.example.persona.theme.model.UserPersona;
import com.example.persona.theme.repository.UserPersonaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Slf4j
@RequiredArgsConstructor
public class UserPersonaOrderingService {

    private final UserPersonaRepository userPersonaRepository;

    @Transactional
    public void updateServiceOrder(String customerNo, ServiceOrderUpdateRequest request) {
        log.debug("Updating service order for customer: {}", customerNo);

        UserPersona userPersona = userPersonaRepository
                .findByCustomerNo(customerNo)
                .orElseThrow(() -> new BusinessException("User persona not found for customer: " + customerNo));

        validateServiceOrder(request.getServiceOrder());

        userPersona.setServiceOrders(request.getServiceOrder());
        userPersona.setVersion(userPersona.getVersion() + 1);

        userPersonaRepository.save(userPersona);
        log.info("Successfully updated service order for customer: {}", customerNo);
    }

    private void validateServiceOrder(List<String> serviceOrder) {
        if (serviceOrder == null || serviceOrder.isEmpty()) {
            throw new BusinessException("Service order cannot be empty");
        }

        // Add additional validation logic as needed
        // e.g., checking for duplicate services, validating service codes, etc.
    }
}

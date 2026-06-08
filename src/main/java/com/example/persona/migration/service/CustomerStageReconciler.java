package com.example.persona.migration.service;

import com.example.persona.enums.StatusType;
import com.example.persona.migration.enums.MigrationStageStatus;
import com.example.persona.migration.model.CustomerMigrationStage;
import com.example.persona.migration.model.MigrationStage;
import com.example.persona.migration.repository.CustomerMigrationStageRepository;
import com.example.persona.migration.repository.MigrationStageRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles initialization and reconciliation of per-customer migration stages
 * {@code dgtl_migration_stage} table.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerStageReconciler {

    private final MigrationStageRepository stageRepository;
    private final CustomerMigrationStageRepository customerStageRepository;

    /**
     * Initialize all active master stages for a brand-new customer with PENDING
     * status.
     *
     * <p>Must run inside a transaction started by a caller (e.g.
     * {@link #ensureStagesReadyForStatusCheck} or {@link #ensureInitializedOrReconciled});
     * kept private to avoid self-invocation on {@code @Transactional}.
     *
     * @param customerKey the customer key
     */
    private void initializeCustomerStages(String customerKey) {
        log.info("Initializing migration stages for new customer: {}", customerKey);

        List<MigrationStage> allStages = stageRepository.findAllActiveOrderByDisplayOrder();

        List<CustomerMigrationStage> customerStages = allStages.stream()
                .map(stage -> CustomerMigrationStage.builder()
                        .customerKey(customerKey)
                        .stage(stage)
                        .stageStatus(MigrationStageStatus.PENDING)
                        .status(StatusType.ACTIVE)
                        .retryCount(0)
                        .build())
                .collect(Collectors.toList());

        try {
            customerStageRepository.saveAll(customerStages);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Another thread already initialized stages for this customer (race at first-time submit).
            // The unique constraint (customer_key, stage_id) caught the duplicate — safe to ignore.
            log.warn("Concurrent stage initialization detected for customer: {} — rows already exist, skipping",
                    customerKey);
        }
    }

    /**
     * Load persisted stages with {@code JOIN FETCH} on {@code stage}; initialize when
     * none exist, otherwise reconcile against master
     */
    @Transactional
    public List<CustomerMigrationStage> ensureStagesReadyForStatusCheck(String customerKey) {
        List<CustomerMigrationStage> stages =
                customerStageRepository.findByCustomerKeyOrderByStageDisplayOrderAsc(customerKey);
        if (stages.isEmpty()) {
            initializeCustomerStages(customerKey);
            return customerStageRepository.findByCustomerKeyOrderByStageDisplayOrderAsc(customerKey);
        }
        return reconcileCustomerStages(customerKey, stages);
    }

    /**
     * Background jobs: create all stage rows when missing, otherwise reconcile with
     * master so newly added stages are picked up.
     */
    @Transactional
    public void ensureInitializedOrReconciled(String customerKey) {
        List<CustomerMigrationStage> existing =
                customerStageRepository.findByCustomerKeyOrderByStageDisplayOrderAsc(customerKey);
        if (existing.isEmpty()) {
            initializeCustomerStages(customerKey);
        } else {
            reconcileCustomerStages(customerKey, existing);
        }
    }

    /**
     * Reconcile an existing customer's stages against the master stage table.
     *
     * <p>When new stages are added to {@code dgtl_migration_stage}, this method
     * inserts missing {@link CustomerMigrationStage} rows (PENDING) so migration
     * is not considered complete until all stages (including new ones) are done.
     * If nothing has changed the existing list is returned as-is, avoiding an
     * unnecessary write.
     *
     * @param customerKey    the customer key
     * @param existingStages the customer's currently persisted stages (caller
     *                       already loaded these to avoid a redundant query)
     * @return the full ordered list of customer stages after reconciliation
     */
    private List<CustomerMigrationStage> reconcileCustomerStages(
            String customerKey, List<CustomerMigrationStage> existingStages) {

        List<MigrationStage> masterStages = stageRepository.findAllActiveOrderByDisplayOrder();

        Set<Long> existingStageIds =
                existingStages.stream().map(cs -> cs.getStage().getId()).collect(Collectors.toSet());

        List<CustomerMigrationStage> toAdd = masterStages.stream()
                .filter(stage -> !existingStageIds.contains(stage.getId()))
                .map(stage -> CustomerMigrationStage.builder()
                        .customerKey(customerKey)
                        .stage(stage)
                        .stageStatus(MigrationStageStatus.PENDING)
                        .status(StatusType.ACTIVE)
                        .retryCount(0)
                        .build())
                .collect(Collectors.toList());

        if (toAdd.isEmpty()) {
            return existingStages;
        }

        log.info("Reconciling migration stages for customer: {}, adding {} new stage(s)", customerKey, toAdd.size());

        customerStageRepository.saveAll(toAdd);

        // Merge and sort in memory to avoid an extra DB round-trip
        List<CustomerMigrationStage> merged = new ArrayList<>(existingStages);
        merged.addAll(toAdd);
        merged.sort(Comparator.comparing(
                cms -> cms.getStage().getDisplayOrder(), Comparator.nullsLast(Comparator.naturalOrder())));

        return merged;
    }
}

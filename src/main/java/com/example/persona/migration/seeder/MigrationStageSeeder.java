package com.example.persona.migration.seeder;

import com.example.persona.enums.StatusType;
import com.example.persona.migration.model.MigrationStage;
import com.example.persona.migration.repository.MigrationStageRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds the {@code dgtl_migration_stage} master table on first startup.
 *
 * <p>Only runs on the {@code local} profile. In deployed environments the DBA
 * (or Flyway) is responsible for inserting these rows.
 *
 * <p>Stage definitions mirror the registered {@link
 * com.example.persona.migration.handler.MigrationStageHandler} implementations:
 * <ol>
 *   <li>MASTER_ACCOUNT — mandatory; migrates master account row from Oracle
 *   <li>ACCOUNT — mandatory; enriches account profile data via CDP
 *   <li>DEVICE — <em>optional</em>; migrates device registration (may not exist)
 *   <li>CARD — optional, final; migrates virtual + physical cards
 * </ol>
 */
@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class MigrationStageSeeder {

    private final MigrationStageRepository stageRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        if (stageRepository.count() > 0) {
            log.debug("[MigrationStageSeeder] Stages already present — skipping");
            return;
        }

        List<MigrationStage> stages = List.of(
                stage(
                        "MASTER_ACCOUNT",
                        "Master Account Migration",
                        "Migrate master account data from Oracle to PostgreSQL",
                        1,
                        false,
                        true),
                stage("ACCOUNT", "Account Profile Migration", "Enrich account profile with CDP data", 2, false, true),
                stage(
                        "DEVICE",
                        "Device Migration",
                        "Migrate device registration data from Oracle (optional — customer may have no device)",
                        3,
                        false,
                        false),
                stage(
                        "CARD",
                        "Card Migration",
                        "Migrate virtual and physical card data from Oracle (final stage)",
                        4,
                        true,
                        false));

        stageRepository.saveAll(stages);
        log.info("[MigrationStageSeeder] Seeded {} migration stages", stages.size());
    }

    private static MigrationStage stage(
            String code, String name, String description, int order, boolean isFinal, boolean isMandatory) {
        return MigrationStage.builder()
                .code(code)
                .name(name)
                .description(description)
                .displayOrder(order)
                .isFinal(isFinal)
                .isMandatory(isMandatory)
                .status(StatusType.ACTIVE)
                .build();
    }
}

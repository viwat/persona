package com.example.persona.config;

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Config;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.model.TypoTolerance;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MeilisearchConfig {

    @Value("${meilisearch.host:http://localhost:7700}")
    private String host;

    @Value("${meilisearch.masterKey:masterKey}")
    private String masterKey;

    @Value("${meilisearch.index.locations:locations}")
    private String locationsIndexName;

    @Bean(name = "meilisearchClient")
    @ConditionalOnProperty(name = "meilisearch.enabled", havingValue = "true", matchIfMissing = true)
    public Client meilisearchClient() {
        log.info("Initializing Meilisearch client - Host: {}", host);

        if (host == null || host.trim().isEmpty()) {
            log.warn("Meilisearch host is not configured. Search functionality will be limited.");
            return null;
        }

        if (masterKey == null || masterKey.trim().isEmpty()) {
            log.warn("Meilisearch masterKey is not configured. Search functionality will be limited.");
            return null;
        }

        try {
            log.info("Creating Meilisearch client with host: {}", host);
            Client client = new Client(new Config(host, masterKey));

            try {
                client.health();
                log.info("Meilisearch client initialized successfully and health check passed");
            } catch (Exception healthException) {
                log.warn("Meilisearch client created but health check failed. Continuing anyway.", healthException);
            }

            return client;
        } catch (Exception e) {
            log.error("Failed to initialize Meilisearch connection. Search functionality will be limited.", e);
            return null;
        }
    }

    /**
     * Configure the locations Meilisearch index on startup (searchable, filterable, sortable
     * attributes, typo tolerance). Non-fatal — logs a warning if Meilisearch is unavailable.
     */
    @PostConstruct
    public void configureLocationIndex() {
        try {
            Client configClient = new Client(new Config(host, masterKey));

            try {
                configClient.createIndex(locationsIndexName, "id");
            } catch (Exception e) {
                log.debug("Index {} may already exist: {}", locationsIndexName, e.getMessage());
            }

            Index index = configClient.index(locationsIndexName);

            index.updateSearchableAttributesSettings(new String[] {
                "name",
                "searchText",
                "fullAddress",
                "province",
                "district",
                "commune",
                "street",
                "typeDisplayName",
                "availableServices"
            });

            index.updateFilterableAttributesSettings(
                    new String[] {"_geo", "type", "status", "province", "district", "commune"});

            index.updateSortableAttributesSettings(new String[] {"_geo", "name", "province"});

            TypoTolerance typoTolerance = new TypoTolerance();
            typoTolerance.setEnabled(true);
            index.updateTypoToleranceSettings(typoTolerance);

            log.info("Meilisearch index '{}' configured successfully", locationsIndexName);
        } catch (Exception e) {
            log.warn(
                    "Failed to configure Meilisearch location index (service may be unavailable at startup): {}",
                    e.getMessage());
        }
    }
}

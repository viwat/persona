package com.example.persona.location.config;

import com.example.persona.location.service.LocationSearchService;
import com.example.persona.location.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeilisearchStartupIndexer {

    private final LocationService locationService;
    private final LocationSearchService locationSearchService;

    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void onApplicationReady() {
        log.info("Starting Meilisearch startup configuration and reindex...");
        try {
            locationSearchService.configureIndex();
            var locations = locationService.findAll(null);
            locationSearchService.reindexAll(locations);
            log.info("Meilisearch startup reindex complete: {} locations indexed", locations.size());
        } catch (Exception e) {
            log.warn("Meilisearch startup configuration/reindex failed (non-fatal): {}", e.getMessage());
        }
    }
}

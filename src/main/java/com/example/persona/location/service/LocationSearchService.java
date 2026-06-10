package com.example.persona.location.service;

import com.example.persona.location.document.LocationDocument;
import com.example.persona.location.model.Coordinate;
import com.example.persona.location.model.Location;
import com.example.persona.location.model.LocationType;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.SearchRequest;
import com.meilisearch.sdk.model.SearchResult;
import com.meilisearch.sdk.model.Settings;
import com.meilisearch.sdk.model.TypoTolerance;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service("locationFeatureSearchService")
@RequiredArgsConstructor
public class LocationSearchService {

    private final Client meilisearchClient;
    private final ObjectMapper objectMapper;

    @Value("${meilisearch.index.locations}")
    private String indexName;

    public record NearbyIndexResult(UUID locationId) {}

    public record SearchIndexResult(List<UUID> locationIds, long totalHits) {}

    @CircuitBreaker(name = "meilisearch")
    @Retry(name = "meilisearch")
    public void index(Location location) {
        try {
            Index index = meilisearchClient.index(indexName);
            LocationDocument doc = LocationDocument.from(location);
            index.addDocuments(toJson(doc), "id");
            log.debug("Indexed location {} in Meilisearch", location.getId());
        } catch (Exception e) {
            log.error("Failed to index location {}: {}", location.getId(), e.getMessage());
            throw new RuntimeException("Meilisearch indexing failed", e);
        }
    }

    @CircuitBreaker(name = "meilisearch")
    public void delete(UUID id) {
        try {
            meilisearchClient.index(indexName).deleteDocument(id.toString());
            log.debug("Deleted location {} from Meilisearch", id);
        } catch (Exception e) {
            log.warn("Failed to delete location {} from Meilisearch: {}", id, e.getMessage());
        }
    }

    @CircuitBreaker(name = "meilisearch")
    @Retry(name = "meilisearch")
    public SearchIndexResult search(
            String text,
            List<LocationType> types,
            String province,
            String district,
            String commune,
            int offset,
            int limit) {
        try {
            Index index = meilisearchClient.index(indexName);
            SearchRequest.SearchRequestBuilder builder = SearchRequest.builder()
                    .q(text != null ? text : "")
                    .offset(offset)
                    .limit(limit)
                    .attributesToRetrieve(new String[] {"id"});

            String filter = buildFilter(types, province, district, commune);
            if (!filter.isBlank()) builder.filter(new String[] {filter});

            SearchResult result = (SearchResult) index.search(builder.build());
            List<UUID> ids = result.getHits().stream()
                    .map(hit -> UUID.fromString((String) hit.get("id")))
                    .collect(Collectors.toList());

            return new SearchIndexResult(ids, result.getEstimatedTotalHits());
        } catch (Exception e) {
            log.error("Meilisearch search failed: {}", e.getMessage());
            throw new RuntimeException("Meilisearch search failed", e);
        }
    }

    @CircuitBreaker(name = "meilisearch")
    @Retry(name = "meilisearch")
    public List<NearbyIndexResult> findNearby(Coordinate center, double radiusKm, List<LocationType> types, int limit) {
        try {
            Index index = meilisearchClient.index(indexName);
            double radiusMeters = radiusKm * 1000.0;
            String geoFilter = String.format(
                    "_geoRadius(%s, %s, %s)", center.getLatitude(), center.getLongitude(), (long) radiusMeters);

            SearchRequest.SearchRequestBuilder builder = SearchRequest.builder()
                    .q("")
                    .limit(limit)
                    .filter(new String[] {buildNearbyFilter(geoFilter, types)})
                    .sort(new String[] {
                        String.format("_geoPoint(%s, %s):asc", center.getLatitude(), center.getLongitude())
                    })
                    .attributesToRetrieve(new String[] {"id"});

            SearchResult result = (SearchResult) index.search(builder.build());
            return result.getHits().stream()
                    .map(hit -> new NearbyIndexResult(UUID.fromString((String) hit.get("id"))))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Meilisearch findNearby failed: {}", e.getMessage());
            throw new RuntimeException("Meilisearch findNearby failed", e);
        }
    }

    /**
     * Applies index settings required for the location feature. Without filterable
     * attributes (incl. {@code _geo}) Meilisearch rejects the type/province/geo filters
     * and every query silently falls back to PostgreSQL. Typo tolerance backs the
     * "show similar location names" requirement (FR-05.3). Idempotent — safe on every boot.
     */
    @CircuitBreaker(name = "meilisearch")
    public void configureIndex() {
        try {
            Index index = meilisearchClient.index(indexName);

            TypoTolerance typoTolerance = new TypoTolerance().setEnabled(true);
            HashMap<String, Integer> minWordSizeForTypos = new HashMap<>();
            minWordSizeForTypos.put("oneTypo", 4);
            minWordSizeForTypos.put("twoTypos", 8);
            typoTolerance.setMinWordSizeForTypos(minWordSizeForTypos);

            Settings settings = new Settings()
                    .setSearchableAttributes(new String[] {
                        "name",
                        "branchName",
                        "branchCode",
                        "atmSerial",
                        "searchText",
                        "fullAddress",
                        "province",
                        "district",
                        "commune"
                    })
                    .setFilterableAttributes(
                            new String[] {"_geo", "type", "status", "categoryCode", "province", "district", "commune"})
                    .setSortableAttributes(new String[] {"_geo"})
                    .setTypoTolerance(typoTolerance);

            index.updateSettings(settings);
            log.info("Configured Meilisearch index '{}' (filterable/sortable/searchable/typo)", indexName);
        } catch (Exception e) {
            log.error("Failed to configure Meilisearch index settings: {}", e.getMessage());
            throw new RuntimeException("Meilisearch settings update failed", e);
        }
    }

    @CircuitBreaker(name = "meilisearch")
    public void reindexAll(List<Location> locations) {
        try {
            Index index = meilisearchClient.index(indexName);
            List<LocationDocument> docs =
                    locations.stream().map(LocationDocument::from).collect(Collectors.toList());
            index.addDocuments(toJsonArray(docs), "id");
            log.info("Reindexed {} locations in Meilisearch", docs.size());
        } catch (Exception e) {
            log.error("Bulk reindex failed: {}", e.getMessage());
            throw new RuntimeException("Meilisearch bulk reindex failed", e);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String buildNearbyFilter(String geoFilter, List<LocationType> types) {
        StringBuilder filter = new StringBuilder(geoFilter);
        appendFilter(filter, "status = \"ACTIVE\"");
        if (types != null && !types.isEmpty()) {
            String typeFilter = types.stream()
                    .map(t -> "type = \"" + t.getCode() + "\"")
                    .collect(Collectors.joining(" OR ", "(", ")"));
            appendFilter(filter, typeFilter);
        }
        return filter.toString();
    }

    private String buildFilter(List<LocationType> types, String province, String district, String commune) {
        StringBuilder filter = new StringBuilder();
        if (types != null && !types.isEmpty()) {
            String typeFilter = types.stream()
                    .map(t -> "type = \"" + t.getCode() + "\"")
                    .collect(Collectors.joining(" OR ", "(", ")"));
            filter.append(typeFilter);
        }
        appendFilter(filter, "status = \"ACTIVE\"");
        if (province != null && !province.isBlank()) appendFilter(filter, "province = \"" + escape(province) + "\"");
        if (district != null && !district.isBlank()) appendFilter(filter, "district = \"" + escape(district) + "\"");
        if (commune != null && !commune.isBlank()) appendFilter(filter, "commune = \"" + escape(commune) + "\"");
        return filter.toString();
    }

    private void appendFilter(StringBuilder sb, String clause) {
        if (sb.length() > 0) sb.append(" AND ");
        sb.append(clause);
    }

    private String escape(String value) {
        return value.trim().replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String toJson(LocationDocument doc) {
        try {
            return objectMapper.writeValueAsString(doc);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize document", e);
        }
    }

    private String toJsonArray(List<LocationDocument> docs) {
        try {
            return objectMapper.writeValueAsString(docs);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize documents", e);
        }
    }
}

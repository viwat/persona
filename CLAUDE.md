# Persona Service — Claude Code Context

## What this project is

`persona` is a Wing Bank (Cambodia) Spring Boot microservice hosting several features
(catalog, favorites, dashboard, widgets, settings, migration, …). The **map location
service** lives inside it as a self-contained feature under
`com.example.persona.location`. It serves Branch, ATM/CRM, Agent, and Master Agent
locations to the Wing Bank mobile app and official website from a single PostgreSQL
source. Target: June 2026 app launch.

> **Architecture note:** the location feature was originally built as a standalone
> hexagonal (ports & adapters) project and has since been **migrated to a simple
> layered architecture** inside this service. There are no port interfaces or
> adapter classes anymore — do NOT reintroduce them. Controllers call services,
> services call repositories, full stop.

---

## Tech stack

| Component     | Technology                                                        |
|---------------|-------------------------------------------------------------------|
| Framework     | Spring Boot **4.0.5** / Java toolchain **25** / Gradle            |
| Database      | PostgreSQL (EDB-compatible — **no PostGIS**), Flyway migrations   |
| Search        | Meilisearch (`meilisearch-java` 0.18.0)                           |
| Cache         | Redis via **Lettuce** (Spring Data Redis) — Jedis was removed     |
| Object store  | AWS S3 SDK v2 (MinIO endpoint override for local dev)             |
| Resilience    | Resilience4j (`meilisearch` + `redis` circuit breakers, retry)    |
| Mapping       | MapStruct (Spring component model)                                |
| Observability | Actuator, Micrometer + OpenTelemetry tracing                      |
| API docs      | SpringDoc OpenAPI at /swagger-ui.html                             |
| Auth          | Host app's `@ZeroTrust` JWT aspect (local stubs); the location module currently uses an `X-Actor` header for admin attribution, no Spring Security |

---

## Location feature — layered layout

```
com.example.persona.location/
  controller/   LocationPublicController, LocationAdminController,
                CategoryController, CategoryAdminController, TagController,
                SearchAdminController, LocationExceptionHandler (scoped @RestControllerAdvice)
  service/      LocationService (orchestration: cache → DB, search → PG fallback, audit),
                LocationSearchService (Meilisearch, @CircuitBreaker + @Retry),
                LocationCacheService (Redis/Lettuce, @CircuitBreaker with no-op fallbacks),
                CategoryService (read), CategoryAdminService (write),
                AuditService, StorageService (S3 upload/delete)
  repository/   LocationJpaRepository (Haversine native query, ILIKE full-text fallback),
                LocationCategoryRepository, LocationTagRepository, AuditJpaRepository
  entity/       LocationEntity (table `locations`), AuditEventEntity (table `audit_events`)
  model/        Location (pure-Java aggregate: create/update/activate/deactivate/
                closeTemporarily/reopen), LocationType, LocationStatus, Coordinate,
                Address, ContactInfo, OpeningHours, ActionLink,
                LocationCategory + LocationTag (JPA entities extending BaseModel,
                multilingual via MultilingualContent _en/_km/_zh),
                AuditEvent, AuditAction
  document/     LocationDocument (flat Meilisearch doc, `_geo` point, `searchText` boost field)
  dto/          request/ (Create/Update/CloseLocationRequest, CategoryUpsertRequest,
                TagUpsertRequest), response/ (ApiResponse, PageResponse, LocationResponse,
                CategoryResponse, TagResponse)
  mapper/       LocationEntityMapper, LocationCategoryMapper, GoogleMapsUrlParser
  exception/    LocationNotFoundException, LocationDuplicateException, CategoryNotFoundException
  config/       S3Config, RequestLoggingFilter (X-Request-ID), MeilisearchStartupIndexer,
                MeilisearchHealthIndicator, OpenApiConfig
```

Mixed persistence styles, intentionally:
- `Location` is a **pure domain model** mapped to `LocationEntity` by `LocationEntityMapper`.
- `LocationCategory` / `LocationTag` are **direct JPA entities** following the host app's
  `BaseModel` + `MultilingualContent` conventions (BIGINT identity ids, `dgtl_` table prefix,
  auditing columns, `StatusType` status, optimistic `version`).

---

## API surface (all live, /api/v1)

### Public
| Method | Path | Notes |
|--------|------|-------|
| GET | /api/v1/locations | `types`, `page`=0, `size`=50 (max 100) |
| GET | /api/v1/locations/{id} | detail |
| GET | /api/v1/locations/search | `q`, `types`, `province`, `district`, `commune`, `page`=0, `size`=20 — Meilisearch, PG ILIKE fallback |
| GET | /api/v1/locations/nearby | `lat`, `lon` required; **`radius`=4.0 km, `limit`=5** (FR-05 defaults; max 50 km / 100) |
| POST | /api/v1/locations/nearby | same as GET via body |
| GET | /api/v1/categories, /api/v1/categories/{code} | active categories with tags, ordered by displayOrder |
| GET | /api/v1/tags, /api/v1/tags/{code} | |

### Admin (all take `X-Actor` header, default "system")
| Method | Path |
|--------|------|
| POST/PUT/DELETE | /api/v1/admin/locations[/{id}] |
| PATCH | /api/v1/admin/locations/{id}/activate · /deactivate · /close · /reopen |
| POST | /api/v1/admin/locations/{id}/images (multipart, `type`=logo\|cover, jpeg/png/webp ≤5 MB → S3) |
| GET | /api/v1/admin/locations/{id}/audit |
| POST/PUT/DELETE | /api/v1/admin/categories[/{code}] · /api/v1/admin/tags[/{code}] |
| POST | /api/v1/admin/search/reindex |

### Response envelope (location module)
```json
{ "success": true, "data": {}, "message": null, "error": {"code","message","fieldErrors"}, "timestamp": "…" }
```
Note: this differs from the host app's common `ApiResponse` (which has `trace_id`/`status`,
snake_case). The location envelope has **no traced_id** today — see "Remaining v2 work".

---

## Flyway migrations (V1–V9, `classpath:db/migration`, public schema)

| V | What |
|---|------|
| V1 | `locations` table — lat/lon DOUBLE PRECISION, JSONB opening_hours/available_services, type/status CHECKs, indexes, updated_at trigger |
| V2 | Seed 5 sample locations |
| V3 | Placeholder — how to add a location type |
| V4 | `google_maps_url` column |
| V5 | `logo_url`, `cover_url` (S3 images) |
| V6 | `temporarily_closed`, `closed_until`, `created_by`, `updated_by` |
| V7 | `audit_events` table (JSONB snapshot) |
| V8 | `dgtl_location_category`, `dgtl_location_tag`, `dgtl_location_category_tag` + seed of the 4 base categories (Khmer names) |
| V9 | Extended location fields: facebook_url, image_url, branch_code, branch_name, atm_serial, **category_code FK → dgtl_location_category(code)** (ON UPDATE CASCADE / ON DELETE RESTRICT), avg_rating CHECK 0–5, action_label, action_url; backfills category_code from type |

---

## Critical constraints — never revert these

### No PostGIS
- Plain `latitude` / `longitude` DOUBLE PRECISION columns.
- Nearby = SQL bounding-box pre-filter on `(latitude, longitude)` index → Haversine
  (native query `findNearbyRaw` and `Coordinate` math). Do not add `hibernate-spatial`/`jts`.

### Redis = Lettuce only
- Jedis was deliberately removed (commit 5867858). Use Spring Data Redis / Lettuce pool.
- Cache keys: `location:id:<uuid>` (TTL 300 s) and `location:all:<types>` (TTL 600 s);
  `evictAll()` scans `location:*`. Cache calls are wrapped in the `redis` circuit breaker
  with graceful no-op fallbacks — a Redis outage must never fail a request.

### Search resilience
- Meilisearch is primary for /search and /nearby; `meilisearch` circuit breaker + retry,
  and `LocationService` falls back to PostgreSQL (`fullTextSearch` ILIKE / Haversine raw).
- Index settings (searchable/filterable/sortable, typo tolerance) are applied by
  `LocationSearchService.configureIndex()`, invoked from `MeilisearchStartupIndexer`
  on ApplicationReadyEvent (async) and from POST /admin/search/reindex.
- Every location write re-indexes the document; keep that in any new mutation path.

### Category integrity
- `locations.category_code` is a real FK; categories use **soft delete** (status → DELETED)
  precisely so the RESTRICT FK never trips. Tags are hard-deleted (join rows cascade).
- `LocationService` validates category code exists and is ACTIVE before create/update.
- Category codes intentionally mirror `LocationType` codes (branch, atm_crm, agent,
  master_agent) during the enum→category transition. `validateCategory` **rejects a
  categoryCode that differs from the location's type code** — lift that guard only when
  the v2 API makes categories the single source of truth and `type` becomes derived.

### Domain invariants
- `Location.create/update` enforce required name/type/coordinate/address and trimming —
  route all mutations through the domain model, not direct entity edits.
- Soft-delete pattern: status ACTIVE/INACTIVE; `temporarily_closed` is orthogonal to status.
- Every admin mutation records an `AuditEvent` (action, actor, JSONB snapshot). Audit
  write failures are logged, never propagated.

---

## Remaining v2-spec work (gap list — verified against code 2026-06-11)

| # | Gap | Detail |
|---|-----|--------|
| 1 | **v2 response wrapper** `{code, title, message, data, traced_id}` | Not implemented. Location `ApiResponse` is `{success, data, message, error, timestamp}`. `RequestLoggingFilter` generates X-Request-ID but does **not** `MDC.put()` — wire that first if adding traced_id. Decide: new envelope on /api/v2 paths vs aligning with host app's common ApiResponse. |
| 2 | **Zoom-level clustering** (1–6 clusters, 7–12 aggregated markers, 13–15 pins, 16+ full) | Not implemented. No `zoom` param anywhere; needs a GROUP BY ROUND(lat,1)/ROUND(lon,1)-style cluster query + tiered response shapes. |
| 3 | **Per-category search behavior** (Branch: name OR province; ATM/Agent/Master Agent: name only) | Not implemented — search currently treats `province` as a uniform equality filter for all types. |
| 4 | **Khmer fields in Meilisearch** | `LocationDocument` has no `nameKm`/category Khmer names; searchable attributes are Latin-only. |
| 5 | **Config drift** | `application-local.yml` has `location.search.nearby-default-radius-km: 5.0` but the controller constant is 4.0 (code wins, FR-05-correct). Reconcile or delete the unused property; `nearby-ttl-seconds: 60` is also defined but unused. |
| 6 | **Integration tests** | All current tests are mock-based unit tests (domain, services, mapper). No @SpringBootTest / Testcontainers; the <1 s SLA (BRR-01) has no test assertion. |
| 7 | **Host-app integration** | Location module doesn't use `@ZeroTrust`, `@ApiVersion`, LocaleInterceptor, or the common ApiResponse/GlobalExceptionHandler — it is deliberately self-contained for now. Revisit when auth is enabled. |

Already DONE from the v2 plan (don't redo): categories+tags with multilingual content and
admin CRUD (V8), extended location fields + category FK (V9), nearby 4 km/5 defaults,
typo-tolerant "similar names" search, audit trail (V7), S3 image upload, Google Maps URL
parsing (V4), public category/tag list endpoints.

---

## Local development

```bash
docker-compose up -d        # postgres (16) + redis (7) only — Meilisearch and MinIO
                            # must be run separately if you need search / image upload
./gradlew bootRun           # `local` is the default profile (SPRING_PROFILES_ACTIVE)
./gradlew test
# Swagger: http://localhost:8080/swagger-ui.html
```

Local defaults (application-local.yml): Postgres `localhost:5432/persona` (postgres/postgres),
Redis `localhost:6379`, Meilisearch `http://localhost:7700` (masterKey), MinIO
`http://localhost:9000` (minioadmin/minioadmin, bucket `location-images`).

package com.example.persona.service;

import com.example.persona.catalog.dto.response.CategoryProductsResponse;
import com.example.persona.catalog.dto.response.ProductResponse;
import com.example.persona.catalog.dto.response.SubCategoryResponse;
import com.example.persona.catalog.model.Product;
import com.example.persona.catalog.model.ProductCategory;
import com.example.persona.catalog.model.ProductSubcategory;
import com.example.persona.catalog.repository.ProductCategoryRepository;
import com.example.persona.catalog.repository.ProductRepository;
import com.example.persona.catalog.repository.ProductSubcategoryRepository;
import com.example.persona.dto.CustomerBaseRequest;
import com.example.persona.dto.response.HomeScreenMetadata;
import com.example.persona.enums.ErrorCode;
import com.example.persona.exception.AppException;
import com.example.persona.utils.LanguageUtils;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class HomeService {

    private final ProductCategoryRepository categoryRepository;
    private final ProductSubcategoryRepository subcategoryRepository;
    private final ProductRepository productRepository;
    private final ObjectProvider<StringRedisTemplate> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CATEGORY_PRODUCTS_CACHE_KEY = "category:products:v1:";
    private static final int CACHE_TTL_SECONDS = 3600; // 1 hour cache
    private static final int MAX_RETRY_ATTEMPTS = 3;

    @Transactional(readOnly = true)
    public List<CategoryProductsResponse> getAllProducts(CustomerBaseRequest request) {
        String cacheKey = buildCacheKey(request.getCustomerSegment(), request.getCustomerSubSegment());

        StringRedisTemplate template = redisTemplate.getIfAvailable();
        if (template == null) {
            log.debug("Redis not available, fetching directly from database");
            return fetchFromDatabase(request.getCustomerSegment(), request.getCustomerSubSegment());
        }

        try {
            // Try to get fromEntity cache first
            List<String> cachedData = template.opsForList().range(cacheKey, 0, -1);
            if (!cachedData.isEmpty()) {
                log.debug("Cache hit for key: {}", cacheKey);
                return cachedData.stream().map(this::deserializeResponse).collect(Collectors.toList());
            }

            // Cache miss - fetch fromEntity database
            log.debug("Cache miss for key: {}", cacheKey);
            List<CategoryProductsResponse> response =
                    fetchFromDatabase(request.getCustomerSegment(), request.getCustomerSubSegment());

            // Store in cache with retry mechanism
            cacheResponse(cacheKey, response);

            return response;
        } catch (Exception e) {
            log.error("Error fetching products with cache", e);
            // Fallback to database on cache error
            return fetchFromDatabase(request.getCustomerSegment(), request.getCustomerSubSegment());
        }
    }

    private String buildCacheKey(String customerSegment, String customerSubSegment) {
        return CATEGORY_PRODUCTS_CACHE_KEY + (customerSegment != null ? customerSegment : "all") + ":"
                + (customerSubSegment != null ? customerSubSegment : "all");
    }

    private List<CategoryProductsResponse> fetchFromDatabase(String customerSegment, String customerSubSegment) {
        return categoryRepository.findAllActiveOrderBySort().stream()
                .map(category -> mapToCategoryResponse(category, customerSegment, customerSubSegment))
                .toList();
    }

    private CategoryProductsResponse deserializeResponse(String json) {
        try {
            return objectMapper.readValue(json, CategoryProductsResponse.class);
        } catch (JacksonException e) {
            log.error("Failed to deserialize cached response", e);
            throw new AppException("Failed to process cached data", ErrorCode.CACHE_ERROR, e);
        }
    }

    private void cacheResponse(String cacheKey, List<CategoryProductsResponse> response) {
        StringRedisTemplate template = redisTemplate.getIfAvailable();
        if (template == null) {
            log.warn("StringRedisTemplate not available, skipping cache write for key: {}", cacheKey);
            return;
        }

        int attempts = 0;
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                List<String> serialized = response.stream()
                        .map(item -> {
                            try {
                                return objectMapper.writeValueAsString(item);
                            } catch (JacksonException e) {
                                log.error("Failed to serialize item for caching", e);
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .toList();

                // Atomic clear + push + expire using pipeline
                template.executePipelined((RedisCallback<Object>) connection -> {
                    byte[] rawKey = template.getStringSerializer().serialize(cacheKey);
                    connection.keyCommands().del(rawKey);
                    serialized.forEach(item -> connection
                            .listCommands()
                            .rPush(rawKey, template.getStringSerializer().serialize(item)));
                    connection.keyCommands().expire(rawKey, CACHE_TTL_SECONDS);
                    return null;
                });

                return;

            } catch (Exception e) {
                attempts++;
                if (attempts == MAX_RETRY_ATTEMPTS) {
                    log.error("Failed to cache response after {} attempts", MAX_RETRY_ATTEMPTS, e);
                } else {
                    log.warn("Cache attempt {} failed, retrying...", attempts, e);
                    try {
                        Thread.sleep(100L * attempts);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }

    private CategoryProductsResponse mapToCategoryResponse(
            ProductCategory category, String customerSegment, String customerSubSegment) {

        return CategoryProductsResponse.builder()
                .categoryId(category.getId())
                .categoryCode(category.getCode())
                .categoryName(LanguageUtils.getLocalizedText(category.getName()))
                .categoryDescription(LanguageUtils.getLocalizedText(category.getDescription()))
                .categoryIconUrl(category.getIconUrl())
                .categoryBadgeUrl(LanguageUtils.getLocalizedText(category.getIconBadgeUrl()))
                .badgeDisplay(LanguageUtils.getLocalizedText(category.getBadgeDisplay()))
                .maintenanceMode(category.getMaintenanceMode() == 1)
                .maintenanceMessage(LanguageUtils.getLocalizedText(category.getMaintenance()))
                .sort(category.getSort())
                .subcategories(getSubcategoriesWithProducts(category, customerSegment, customerSubSegment))
                .build();
    }

    private List<ProductResponse> mapToProductResponses(List<Product> products) {
        return products.stream()
                .map(product -> ProductResponse.builder()
                        .id(product.getId())
                        .code(product.getProductCode())
                        .name(LanguageUtils.getLocalizedText(product.getName()))
                        .description(LanguageUtils.getLocalizedText(product.getDescription()))
                        .primaryIconUrl(product.getPrimaryIconUrl())
                        .build())
                .toList();
    }

    private List<SubCategoryResponse> getSubcategoriesWithProducts(
            ProductCategory category, String customerSegment, String customerSubSegment) {

        return subcategoryRepository.findAllActiveByCategoryId(category.getId()).stream()
                .map(subcategory -> mapToSubcategoryResponse(subcategory, customerSegment, customerSubSegment))
                .toList();
    }

    private SubCategoryResponse mapToSubcategoryResponse(
            ProductSubcategory subcategory, String customerSegment, String customerSubSegment) {

        List<Product> products = productRepository.findAllActiveBySubcategoryId(subcategory.getId());

        return SubCategoryResponse.builder()
                .id(subcategory.getId())
                .code(subcategory.getCode())
                .name(LanguageUtils.getLocalizedText(subcategory.getName()))
                .description(LanguageUtils.getLocalizedText(subcategory.getDescription()))
                .sort(subcategory.getSort())
                .products(mapToProductResponses(products))
                .build();
    }

    public HomeScreenMetadata getMetadata(CustomerBaseRequest request) {
        return HomeScreenMetadata.builder().build();
    }

    // Additional helper methods for mapping subcategories and products
    // ... existing code ...
}

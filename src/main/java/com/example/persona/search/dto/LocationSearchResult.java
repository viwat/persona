package com.example.persona.search.dto;

public record LocationSearchResult(
        String id,
        String name,
        String nameEn,
        String nameZh,
        String tags,
        String metadata,
        String deepLink,
        String imageUrl,
        String locationType,
        double latitude,
        double longitude) {}

package com.example.persona.search.dto;

public record BillerSearchResult(
        String id,
        String name,
        String nameKm,
        String nameZh,
        String category,
        String subCategory,
        String tags,
        String imageUrl,
        String deepLink,
        String metadata) {}

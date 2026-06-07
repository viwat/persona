package com.example.persona.search.dto;

public record ServiceSearchResult(
        String id, String code, String name, String description, String imageUrl, String location, String deep_link) {}

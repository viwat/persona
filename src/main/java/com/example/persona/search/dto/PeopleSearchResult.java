package com.example.persona.search.dto;

public record PeopleSearchResult(
        String id, String name, String description, String imageUrl, String location, String deepLink) {}

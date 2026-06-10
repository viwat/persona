package com.example.persona.location.dto.response;

import lombok.Builder;

/** FR-03 Tag projection: code + multilingual name. */
@Builder
public record TagResponse(String code, String name, String nameKhmer) {}

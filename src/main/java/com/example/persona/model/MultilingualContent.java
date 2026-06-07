package com.example.persona.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class MultilingualContent implements MultilingualField {

    @Column(name = "text_en")
    private String en;

    @Column(name = "text_km")
    private String km;

    @Column(name = "text_zh")
    private String zh;
}

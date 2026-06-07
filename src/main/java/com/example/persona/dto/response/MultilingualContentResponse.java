package com.example.persona.dto.response;

import com.example.persona.model.MultilingualContent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultilingualContentResponse {
    private String en;
    private String km;
    private String zh;

    public static MultilingualContentResponse fromEntity(MultilingualContent content) {
        if (content == null) {
            return null;
        }

        return MultilingualContentResponse.builder()
                .en(content.getEn())
                .km(content.getKm())
                .zh(content.getZh())
                .build();
    }
}

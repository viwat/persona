package com.example.persona.theme.dto.response;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AccentColorResponse {
    private String colorCode;
    private String displayName;
    private String rgbCode;
    private String iconUrl;
    private String hexValue;

    public String getHexValue() {
        return convertRgbToHex(rgbCode);
    }

    public String convertRgbToHex(String rgb) {
        if (rgb == null || rgb.isEmpty()) return null;

        // Extract numbers from formats like "255,87,51" or "rgb(255,87,51)"
        Pattern pattern = Pattern.compile("(\\d+)");
        Matcher matcher = pattern.matcher(rgb);

        int[] values = new int[3];
        int i = 0;

        while (matcher.find() && i < 3) {
            values[i++] = Integer.parseInt(matcher.group());
        }

        if (i != 3) {
            throw new IllegalArgumentException("Invalid RGB format");
        }

        return String.format("#%02X%02X%02X", values[0], values[1], values[2]);
    }
}

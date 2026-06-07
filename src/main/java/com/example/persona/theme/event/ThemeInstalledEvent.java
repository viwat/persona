package com.example.persona.theme.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ThemeInstalledEvent extends ApplicationEvent {
    private final String userId;
    private final String themeCode;

    public ThemeInstalledEvent(Object source, String userId, String themeCode) {
        super(source);
        this.userId = userId;
        this.themeCode = themeCode;
    }
}

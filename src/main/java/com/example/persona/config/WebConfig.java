package com.example.persona.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LocaleInterceptor localeInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeInterceptor)
                .addPathPatterns("/transactions/**", "/home/**")
                .addPathPatterns("/api/v1/locations/**")
                .addPathPatterns("/api/v1/settings/**")
                .addPathPatterns("/api/v1/themes/**")
                .excludePathPatterns("/actuator/**");
    }
}

package com.example.persona.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "system")
public class SystemProperties {

    private String env;
    private RedisTtl redisTtl = new RedisTtl();
    private Cdp cdp = new Cdp();

    @Getter
    @Setter
    public static class RedisTtl {
        private Long settingMs = 172800L;
        private Long favoriteMs = 172800L;
        private Long scheduleMs = 172800L;
    }

    @Getter
    @Setter
    public static class Cdp {
        private String baseUrl = "https://qa-gateway.wingmoney.com/cdp/1.0.0/wingmoney";
        private String accountInfoUrl = "/v3/account/detail?";
        private int timeout = 5;
    }
}

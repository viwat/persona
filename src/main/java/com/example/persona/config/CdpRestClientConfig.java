package com.example.persona.config;

import com.example.persona.client.CdpHttpClient;
import com.example.persona.config.properties.SystemProperties;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Profile("!local")
@Configuration
@RequiredArgsConstructor
public class CdpRestClientConfig {

    private final SystemProperties systemProperties;

    @Bean
    public CdpHttpClient cdpHttpClient() {
        SystemProperties.Cdp cdp = systemProperties.getCdp();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(cdp.getTimeout()));
        factory.setReadTimeout(Duration.ofSeconds(cdp.getTimeout()));

        RestClient restClient = RestClient.builder()
                .baseUrl(cdp.getBaseUrl())
                .requestFactory(factory)
                .build();

        HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(RestClientAdapter.create(restClient))
                .build();

        return proxyFactory.createClient(CdpHttpClient.class);
    }
}

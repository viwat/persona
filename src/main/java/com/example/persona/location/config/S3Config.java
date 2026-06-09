package com.example.persona.location.config;

import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@Slf4j
@Configuration
public class S3Config {

    @Value("${storage.s3.region:ap-southeast-1}")
    private String region;

    @Value("${storage.s3.access-key:minioadmin}")
    private String accessKey;

    @Value("${storage.s3.secret-key:minioadmin}")
    private String secretKey;

    /** Override endpoint for MinIO (local dev). Leave blank for real AWS. */
    @Value("${storage.s3.endpoint-override:}")
    private String endpointOverride;

    @Bean
    public S3Client s3Client() {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)));

        if (endpointOverride != null && !endpointOverride.isBlank()) {
            builder.endpointOverride(URI.create(endpointOverride)).forcePathStyle(true); // required for MinIO
            log.info("S3 client using endpoint override: {}", endpointOverride);
        }

        return builder.build();
    }
}

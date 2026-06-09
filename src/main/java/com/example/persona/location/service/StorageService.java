package com.example.persona.location.service;

import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
public class StorageService {

    private final S3Client s3Client;
    private final String bucket;
    private final String publicBaseUrl;

    public StorageService(
            S3Client s3Client,
            @Value("${storage.s3.bucket:location-images}") String bucket,
            @Value("${storage.s3.public-base-url:}") String publicBaseUrl) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.publicBaseUrl = (publicBaseUrl != null && !publicBaseUrl.isBlank())
                ? (publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1) : publicBaseUrl)
                : null;
    }

    public String upload(InputStream content, String contentType, String key) {
        try {
            byte[] bytes = content.readAllBytes();
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(contentType)
                            .contentLength((long) bytes.length)
                            .build(),
                    RequestBody.fromBytes(bytes));

            String url = publicBaseUrl != null
                    ? publicBaseUrl + "/" + key
                    : s3Client.utilities()
                            .getUrl(b -> b.bucket(bucket).key(key))
                            .toString();

            log.debug("Uploaded {} ({} bytes) → {}", key, bytes.length, url);
            return url;
        } catch (Exception e) {
            log.error("S3 upload failed for key {}: {}", key, e.getMessage());
            throw new RuntimeException("Image upload failed", e);
        }
    }

    public void delete(String key) {
        try {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder().bucket(bucket).key(key).build());
            log.debug("Deleted S3 object: {}", key);
        } catch (Exception e) {
            log.warn("S3 delete failed for key {}: {}", key, e.getMessage());
        }
    }
}

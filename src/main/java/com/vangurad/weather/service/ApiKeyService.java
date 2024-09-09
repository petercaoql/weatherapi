package com.vangurad.weather.service;

import com.vangurad.weather.exception.InvalidApiKeyException;
import com.vangurad.weather.exception.RateLimitException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ApiKeyService {

    @Value("${api.hourly-rate-limit}")
    private long rateLimit;

    private static final Set<String> API_KEY_SET = Set.of(
            "25c7a83616ae8632d5b9df84cfea4114",
            "6aa995dbe78f6c1bb99b0ad508828789",
            "2b54ab1771d2342aee487be95b304d87",
            "034f51b86f4ed35ae921a571485cb0aa",
            "621133d3793e3a41821d9aa69699bb5f"
    );

    private static final Map<String, Bucket> BUCKET_STORAGE = new ConcurrentHashMap<>();

    public boolean validateApiKey(String apiKey) {
        if (StringUtils.isBlank(apiKey) || !API_KEY_SET.contains(apiKey)) {
            throw new InvalidApiKeyException("Invalid API Key");
        }

        Bucket bucket = resolveBucket(apiKey);
        if (!bucket.tryConsume(1)) {
            throw new RateLimitException("API key has reached hourly limit");
        }

        return true;
    }

    private Bucket resolveBucket(String apiKey) {
        return BUCKET_STORAGE.computeIfAbsent(apiKey, this::createNewBucket);
    }

    private Bucket createNewBucket(String apiKey) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(rateLimit)
                .refillIntervally(rateLimit, Duration.ofHours(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}

package org.example.service;

import lombok.extern.log4j.Log4j2;
import org.example.model.ApiResponse;
import org.example.model.CacheEntry;
import org.example.model.LruCache;
import org.example.model.RateLimiter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
public class ApiServiceProxy implements ApiService{

    private final ApiService apiService;

    // LRU cache for API responses (max 50 entries). Each entry may also have an optional expiration time.
    private final Map<String, CacheEntry> cache = Collections.synchronizedMap(new LruCache<>(50));

    private final Map<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    // Optional per-URL locks to prevent duplicate fetches
    private final Map<String, Object> urlLocks = new ConcurrentHashMap<>();

    public ApiServiceProxy(ApiService apiService) {
        this.apiService = apiService;
    }

    public ApiResponse getData(String url) {
        return getData(url, Map.of(), LocalDateTime.now().plusDays(30));
    }

    public ApiResponse getData(String url, Map<String, String> headers) {
        return getData(url, headers, LocalDateTime.now().plusDays(30));
    }

    @Override
    public ApiResponse getData(String url, Map<String, String> headers, LocalDateTime evictionTime) {
        long currentTime = System.currentTimeMillis();

        RateLimiter rateLimiter = getRateLimiter(url);
        if (!rateLimiter.allowedRequest()) {
            System.out.println("[RATE LIMIT] Request blocked for " + url);
            log.warn("[RATE LIMIT] Request blocked for {}", url);
            return null;
        }

        if (checkMaliciousUrl(url)) {
            System.out.println("[BLOCKED] The URL is not allowed: " + url);
            log.warn("[BLOCKED] The URL is not allowed: {}", url);
            return null;
        }

        // Lock per url to prevent duplicate API fetches
        synchronized (getLock(url)) {
            // Check the cache if the data exists
            if (cache.containsKey(url)) {
                CacheEntry entry = cache.get(url);

                if (!entry.isExpired()) {
                    long duration = System.currentTimeMillis() - currentTime;
                    System.out.println("\n[CACHE HIT] Retrieved from cache: " + url + " (" + duration + "ms)");
                    log.info("[CACHE HIT] Retrieved from cache: {} ({}ms)", url, duration);
                    return cache.get(url).getData();
                }

                // If expired → evict it
                cache.remove(url);
                log.debug("Cache entry expired and removed for URL: {}", url);
            }

            // Fetch from the real service and cache the result
            ApiResponse data = apiService.getData(url, headers);

            cache.put(url, new CacheEntry(data, evictionTime));
            long duration = System.currentTimeMillis() - currentTime;
            System.out.println("\n[CACHE MISS] Fetched from API and cached: " + url + " (" + duration + "ms)");
            log.info("[CACHE MISS] Fetched from API and cached: {} ({}ms)", url, duration);
            return data;
        }

    }

    // Async methods using executor (queueing excess requests automatically)
    public CompletableFuture<ApiResponse> getDataAsync(String url) {
        return CompletableFuture.supplyAsync(() -> getData(url), executor);
    }

    public CompletableFuture<ApiResponse> getDataAsync(String url, Map<String, String> headers) {
        return CompletableFuture.supplyAsync(() -> getData(url, headers), executor);
    }

    public CompletableFuture<ApiResponse> getDataAsync(String url, Map<String, String> headers, LocalDateTime evictionTime) {
        return CompletableFuture.supplyAsync(() -> getData(url, headers, evictionTime), executor);
    }

    private Object getLock(String url) {
        return urlLocks.computeIfAbsent(url, k -> new Object());
    }

    private RateLimiter getRateLimiter(String url) {
        // If the limiter already exists, return it
        // Otherwise create a new one with defaults (e.g., 5 requests/sec)
        return rateLimiters.computeIfAbsent(url, k -> new RateLimiter(5, 1000));
    }

    private boolean checkMaliciousUrl(String url) {
        List<String> maliciousKeywords = List.of(
                "porn", "sex", "xxx", "gambling", "malware", "phishing", "hacking", "piracy", "torrent", "drugs",
                "violence", "scam", "casino", "adults", "nsfw", "fake", "fraud", "spyware", "cheat", "crack",
                "bypass", "illegal", "theft", "virus", "spy", "botnet", "keygen", "proxy", "attack", "exploit",
                "ransomware", "hack", "cybercrime", "threat", "inappropriate", "abuse", "childporn", "gamble",
                "lootbox", "counterfeit", "unauthorized", "blackhat", "trojan", "worm", "backdoor", "spybot", "adware",
                "pharma", "pirated", "hentai", "seduce", "erotic"
        );

        return maliciousKeywords.stream()
                .anyMatch(keyword -> url.toLowerCase().contains(keyword));
    }

}

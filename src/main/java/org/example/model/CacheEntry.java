package org.example.model;

import lombok.Getter;

import java.time.LocalDateTime;

public class CacheEntry {

    @Getter
    private final  ApiResponse data;              // Cached data returned to the user
    private final LocalDateTime evictionTime; // When this entry should expire (null = never)

    public CacheEntry( ApiResponse data, LocalDateTime evictionTime) {
        this.data = data;
        this.evictionTime = evictionTime;      // Optional expiration time
    }

    // Determines whether the cache entry should be removed
    public boolean isExpired() {
        // No eviction time → entry never expires
        if (evictionTime == null) return false;

        // Expired if current time is past eviction time
        return LocalDateTime.now().isAfter(evictionTime);
    }
}

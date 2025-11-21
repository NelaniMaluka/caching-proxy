package org.example.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple LRU (Least Recently Used) cache using LinkedHashMap.
 * Automatically evicts the least recently used entry when max size is exceeded.
 */
public class LruCache<K, V> extends LinkedHashMap<K, V> {

    private final int maxSize; // Maximum number of entries in the cache

    public LruCache(int maxSize) {
        super(16, 0.75f, true); // accessOrder = true enables LRU behavior
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize; // Evict the oldest entry if cache exceeds max size
    }
}

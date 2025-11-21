package org.example.model;

public class RateLimiter {

    private final int maxRequests; // Max number of requests allowed
    private final long timeWindowMillis; // Time window (e.g., 1000 ms = 1 second)

    private int requestCount = 0; // Requests made in the current window
    private long windowStart = System.currentTimeMillis(); // Start time of current window

    public RateLimiter(int maxRequests, long timeWindowMillis) {
        this.maxRequests = maxRequests;
        this.timeWindowMillis = timeWindowMillis;
    }

    // Controls whether a request is allowed under the rate limit
    public synchronized boolean allowedRequest() {
        long now = System.currentTimeMillis();

        // If the time window expired, → reset
        if (now - windowStart > timeWindowMillis) {
            requestCount = 0;
            windowStart = now;
        }

        // Allow request if under the limit
        if (requestCount < maxRequests) {
            requestCount++;
            return true;
        }

        // Otherwise → deny
        return false;
    }
}

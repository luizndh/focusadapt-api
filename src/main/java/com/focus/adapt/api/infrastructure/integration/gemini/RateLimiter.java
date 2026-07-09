package com.focus.adapt.api.infrastructure.integration.gemini;

import lombok.extern.slf4j.Slf4j;

/**
 * Thread-safe blocking Rate Limiter implementing the Token Bucket algorithm.
 * Limits outbound requests to a configured Requests Per Minute (RPM) threshold.
 */
@Slf4j
public class RateLimiter {

    private final double capacity;
    private final double refillRatePerMs;
    private double tokens;
    private long lastRefillTime;

    public RateLimiter(int rpm) {
        this.capacity = rpm;
        // Refill rate: rpm tokens per 60000 milliseconds
        this.refillRatePerMs = (double) rpm / 60000.0;
        this.tokens = rpm;
        this.lastRefillTime = System.currentTimeMillis();
    }

    /**
     * Acquires a token, blocking (sleeping) if necessary until one becomes available.
     */
    public synchronized void acquire() throws InterruptedException {
        refill();

        while (tokens < 1.0) {
            // Calculate time needed to generate the missing tokens
            double missingTokens = 1.0 - tokens;
            long waitMs = (long) Math.ceil(missingTokens / refillRatePerMs);

            if (waitMs > 0) {
                log.info("Rate limiter: bucket exhausted. Sleeping for {} ms to acquire token...", waitMs);
                Thread.sleep(waitMs);
            }
            refill();
        }

        tokens -= 1.0;
    }

    private void refill() {
        long now = System.currentTimeMillis();
        long elapsedMs = now - lastRefillTime;

        if (elapsedMs > 0) {
            double refillTokens = elapsedMs * refillRatePerMs;
            tokens = Math.min(capacity, tokens + refillTokens);
            lastRefillTime = now;
        }
    }

    // Helper getter for testing
    public synchronized double getTokens() {
        refill();
        return tokens;
    }
}

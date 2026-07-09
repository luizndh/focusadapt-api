package com.focus.adapt.api.infrastructure.integration.gemini;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterTest {

    @Test
    void shouldAcquireImmediatelyWhenTokensAvailable() throws Exception {
        RateLimiter limiter = new RateLimiter(10);
        
        long start = System.currentTimeMillis();
        limiter.acquire();
        limiter.acquire();
        long elapsed = System.currentTimeMillis() - start;

        // Since bucket capacity is 10, both calls must execute without block (under 50ms)
        assertTrue(elapsed < 50, "Acquiring available tokens should not block");
        assertTrue(limiter.getTokens() <= 8.0, "Bucket tokens count should reflect consumption");
    }
}

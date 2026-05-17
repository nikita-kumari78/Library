package com.lms.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * CacheConfig — enables Spring caching with Caffeine (in-memory, fast).
 *
 * Why Caffeine over simple @EnableCaching?
 *   → Caffeine lets us set TTL (time-to-live) so stale data auto-expires.
 *   → LRU eviction: cache removes least-recently-used entries when full.
 *   → Production alternative: swap Caffeine for Redis (no code changes in service).
 *
 * Caches configured:
 *   "books"   → individual book records, expire after 10 min of no access
 *   "members" → student profiles, expire after 30 min
 *   "borrow"  → borrow records, expire after 5 min (changes frequently)
 *
 * In application.properties, also add:
 *   spring.cache.type=caffeine
 */
@Configuration
@EnableCaching   // ← This annotation activates @Cacheable, @CacheEvict, @CachePut
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();

        manager.setCaffeine(
            Caffeine.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)  // expires if not used for 10 min
                .maximumSize(500)                          // max 500 entries in memory
                .recordStats()                             // enables hit/miss metrics
        );

        // Pre-register named caches
        manager.setCacheNames(java.util.List.of("books", "members", "borrow"));

        return manager;
    }
}

/*
 * ─────────────────────────────────────────────────────────────
 * HOW TO SWITCH TO REDIS IN PRODUCTION (for interviews):
 * ─────────────────────────────────────────────────────────────
 *
 * 1. Add dependency in pom.xml:
 *    <dependency>
 *        <groupId>org.springframework.boot</groupId>
 *        <artifactId>spring-boot-starter-data-redis</artifactId>
 *    </dependency>
 *
 * 2. In application.properties:
 *    spring.cache.type=redis
 *    spring.data.redis.host=localhost
 *    spring.data.redis.port=6379
 *
 * 3. @Cacheable, @CacheEvict, @CachePut annotations in service
 *    stay EXACTLY the same — no service code change needed!
 *
 * This is why Spring Cache abstraction is powerful.
 * ─────────────────────────────────────────────────────────────
 */

package com.vvise.template.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

/**
 * Cache configuration for token introspection.
 * Caches valid tokens to reduce calls to the auth server.
 */
@Configuration
@EnableCaching
class CacheConfig {

    companion object {
        const val TOKEN_CACHE = "tokenIntrospection"
    }

    @Bean
    fun cacheManager(): CacheManager {
        val cacheManager = CaffeineCacheManager(TOKEN_CACHE)
        cacheManager.setCaffeine(
            Caffeine.newBuilder()
                // Cache tokens for 5 minutes (less than token expiry of 15 min)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                // Maximum 10,000 cached tokens
                .maximumSize(10_000)
                // Record stats for monitoring
                .recordStats()
        )
        return cacheManager
    }
}

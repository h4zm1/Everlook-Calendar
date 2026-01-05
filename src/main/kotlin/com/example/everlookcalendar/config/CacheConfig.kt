package com.example.everlookcalendar.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit


@Configuration
@EnableCaching
class CacheConfig {

    @Bean
    fun cacheManager(): CacheManager {
        val caffeine = Caffeine.newBuilder()
        return SimpleCacheManager().apply {
            setCaches(
                listOf(
                    buildCache("events", 24, TimeUnit.HOURS),
                    buildCache("zgboss", 24, TimeUnit.HOURS),
                )
            )
        }
    }

    private fun buildCache(name: String, duration: Long, unit: TimeUnit): CaffeineCache {
        return CaffeineCache(
            name,
            Caffeine.newBuilder()
                .expireAfterWrite(duration, unit)
                .maximumSize(5)
                .build()
        )
    }
}

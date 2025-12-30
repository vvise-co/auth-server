package com.vvise.auth.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class CorsConfig {

    private val log = LoggerFactory.getLogger(CorsConfig::class.java)

    @Value("\${app.cors.allowed-origins:}")
    private lateinit var allowedOrigins: String

    @Value("\${app.cors.allowed-origin-patterns:}")
    private lateinit var allowedOriginPatterns: String

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            // Add specific origins
            val origins = this@CorsConfig.allowedOrigins.split(",").map { it.trim() }.filter { it.isNotBlank() }
            if (origins.isNotEmpty()) {
                allowedOrigins = origins
                log.info("CORS allowed origins: $origins")
            }

            // Add origin patterns (supports wildcards like https://*.koyeb.app)
            val patterns = this@CorsConfig.allowedOriginPatterns.split(",").map { it.trim() }.filter { it.isNotBlank() }
            if (patterns.isNotEmpty()) {
                allowedOriginPatterns = patterns
                log.info("CORS allowed origin patterns: $patterns")
            }

            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            allowedHeaders = listOf("*")
            exposedHeaders = listOf("Authorization", "Content-Type")
            allowCredentials = true
            maxAge = 3600L
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }
}

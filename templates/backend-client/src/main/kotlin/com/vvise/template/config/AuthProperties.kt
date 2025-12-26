package com.vvise.template.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for connecting to the central auth server.
 * These values should match the auth server's configuration.
 */
@ConfigurationProperties(prefix = "auth.server")
data class AuthProperties(
    /**
     * Base URL of the auth server (e.g., http://localhost:8081)
     */
    val baseUrl: String,

    /**
     * JWT secret key - MUST match the auth server's JWT_SECRET
     */
    val jwtSecret: String,

    /**
     * Access token expiration in milliseconds
     */
    val accessTokenExpiration: Long = 900000 // 15 minutes
)

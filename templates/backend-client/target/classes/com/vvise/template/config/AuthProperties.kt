package com.vvise.template.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for connecting to the central auth server.
 * No JWT_SECRET required - token validation is done via introspection.
 */
@ConfigurationProperties(prefix = "auth.server")
data class AuthProperties(
    /**
     * Base URL of the auth server (e.g., https://auth.koyeb.app)
     */
    val authServerUrl: String,

    /**
     * Cache TTL for token introspection in seconds (default: 300 = 5 minutes)
     */
    val cacheTtlSeconds: Long = 300
)

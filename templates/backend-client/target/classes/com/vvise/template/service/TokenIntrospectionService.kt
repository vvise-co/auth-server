package com.vvise.template.service

import com.vvise.template.config.AuthProperties
import com.vvise.template.config.CacheConfig
import com.vvise.template.dto.TokenIntrospectionResponse
import com.vvise.template.security.AuthenticatedUser
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

/**
 * Service for validating tokens via the auth server's introspection endpoint.
 * Results are cached to minimize network calls.
 *
 * No JWT_SECRET required - tokens are validated by the auth server.
 */
@Service
class TokenIntrospectionService(
    private val authProperties: AuthProperties
) {
    private val logger = LoggerFactory.getLogger(TokenIntrospectionService::class.java)
    private val restTemplate = RestTemplate()

    /**
     * Introspect a token with the auth server.
     * Results are cached for 5 minutes to reduce auth server load.
     *
     * @param token The access token to validate
     * @return TokenIntrospectionResponse with user info if valid, or inactive response if invalid
     */
    @Cacheable(value = [CacheConfig.TOKEN_CACHE], key = "#token", unless = "#result == null || !#result.active")
    fun introspect(token: String): TokenIntrospectionResponse? {
        return try {
            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }
            val body = mapOf("token" to token)
            val entity = HttpEntity(body, headers)

            val response = restTemplate.postForEntity(
                "${authProperties.authServerUrl}/api/auth/introspect",
                entity,
                TokenIntrospectionResponse::class.java
            )

            response.body
        } catch (ex: Exception) {
            logger.error("Failed to introspect token with auth server: ${ex.message}")
            null
        }
    }

    /**
     * Validate token and return AuthenticatedUser if valid.
     *
     * @param token The access token to validate
     * @return AuthenticatedUser if token is valid, null otherwise
     */
    fun validateAndGetUser(token: String): AuthenticatedUser? {
        val response = introspect(token) ?: return null

        if (!response.active) {
            return null
        }

        val userId = response.sub?.toLongOrNull() ?: return null
        val email = response.email ?: return null

        return AuthenticatedUser(
            id = userId,
            email = email,
            name = response.name ?: email,
            roles = response.getRolesList(),
            imageUrl = response.imageUrl
        )
    }
}

package com.vvise.template.service

import com.vvise.template.config.AuthProperties
import com.vvise.template.dto.UserDto
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange

/**
 * Client for communicating with the central auth server.
 * Use this to fetch additional user data or validate tokens server-to-server.
 */
@Service
class AuthServerClient(
    private val authProperties: AuthProperties
) {
    private val logger = LoggerFactory.getLogger(AuthServerClient::class.java)
    private val restTemplate = RestTemplate()

    /**
     * Fetch user details from the auth server.
     */
    fun getUserById(userId: Long, accessToken: String): UserDto? {
        return try {
            val headers = HttpHeaders().apply {
                setBearerAuth(accessToken)
            }
            val entity = HttpEntity<Any>(headers)

            val response = restTemplate.exchange<UserDto>(
                "${authProperties.authServerUrl}/api/users/$userId",
                HttpMethod.GET,
                entity
            )

            response.body
        } catch (ex: Exception) {
            logger.error("Failed to fetch user $userId from auth server", ex)
            null
        }
    }

    /**
     * Validate a token with the auth server (optional - JWT validation is usually sufficient).
     */
    fun validateTokenWithServer(accessToken: String): UserDto? {
        return try {
            val headers = HttpHeaders().apply {
                setBearerAuth(accessToken)
            }
            val entity = HttpEntity<Any>(headers)

            val response = restTemplate.exchange<UserDto>(
                "${authProperties.authServerUrl}/api/auth/me",
                HttpMethod.GET,
                entity
            )

            response.body
        } catch (ex: Exception) {
            logger.error("Failed to validate token with auth server", ex)
            null
        }
    }

    /**
     * Get the auth server base URL for redirecting to login.
     */
    fun getAuthServerUrl(): String = authProperties.authServerUrl

    /**
     * Get the OAuth2 authorization URL for a specific provider.
     */
    fun getOAuth2AuthorizationUrl(provider: String): String {
        return "${authProperties.authServerUrl}/oauth2/authorization/$provider"
    }
}

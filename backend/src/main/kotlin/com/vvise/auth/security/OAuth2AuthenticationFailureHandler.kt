package com.vvise.auth.security

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class OAuth2AuthenticationFailureHandler : SimpleUrlAuthenticationFailureHandler() {

    private val log = LoggerFactory.getLogger(OAuth2AuthenticationFailureHandler::class.java)

    @Value("\${app.oauth2.authorized-redirect-uri}")
    private lateinit var defaultRedirectUri: String

    @Value("\${app.oauth2.allowed-redirect-domains:}")
    private lateinit var allowedRedirectDomains: String

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        log.error("OAuth2 authentication failed: ${exception.message}")

        // Get redirect URI from cookie or use default
        val redirectUri = getRedirectUriFromCookie(request) ?: defaultRedirectUri

        // Clear the redirect URI cookie
        clearRedirectUriCookie(response)

        val targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
            .queryParam("error", "auth_failed")
            .build().toUriString()

        redirectStrategy.sendRedirect(request, response, targetUrl)
    }

    private fun getRedirectUriFromCookie(request: HttpServletRequest): String? {
        val cookie = request.cookies?.find { it.name == OAuth2AuthenticationSuccessHandler.REDIRECT_URI_COOKIE }
        val redirectUri = cookie?.value

        if (redirectUri.isNullOrBlank()) {
            return null
        }

        return if (isValidRedirectUri(redirectUri)) {
            // For failure, redirect to login page of the client app
            try {
                val uri = URI.create(redirectUri)
                "${uri.scheme}://${uri.host}${if (uri.port != -1 && uri.port != 80 && uri.port != 443) ":${uri.port}" else ""}/login"
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    private fun isValidRedirectUri(uri: String): Boolean {
        return try {
            val parsedUri = URI.create(uri)
            val host = parsedUri.host ?: return false

            if (host == "localhost" || host == "127.0.0.1") {
                return true
            }

            val allowedDomains = allowedRedirectDomains
                .split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }

            if (allowedDomains.isEmpty()) {
                val defaultHost = URI.create(defaultRedirectUri).host
                return host == defaultHost
            }

            allowedDomains.any { domain ->
                if (domain.startsWith("*.")) {
                    val suffix = domain.substring(1)
                    host.endsWith(suffix) || host == domain.substring(2)
                } else {
                    host == domain
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun clearRedirectUriCookie(response: HttpServletResponse) {
        val cookie = Cookie(OAuth2AuthenticationSuccessHandler.REDIRECT_URI_COOKIE, "")
        cookie.path = "/"
        cookie.maxAge = 0
        response.addCookie(cookie)
    }
}

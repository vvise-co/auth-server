package com.vvise.auth.security

import com.vvise.auth.service.RefreshTokenService
import com.vvise.auth.service.UserService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class OAuth2AuthenticationSuccessHandler(
    private val tokenProvider: JwtTokenProvider,
    private val refreshTokenService: RefreshTokenService,
    private val userService: UserService
) : SimpleUrlAuthenticationSuccessHandler() {

    private val log = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler::class.java)

    @Value("\${app.oauth2.authorized-redirect-uri}")
    private lateinit var defaultRedirectUri: String

    @Value("\${app.oauth2.allowed-redirect-domains:}")
    private lateinit var allowedRedirectDomains: String

    companion object {
        const val REDIRECT_URI_COOKIE = "oauth2_redirect_uri"
    }

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val targetUrl = determineTargetUrl(request, authentication)

        // Clear the redirect URI cookie
        clearRedirectUriCookie(response)

        if (response.isCommitted) {
            log.debug("Response has already been committed. Unable to redirect to $targetUrl")
            return
        }

        clearAuthenticationAttributes(request)
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }

    private fun determineTargetUrl(request: HttpServletRequest, authentication: Authentication): String {
        val userPrincipal = authentication.principal as UserPrincipal

        val accessToken = tokenProvider.generateAccessToken(userPrincipal)

        // Get full user to create refresh token
        val user = userService.findById(userPrincipal.id)
            ?: throw RuntimeException("User not found")

        // Delete existing refresh tokens for this user
        refreshTokenService.deleteByUser(user)

        // Create new refresh token
        val refreshToken = refreshTokenService.createRefreshToken(user)

        // Get redirect URI from cookie or use default
        val redirectUri = getRedirectUriFromCookie(request) ?: defaultRedirectUri

        return UriComponentsBuilder.fromUriString(redirectUri)
            .queryParam("token", accessToken)
            .queryParam("refreshToken", refreshToken.token)
            .build().toUriString()
    }

    private fun getRedirectUriFromCookie(request: HttpServletRequest): String? {
        val cookie = request.cookies?.find { it.name == REDIRECT_URI_COOKIE }
        val redirectUri = cookie?.value

        if (redirectUri.isNullOrBlank()) {
            return null
        }

        // Validate the redirect URI
        return if (isValidRedirectUri(redirectUri)) {
            redirectUri
        } else {
            log.warn("Invalid redirect URI in cookie: $redirectUri")
            null
        }
    }

    private fun isValidRedirectUri(uri: String): Boolean {
        return try {
            val parsedUri = URI.create(uri)
            val host = parsedUri.host ?: return false

            // Allow localhost for development
            if (host == "localhost" || host == "127.0.0.1") {
                return true
            }

            // Check against allowed domains
            val allowedDomains = allowedRedirectDomains
                .split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }

            if (allowedDomains.isEmpty()) {
                // If no domains configured, only allow same origin as default
                val defaultHost = URI.create(defaultRedirectUri).host
                return host == defaultHost
            }

            // Check if host matches any allowed domain (supports wildcards like *.koyeb.app)
            allowedDomains.any { domain ->
                if (domain.startsWith("*.")) {
                    val suffix = domain.substring(1)
                    host.endsWith(suffix) || host == domain.substring(2)
                } else {
                    host == domain
                }
            }
        } catch (e: Exception) {
            log.error("Failed to parse redirect URI: $uri", e)
            false
        }
    }

    private fun clearRedirectUriCookie(response: HttpServletResponse) {
        val cookie = Cookie(REDIRECT_URI_COOKIE, "")
        cookie.path = "/"
        cookie.maxAge = 0
        response.addCookie(cookie)
    }
}

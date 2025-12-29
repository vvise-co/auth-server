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
        const val ACCESS_TOKEN_COOKIE = "access_token"
        const val REFRESH_TOKEN_COOKIE = "refresh_token"
        const val ACCESS_TOKEN_MAX_AGE = 60 * 15 // 15 minutes
        const val REFRESH_TOKEN_MAX_AGE = 60 * 60 * 24 * 7 // 7 days
    }

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        log.info("=== OAuth2 Authentication Success ===")
        log.info("User authenticated: ${authentication.name}")
        log.info("Principal type: ${authentication.principal::class.simpleName}")

        val userPrincipal = authentication.principal as UserPrincipal
        log.info("Generating tokens for user: ${userPrincipal.id}")

        // Generate access token
        val accessToken = tokenProvider.generateAccessToken(userPrincipal)
        log.info("Access token generated (length: ${accessToken.length})")

        // Get full user to create refresh token
        val user = userService.findById(userPrincipal.id)
            ?: throw RuntimeException("User not found")

        // Delete existing refresh tokens for this user
        refreshTokenService.deleteByUser(user)

        // Create new refresh token
        val refreshToken = refreshTokenService.createRefreshToken(user)
        log.info("Refresh token created")

        // Determine if we should use secure cookies
        val isSecure = isSecureRequest(request)
        log.info("Using secure cookies: $isSecure")

        // Determine redirect URL and whether to include tokens in URL
        val targetUrl = determineTargetUrl(request, accessToken, refreshToken.token)
        log.info("Target redirect URL: $targetUrl")

        // Set auth cookies for same-domain requests (they work via SameSite=Lax)
        setAuthCookies(response, accessToken, refreshToken.token, isSecure)
        log.info("Auth cookies set on response")

        // Clear the redirect URI cookie
        clearRedirectUriCookie(response)

        if (response.isCommitted) {
            log.warn("Response has already been committed. Unable to redirect to $targetUrl")
            return
        }

        clearAuthenticationAttributes(request)
        log.info("Redirecting user to: $targetUrl")
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }

    private fun determineTargetUrl(request: HttpServletRequest, accessToken: String, refreshToken: String): String {
        // Get redirect URI from cookie or use default
        val cookieRedirectUri = getRedirectUriFromCookie(request)
        val redirectUri = cookieRedirectUri ?: defaultRedirectUri
        log.info("Cookie redirect URI: $cookieRedirectUri")
        log.info("Default redirect URI: $defaultRedirectUri")
        log.info("Using redirect URI: $redirectUri")

        // Check if this is a cross-domain redirect (client app)
        val isCrossDomain = isCrossDomainRedirect(request, redirectUri)
        log.info("Is cross-domain redirect: $isCrossDomain")

        return if (isCrossDomain) {
            // For cross-domain (client apps), pass tokens in URL since cookies won't work cross-site
            val separator = if (redirectUri.contains("?")) "&" else "?"
            "$redirectUri${separator}token=$accessToken&refreshToken=$refreshToken"
        } else {
            // For same-domain (auth server frontend), cookies are already set
            redirectUri
        }
    }

    private fun isCrossDomainRedirect(request: HttpServletRequest, redirectUri: String): Boolean {
        return try {
            val redirectHost = URI.create(redirectUri).host ?: return false
            val requestHost = request.getHeader("X-Forwarded-Host")
                ?: request.getHeader("Host")?.split(":")?.firstOrNull()
                ?: request.serverName

            log.debug("Checking cross-domain: redirectHost=$redirectHost, requestHost=$requestHost")

            // If redirect is to a different host, it's cross-domain
            redirectHost != requestHost
        } catch (e: Exception) {
            log.warn("Failed to determine if cross-domain: ${e.message}")
            false
        }
    }

    private fun isSecureRequest(request: HttpServletRequest): Boolean {
        // Check if request is behind HTTPS proxy
        val forwardedProto = request.getHeader("X-Forwarded-Proto")
        if (forwardedProto == "https") {
            return true
        }

        // Check if direct HTTPS
        if (request.isSecure) {
            return true
        }

        // Check host for cloud deployment
        val host = request.getHeader("X-Forwarded-Host") ?: request.getHeader("Host") ?: ""
        if (host.contains("koyeb") || host.contains("railway")) {
            return true
        }

        // Default to false for localhost development
        return false
    }

    private fun setAuthCookies(
        response: HttpServletResponse,
        accessToken: String,
        refreshToken: String,
        secure: Boolean
    ) {
        // Access token cookie
        val accessCookie = Cookie(ACCESS_TOKEN_COOKIE, accessToken).apply {
            isHttpOnly = true
            this.secure = secure
            path = "/"
            maxAge = ACCESS_TOKEN_MAX_AGE
        }
        // Set SameSite attribute
        response.addHeader(
            "Set-Cookie",
            "${ACCESS_TOKEN_COOKIE}=$accessToken; Path=/; Max-Age=$ACCESS_TOKEN_MAX_AGE; HttpOnly; SameSite=Lax${if (secure) "; Secure" else ""}"
        )

        // Refresh token cookie
        response.addHeader(
            "Set-Cookie",
            "${REFRESH_TOKEN_COOKIE}=$refreshToken; Path=/; Max-Age=$REFRESH_TOKEN_MAX_AGE; HttpOnly; SameSite=Lax${if (secure) "; Secure" else ""}"
        )
    }

    fun clearAuthCookies(response: HttpServletResponse, secure: Boolean = true) {
        response.addHeader(
            "Set-Cookie",
            "${ACCESS_TOKEN_COOKIE}=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax${if (secure) "; Secure" else ""}"
        )
        response.addHeader(
            "Set-Cookie",
            "${REFRESH_TOKEN_COOKIE}=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax${if (secure) "; Secure" else ""}"
        )
    }

    private fun getRedirectUriFromCookie(request: HttpServletRequest): String? {
        log.debug(
            "Looking for redirect_uri cookie. Available cookies: {}",
            request.cookies?.map { "${it.name}=${it.value}" }?.joinToString(", ") ?: "none"
        )

        val cookie = request.cookies?.find { it.name == REDIRECT_URI_COOKIE }
        val redirectUri = cookie?.value

        if (redirectUri.isNullOrBlank()) {
            log.debug("No redirect_uri cookie found, will use default")
            return null
        }

        log.debug("Found redirect_uri cookie: {}", redirectUri)

        // Validate the redirect URI
        return if (isValidRedirectUri(redirectUri)) {
            log.debug("Redirect URI is valid: {}", redirectUri)
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

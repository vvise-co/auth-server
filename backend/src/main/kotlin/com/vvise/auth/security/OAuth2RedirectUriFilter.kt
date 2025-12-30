package com.vvise.auth.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Filter that captures the redirect_uri query parameter from OAuth2 authorization requests
 * and stores it in a cookie for use after the OAuth flow completes.
 *
 * This allows client applications to specify where they want to be redirected after authentication:
 * /oauth2/authorization/google?redirect_uri=https://client-app.com/auth/callback
 */
@Component
class OAuth2RedirectUriFilter : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(OAuth2RedirectUriFilter::class.java)

    companion object {
        const val REDIRECT_URI_COOKIE = "oauth2_redirect_uri"
        const val REDIRECT_URI_PARAM = "redirect_uri"
        const val COOKIE_MAX_AGE = 10 * 60 // 10 minutes
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // Only process OAuth2 authorization requests
        if (request.requestURI.startsWith("/oauth2/authorization/")) {
            val redirectUri = request.getParameter(REDIRECT_URI_PARAM)
            log.info("OAuth2 authorization request: URI={}, redirect_uri={}", request.requestURI, redirectUri)

            if (!redirectUri.isNullOrBlank()) {
                // Check if request is behind HTTPS proxy
                val forwardedProto = request.getHeader("X-Forwarded-Proto")
                val host = request.getHeader("X-Forwarded-Host") ?: request.getHeader("Host") ?: ""
                val isSecure = request.isSecure || forwardedProto == "https" ||
                    host.contains("koyeb") || host.contains("railway")

                log.info("Setting redirect_uri cookie: value={}, secure={}, forwardedProto={}, host={}",
                    redirectUri, isSecure, forwardedProto, host)

                // Store redirect_uri in a cookie
                // IMPORTANT: Use SameSite=None for cross-site OAuth flow (Google/GitHub redirect back)
                // This requires Secure=true
                // URL-encode the value to handle special characters
                val encodedUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)

                if (isSecure) {
                    response.addHeader(
                        "Set-Cookie",
                        "$REDIRECT_URI_COOKIE=$encodedUri; Path=/; Max-Age=$COOKIE_MAX_AGE; HttpOnly; SameSite=None; Secure"
                    )
                } else {
                    // For local development (HTTP), use SameSite=Lax
                    val cookie = Cookie(REDIRECT_URI_COOKIE, encodedUri)
                    cookie.path = "/"
                    cookie.maxAge = COOKIE_MAX_AGE
                    cookie.isHttpOnly = true
                    response.addCookie(cookie)
                }
            }
        }

        filterChain.doFilter(request, response)
    }
}

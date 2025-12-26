package com.vvise.auth.security

import com.vvise.auth.service.RefreshTokenService
import com.vvise.auth.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class OAuth2AuthenticationSuccessHandler(
    private val tokenProvider: JwtTokenProvider,
    private val refreshTokenService: RefreshTokenService,
    private val userService: UserService
) : SimpleUrlAuthenticationSuccessHandler() {

    private val log = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler::class.java)

    @Value("\${app.oauth2.authorized-redirect-uri}")
    private lateinit var redirectUri: String

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val targetUrl = determineTargetUrl(authentication)

        if (response.isCommitted) {
            log.debug("Response has already been committed. Unable to redirect to $targetUrl")
            return
        }

        clearAuthenticationAttributes(request)
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }

    private fun determineTargetUrl(authentication: Authentication): String {
        val userPrincipal = authentication.principal as UserPrincipal

        val accessToken = tokenProvider.generateAccessToken(userPrincipal)

        // Get full user to create refresh token
        val user = userService.findById(userPrincipal.id)
            ?: throw RuntimeException("User not found")

        // Delete existing refresh tokens for this user
        refreshTokenService.deleteByUser(user)

        // Create new refresh token
        val refreshToken = refreshTokenService.createRefreshToken(user)

        return UriComponentsBuilder.fromUriString(redirectUri)
            .queryParam("token", accessToken)
            .queryParam("refreshToken", refreshToken.token)
            .build().toUriString()
    }
}

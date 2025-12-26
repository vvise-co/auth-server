package com.vvise.demo.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class OAuth2AuthenticationFailureHandler : SimpleUrlAuthenticationFailureHandler() {

    private val log = LoggerFactory.getLogger(OAuth2AuthenticationFailureHandler::class.java)

    @Value("\${app.oauth2.authorized-redirect-uri}")
    private lateinit var redirectUri: String

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        log.error("OAuth2 authentication failed: ${exception.message}")

        val targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
            .queryParam("error", exception.localizedMessage)
            .build().toUriString()

        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
}

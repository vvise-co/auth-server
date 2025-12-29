package com.vvise.auth.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RequestLoggingFilter : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(RequestLoggingFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val uri = request.requestURI
        val queryString = request.queryString

        // Log OAuth-related requests in detail
        if (uri.contains("oauth2") || uri.contains("login/oauth2") || uri.contains("auth/callback")) {
            log.info("=== Incoming Request ===")
            log.info("Method: ${request.method}")
            log.info("URI: $uri")
            log.info("Query: ${queryString ?: "(none)"}")
            log.info("Remote: ${request.remoteAddr}")
            log.info("X-Forwarded-Proto: ${request.getHeader("X-Forwarded-Proto")}")
            log.info("X-Forwarded-Host: ${request.getHeader("X-Forwarded-Host")}")
            log.info("Host: ${request.getHeader("Host")}")
            log.info("========================")
        }

        filterChain.doFilter(request, response)
    }
}

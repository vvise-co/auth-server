package com.vvise.template.security

import com.vvise.template.service.TokenIntrospectionService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Filter that validates JWT tokens via the auth server's introspection endpoint.
 * Tokens are cached to minimize auth server calls.
 *
 * No JWT_SECRET required - validation is done by the auth server.
 */
@Component
class JwtAuthenticationFilter(
    private val tokenIntrospectionService: TokenIntrospectionService
) : OncePerRequestFilter() {

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
        private const val ACCESS_TOKEN_COOKIE = "access_token"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val token = extractToken(request)

            if (token != null) {
                val user = tokenIntrospectionService.validateAndGetUser(token)

                if (user != null) {
                    val authentication = UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.authorities
                    ).apply {
                        details = WebAuthenticationDetailsSource().buildDetails(request)
                    }

                    SecurityContextHolder.getContext().authentication = authentication
                }
            }
        } catch (ex: Exception) {
            logger.error("Could not set user authentication in security context", ex)
        }

        filterChain.doFilter(request, response)
    }

    /**
     * Extract token from Authorization header or cookie.
     */
    private fun extractToken(request: HttpServletRequest): String? {
        // Try Authorization header first
        val authHeader = request.getHeader(AUTHORIZATION_HEADER)
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length)
        }

        // Fall back to cookie
        request.cookies?.forEach { cookie ->
            if (cookie.name == ACCESS_TOKEN_COOKIE) {
                return cookie.value
            }
        }

        return null
    }
}

package com.vvise.template.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Filter that validates JWT tokens from the Authorization header or cookies.
 * Tokens are validated against the shared JWT secret with the auth server.
 */
@Component
class JwtAuthenticationFilter(
    private val jwtTokenValidator: JwtTokenValidator
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
                val claims = jwtTokenValidator.validateToken(token)

                if (claims != null) {
                    val userId = jwtTokenValidator.getUserId(claims)
                    val email = jwtTokenValidator.getEmail(claims)
                    val name = jwtTokenValidator.getName(claims)
                    val roles = jwtTokenValidator.getRoles(claims)

                    if (userId != null && email != null) {
                        val user = AuthenticatedUser(
                            id = userId,
                            email = email,
                            name = name ?: email,
                            roles = roles
                        )

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

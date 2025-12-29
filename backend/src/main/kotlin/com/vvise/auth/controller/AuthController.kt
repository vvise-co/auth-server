package com.vvise.auth.controller

import com.vvise.auth.dto.AuthResponse
import com.vvise.auth.dto.RefreshTokenRequest
import com.vvise.auth.dto.TokenIntrospectionResponse
import com.vvise.auth.dto.UserDto
import com.vvise.auth.security.JwtTokenProvider
import com.vvise.auth.security.OAuth2AuthenticationSuccessHandler
import com.vvise.auth.security.UserPrincipal
import com.vvise.auth.service.RefreshTokenService
import com.vvise.auth.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val tokenProvider: JwtTokenProvider,
    private val refreshTokenService: RefreshTokenService,
    private val userService: UserService,
    private val oAuth2SuccessHandler: OAuth2AuthenticationSuccessHandler
) {

    @GetMapping("/me")
    fun getCurrentUser(@AuthenticationPrincipal userPrincipal: UserPrincipal?): ResponseEntity<UserDto> {
        if (userPrincipal == null) {
            return ResponseEntity.status(401).build()
        }

        val user = userService.findById(userPrincipal.id)
            ?: throw RuntimeException("User not found")

        return ResponseEntity.ok(UserDto.fromEntity(user))
    }

    @PostMapping("/refresh")
    fun refreshToken(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<AuthResponse> {
        val refreshTokenStr = request.refreshToken

        val refreshToken = refreshTokenService.findByToken(refreshTokenStr)
            ?: throw RuntimeException("Refresh token not found")

        val verifiedToken = refreshTokenService.verifyExpiration(refreshToken)
        val user = verifiedToken.user ?: throw RuntimeException("User not found")

        val userPrincipal = UserPrincipal.create(user)
        val accessToken = tokenProvider.generateAccessToken(userPrincipal)

        return ResponseEntity.ok(
            AuthResponse.of(
                accessToken = accessToken,
                refreshToken = refreshTokenStr,
                expiresIn = tokenProvider.getAccessTokenExpiration(),
                user = UserDto.fromEntity(user)
            )
        )
    }

    @PostMapping("/logout")
    fun logout(
        @AuthenticationPrincipal userPrincipal: UserPrincipal?,
        @RequestBody(required = false) request: RefreshTokenRequest?,
        httpRequest: HttpServletRequest,
        httpResponse: HttpServletResponse
    ): ResponseEntity<Map<String, String>> {
        // Invalidate refresh tokens
        if (request?.refreshToken != null) {
            refreshTokenService.deleteByToken(request.refreshToken)
        } else if (userPrincipal != null) {
            val user = userService.findById(userPrincipal.id)
            if (user != null) {
                refreshTokenService.deleteByUser(user)
            }
        }

        // Clear auth cookies
        val isSecure = isSecureRequest(httpRequest)
        oAuth2SuccessHandler.clearAuthCookies(httpResponse, isSecure)

        return ResponseEntity.ok(mapOf("message" to "Logged out successfully"))
    }

    private fun isSecureRequest(request: HttpServletRequest): Boolean {
        val forwardedProto = request.getHeader("X-Forwarded-Proto")
        if (forwardedProto == "https") return true
        if (request.isSecure) return true
        val host = request.getHeader("X-Forwarded-Host") ?: request.getHeader("Host") ?: ""
        return host.contains("koyeb") || host.contains("railway")
    }

    @GetMapping("/providers")
    fun getProviders(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(
            mapOf(
                "google" to "/oauth2/authorization/google",
                "github" to "/oauth2/authorization/github",
                "microsoft" to "/oauth2/authorization/microsoft"
            )
        )
    }

    @GetMapping("/debug/config")
    fun debugConfig(): ResponseEntity<Map<String, Any?>> {
        return ResponseEntity.ok(
            mapOf(
                "oauth2_base_url" to System.getenv("OAUTH2_BASE_URL"),
                "oauth2_redirect_uri" to System.getenv("OAUTH2_REDIRECT_URI"),
                "cors_allowed_origins" to System.getenv("CORS_ALLOWED_ORIGINS"),
                "node_env" to System.getenv("NODE_ENV"),
                "server_port" to System.getenv("SERVER_PORT")
            )
        )
    }

    /**
     * Token introspection endpoint (RFC 7662).
     * Client applications can use this to validate tokens without needing the JWT secret.
     *
     * Usage: POST /api/auth/introspect
     * Body: { "token": "access_token_here" }
     *
     * Returns user info if token is valid, or { "active": false } if invalid.
     */
    @PostMapping("/introspect")
    fun introspectToken(@RequestBody request: Map<String, String>): ResponseEntity<TokenIntrospectionResponse> {
        val token = request["token"]
            ?: return ResponseEntity.ok(TokenIntrospectionResponse.inactive())

        if (!tokenProvider.validateToken(token)) {
            return ResponseEntity.ok(TokenIntrospectionResponse.inactive())
        }

        return try {
            val claims = tokenProvider.getClaimsFromToken(token)
            val userId = claims.subject.toLongOrNull()

            // Optionally fetch fresh user data from database
            val user = userId?.let { userService.findById(it) }

            ResponseEntity.ok(
                TokenIntrospectionResponse(
                    active = true,
                    sub = claims.subject,
                    email = claims["email"] as? String,
                    name = claims["name"] as? String,
                    roles = claims["roles"] as? String,
                    exp = claims.expiration?.time?.div(1000),
                    iat = claims.issuedAt?.time?.div(1000),
                    imageUrl = user?.imageUrl
                )
            )
        } catch (e: Exception) {
            ResponseEntity.ok(TokenIntrospectionResponse.inactive())
        }
    }

    /**
     * GET version of introspect for simpler client usage.
     * Token is passed via Authorization header.
     */
    @GetMapping("/introspect")
    fun introspectTokenGet(
        @RequestHeader("Authorization") authHeader: String?
    ): ResponseEntity<TokenIntrospectionResponse> {
        val token = authHeader?.removePrefix("Bearer ")?.trim()
            ?: return ResponseEntity.ok(TokenIntrospectionResponse.inactive())

        return introspectToken(mapOf("token" to token))
    }
}

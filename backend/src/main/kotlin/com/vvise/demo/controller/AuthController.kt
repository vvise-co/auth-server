package com.vvise.demo.controller

import com.vvise.demo.dto.AuthResponse
import com.vvise.demo.dto.RefreshTokenRequest
import com.vvise.demo.dto.UserDto
import com.vvise.demo.security.JwtTokenProvider
import com.vvise.demo.security.UserPrincipal
import com.vvise.demo.service.RefreshTokenService
import com.vvise.demo.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val tokenProvider: JwtTokenProvider,
    private val refreshTokenService: RefreshTokenService,
    private val userService: UserService
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
        @RequestBody(required = false) request: RefreshTokenRequest?
    ): ResponseEntity<Map<String, String>> {
        if (request?.refreshToken != null) {
            refreshTokenService.deleteByToken(request.refreshToken)
        } else if (userPrincipal != null) {
            val user = userService.findById(userPrincipal.id)
            if (user != null) {
                refreshTokenService.deleteByUser(user)
            }
        }

        return ResponseEntity.ok(mapOf("message" to "Logged out successfully"))
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
}

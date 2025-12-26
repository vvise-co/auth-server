package com.vvise.demo.dto

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val user: UserDto
) {
    companion object {
        fun of(accessToken: String, refreshToken: String, expiresIn: Long, user: UserDto): AuthResponse =
            AuthResponse(
                accessToken = accessToken,
                refreshToken = refreshToken,
                tokenType = "Bearer",
                expiresIn = expiresIn,
                user = user
            )
    }
}

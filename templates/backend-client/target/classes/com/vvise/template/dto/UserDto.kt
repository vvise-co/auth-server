package com.vvise.template.dto

/**
 * Data Transfer Object for user data from the auth server.
 * Matches the UserDto from the auth server's API.
 */
data class UserDto(
    val id: Long,
    val email: String,
    val name: String,
    val avatarUrl: String?,
    val roles: Set<String>,
    val provider: String,
    val createdAt: String?,
    val updatedAt: String?
)

/**
 * Minimal user info for embedding in other DTOs.
 */
data class UserSummary(
    val id: Long,
    val name: String,
    val email: String,
    val avatarUrl: String?
)

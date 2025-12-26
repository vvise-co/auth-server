package com.vvise.auth.dto

import com.vvise.auth.entity.User
import java.time.LocalDateTime

data class UserDto(
    val id: Long?,
    val email: String,
    val name: String,
    val imageUrl: String?,
    val provider: String,
    val roles: Set<String>,
    val createdAt: LocalDateTime?
) {
    companion object {
        fun fromEntity(user: User): UserDto = UserDto(
            id = user.id,
            email = user.email,
            name = user.name,
            imageUrl = user.imageUrl,
            provider = user.provider.name,
            roles = user.roles.map { it.name.replace("ROLE_", "") }.toSet(),
            createdAt = user.createdAt
        )
    }
}

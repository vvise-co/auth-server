package com.vvise.auth.service

import com.vvise.auth.dto.UserDto
import com.vvise.auth.entity.Role
import com.vvise.auth.entity.User
import com.vvise.auth.repository.RoleRepository
import com.vvise.auth.repository.UserRepository
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository
) {
    private val log = LoggerFactory.getLogger(UserService::class.java)

    @PostConstruct
    fun initRoles() {
        if (!roleRepository.existsByName(Role.ROLE_USER)) {
            roleRepository.save(Role(name = Role.ROLE_USER))
            log.info("Created ROLE_USER")
        }
        if (!roleRepository.existsByName(Role.ROLE_ADMIN)) {
            roleRepository.save(Role(name = Role.ROLE_ADMIN))
            log.info("Created ROLE_ADMIN")
        }
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): User? = userRepository.findById(id).orElse(null)

    @Transactional(readOnly = true)
    fun findByEmail(email: String): User? = userRepository.findByEmail(email)

    @Transactional(readOnly = true)
    fun findByProviderAndProviderId(provider: User.AuthProvider, providerId: String): User? =
        userRepository.findByProviderAndProviderId(provider, providerId)

    @Transactional
    fun createOrUpdateOAuth2User(
        email: String,
        name: String,
        imageUrl: String?,
        provider: User.AuthProvider,
        providerId: String
    ): User {
        val existingUser = findByProviderAndProviderId(provider, providerId)

        if (existingUser != null) {
            existingUser.name = name
            existingUser.imageUrl = imageUrl
            existingUser.email = email
            return userRepository.save(existingUser)
        }

        val userRole = roleRepository.findByName(Role.ROLE_USER)
            ?: throw RuntimeException("Default role not found")

        val newUser = User(
            email = email,
            name = name,
            imageUrl = imageUrl,
            provider = provider,
            providerId = providerId,
            roles = mutableSetOf(userRole)
        )

        return userRepository.save(newUser)
    }

    @Transactional
    fun addAdminRole(userId: Long): User {
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("User not found") }

        val adminRole = roleRepository.findByName(Role.ROLE_ADMIN)
            ?: throw RuntimeException("Admin role not found")

        user.roles.add(adminRole)
        return userRepository.save(user)
    }

    @Transactional
    fun removeAdminRole(userId: Long): User {
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("User not found") }

        user.roles.removeIf { it.name == Role.ROLE_ADMIN }
        return userRepository.save(user)
    }

    fun toDto(user: User): UserDto = UserDto.fromEntity(user)
}

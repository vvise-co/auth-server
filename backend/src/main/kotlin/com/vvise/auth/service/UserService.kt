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
    fun findAll(): List<User> = userRepository.findAll()

    @Transactional(readOnly = true)
    fun findByEmail(email: String): User? = userRepository.findByEmail(email)

    @Transactional(readOnly = true)
    fun findByProviderAndProviderId(provider: User.AuthProvider, providerId: String): User? =
        userRepository.findByProviderAndProviderId(provider, providerId)

    /**
     * Creates or updates a user with OIDC standard claims from OAuth2/OIDC provider.
     */
    @Transactional
    fun createOrUpdateOAuth2User(
        email: String,
        name: String,
        provider: User.AuthProvider,
        providerId: String,
        givenName: String? = null,
        familyName: String? = null,
        middleName: String? = null,
        nickname: String? = null,
        preferredUsername: String? = null,
        profile: String? = null,
        picture: String? = null,
        website: String? = null,
        emailVerified: Boolean = false,
        gender: String? = null,
        birthdate: java.time.LocalDate? = null,
        zoneinfo: String? = null,
        locale: String? = null,
        phoneNumber: String? = null,
        phoneNumberVerified: Boolean = false
    ): User {
        val existingUser = findByProviderAndProviderId(provider, providerId)

        if (existingUser != null) {
            // Update existing user with new data from provider
            existingUser.name = name
            existingUser.email = email
            existingUser.givenName = givenName ?: existingUser.givenName
            existingUser.familyName = familyName ?: existingUser.familyName
            existingUser.middleName = middleName ?: existingUser.middleName
            existingUser.nickname = nickname ?: existingUser.nickname
            existingUser.preferredUsername = preferredUsername ?: existingUser.preferredUsername
            existingUser.profile = profile ?: existingUser.profile
            existingUser.picture = picture ?: existingUser.picture
            existingUser.website = website ?: existingUser.website
            existingUser.emailVerified = emailVerified
            existingUser.gender = gender ?: existingUser.gender
            existingUser.birthdate = birthdate ?: existingUser.birthdate
            existingUser.zoneinfo = zoneinfo ?: existingUser.zoneinfo
            existingUser.locale = locale ?: existingUser.locale
            existingUser.phoneNumber = phoneNumber ?: existingUser.phoneNumber
            existingUser.phoneNumberVerified = phoneNumberVerified
            return userRepository.save(existingUser)
        }

        val userRole = roleRepository.findByName(Role.ROLE_USER)
            ?: throw RuntimeException("Default role not found")

        val newUser = User(
            email = email,
            name = name,
            provider = provider,
            providerId = providerId,
            givenName = givenName,
            familyName = familyName,
            middleName = middleName,
            nickname = nickname,
            preferredUsername = preferredUsername,
            profile = profile,
            picture = picture,
            website = website,
            emailVerified = emailVerified,
            gender = gender,
            birthdate = birthdate,
            zoneinfo = zoneinfo,
            locale = locale,
            phoneNumber = phoneNumber,
            phoneNumberVerified = phoneNumberVerified,
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

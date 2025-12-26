package com.vvise.auth.repository

import com.vvise.auth.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun findByProviderAndProviderId(provider: User.AuthProvider, providerId: String): User?
    fun existsByEmail(email: String): Boolean
}

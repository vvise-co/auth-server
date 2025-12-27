package com.vvise.template.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * Represents an authenticated user from the central auth server.
 * This is populated from the token introspection response.
 */
data class AuthenticatedUser(
    val id: Long,
    val email: String,
    val name: String,
    val roles: List<String>,
    val imageUrl: String? = null
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return roles.map { SimpleGrantedAuthority("ROLE_$it") }
    }

    override fun getPassword(): String? = null

    override fun getUsername(): String = email

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

    /**
     * Check if user has a specific role.
     */
    fun hasRole(role: String): Boolean {
        return roles.contains(role.uppercase())
    }

    /**
     * Check if user is an admin.
     */
    fun isAdmin(): Boolean = hasRole("ADMIN")
}

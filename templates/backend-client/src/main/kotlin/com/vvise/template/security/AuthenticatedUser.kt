package com.vvise.template.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * Represents an authenticated user from the central auth server.
 * This is populated from the token introspection response.
 * Uses OIDC-compliant field names.
 */
data class AuthenticatedUser(
    // OIDC: sub - Subject Identifier
    val sub: String,
    // OIDC: email
    val email: String,
    // OIDC: name - Full name
    val name: String,
    // Application-specific roles
    val roles: List<String>,
    // OIDC: picture - URL of the user's profile picture
    val picture: String? = null,
    // OIDC: email_verified
    val emailVerified: Boolean? = null,
    // OIDC: given_name
    val givenName: String? = null,
    // OIDC: family_name
    val familyName: String? = null
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

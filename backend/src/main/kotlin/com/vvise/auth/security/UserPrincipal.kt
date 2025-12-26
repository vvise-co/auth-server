package com.vvise.auth.security

import com.vvise.auth.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.core.oidc.user.OidcUser

class UserPrincipal(
    val id: Long,
    private val email: String,
    private val name: String,
    val imageUrl: String?,
    private val authorities: Collection<GrantedAuthority>,
    private var attributes: Map<String, Any>? = null,
    private val idToken: OidcIdToken? = null,
    private val userInfo: OidcUserInfo? = null
) : OidcUser, UserDetails {

    override fun getEmail(): String = email

    override fun getName(): String = name

    override fun getPassword(): String? = null // OAuth2 only, no password

    override fun getUsername(): String = email

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

    override fun getAuthorities(): Collection<GrantedAuthority> = authorities

    override fun getAttributes(): Map<String, Any> = attributes ?: emptyMap()

    fun setAttributes(attributes: Map<String, Any>) {
        this.attributes = attributes
    }

    override fun getClaims(): Map<String, Any> {
        return idToken?.claims ?: attributes ?: emptyMap()
    }

    override fun getUserInfo(): OidcUserInfo? = userInfo

    override fun getIdToken(): OidcIdToken? = idToken

    companion object {
        fun create(user: User): UserPrincipal {
            val authorities = user.roles.map { role ->
                SimpleGrantedAuthority(role.name)
            }

            return UserPrincipal(
                id = user.id!!,
                email = user.email,
                name = user.name,
                imageUrl = user.imageUrl,
                authorities = authorities
            )
        }

        fun create(user: User, attributes: Map<String, Any>): UserPrincipal {
            val userPrincipal = create(user)
            userPrincipal.attributes = attributes
            return userPrincipal
        }

        fun createOidc(
            user: User,
            attributes: Map<String, Any>,
            idToken: OidcIdToken,
            userInfo: OidcUserInfo?
        ): UserPrincipal {
            val authorities = user.roles.map { role ->
                SimpleGrantedAuthority(role.name)
            }

            return UserPrincipal(
                id = user.id!!,
                email = user.email,
                name = user.name,
                imageUrl = user.imageUrl,
                authorities = authorities,
                attributes = attributes,
                idToken = idToken,
                userInfo = userInfo
            )
        }
    }
}

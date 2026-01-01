package com.vvise.auth.security

import com.vvise.auth.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import java.time.LocalDate

/**
 * UserPrincipal implementing both UserDetails and OidcUser with full OIDC standard claims support.
 */
class UserPrincipal(
    val id: Long,
    private val _sub: String,
    private val _email: String,
    private val _emailVerified: Boolean,
    private val _name: String,
    private val _givenName: String?,
    private val _familyName: String?,
    private val _middleName: String?,
    private val _nickname: String?,
    private val _preferredUsername: String?,
    private val _profile: String?,
    private val _picture: String?,
    private val _website: String?,
    private val _gender: String?,
    private val _birthdate: LocalDate?,
    private val _zoneinfo: String?,
    private val _locale: String?,
    private val _phoneNumber: String?,
    private val _phoneNumberVerified: Boolean,
    private val _updatedAt: Long?,
    private val _authorities: Collection<GrantedAuthority>,
    private var attributes: Map<String, Any>? = null,
    private val idToken: OidcIdToken? = null,
    private val userInfo: OidcUserInfo? = null
) : OidcUser, UserDetails {

    // OIDC Standard Claims
    override fun getSubject(): String = _sub
    override fun getEmail(): String = _email
    override fun getEmailVerified(): Boolean = _emailVerified
    override fun getName(): String = _name
    override fun getGivenName(): String? = _givenName
    override fun getFamilyName(): String? = _familyName
    override fun getMiddleName(): String? = _middleName
    override fun getNickName(): String? = _nickname
    override fun getPreferredUsername(): String? = _preferredUsername
    override fun getProfile(): String? = _profile
    override fun getPicture(): String? = _picture
    override fun getWebsite(): String? = _website
    override fun getGender(): String? = _gender
    override fun getBirthdate(): String? = _birthdate?.toString()
    override fun getZoneInfo(): String? = _zoneinfo
    override fun getLocale(): String? = _locale
    override fun getPhoneNumber(): String? = _phoneNumber
    override fun getPhoneNumberVerified(): Boolean = _phoneNumberVerified
    override fun getUpdatedAt(): java.time.Instant? = _updatedAt?.let { java.time.Instant.ofEpochSecond(it) }

    // UserDetails implementation
    override fun getPassword(): String? = null // OAuth2 only, no password
    override fun getUsername(): String = _email
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
    override fun getAuthorities(): Collection<GrantedAuthority> = _authorities

    // OidcUser implementation
    override fun getAttributes(): Map<String, Any> = attributes ?: emptyMap()

    fun setAttributes(attributes: Map<String, Any>) {
        this.attributes = attributes
    }

    override fun getClaims(): Map<String, Any> {
        return idToken?.claims ?: buildClaimsMap()
    }

    override fun getUserInfo(): OidcUserInfo? = userInfo

    override fun getIdToken(): OidcIdToken? = idToken

    /**
     * Builds a claims map from the user principal's OIDC fields.
     */
    private fun buildClaimsMap(): Map<String, Any> {
        val claims = mutableMapOf<String, Any>()
        claims["sub"] = _sub
        claims["name"] = _name
        claims["email"] = _email
        claims["email_verified"] = _emailVerified
        _givenName?.let { claims["given_name"] = it }
        _familyName?.let { claims["family_name"] = it }
        _middleName?.let { claims["middle_name"] = it }
        _nickname?.let { claims["nickname"] = it }
        _preferredUsername?.let { claims["preferred_username"] = it }
        _profile?.let { claims["profile"] = it }
        _picture?.let { claims["picture"] = it }
        _website?.let { claims["website"] = it }
        _gender?.let { claims["gender"] = it }
        _birthdate?.let { claims["birthdate"] = it.toString() }
        _zoneinfo?.let { claims["zoneinfo"] = it }
        _locale?.let { claims["locale"] = it }
        _phoneNumber?.let { claims["phone_number"] = it }
        claims["phone_number_verified"] = _phoneNumberVerified
        _updatedAt?.let { claims["updated_at"] = it }
        return claims
    }

    companion object {
        fun create(user: User): UserPrincipal {
            val authorities = user.roles.map { role ->
                SimpleGrantedAuthority(role.name)
            }

            return UserPrincipal(
                id = user.id!!,
                _sub = user.getSub(),
                _email = user.email,
                _emailVerified = user.emailVerified,
                _name = user.name,
                _givenName = user.givenName,
                _familyName = user.familyName,
                _middleName = user.middleName,
                _nickname = user.nickname,
                _preferredUsername = user.preferredUsername,
                _profile = user.profile,
                _picture = user.picture,
                _website = user.website,
                _gender = user.gender,
                _birthdate = user.birthdate,
                _zoneinfo = user.zoneinfo,
                _locale = user.locale,
                _phoneNumber = user.phoneNumber,
                _phoneNumberVerified = user.phoneNumberVerified,
                _updatedAt = user.getUpdatedAtEpoch(),
                _authorities = authorities
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
                _sub = user.getSub(),
                _email = user.email,
                _emailVerified = user.emailVerified,
                _name = user.name,
                _givenName = user.givenName,
                _familyName = user.familyName,
                _middleName = user.middleName,
                _nickname = user.nickname,
                _preferredUsername = user.preferredUsername,
                _profile = user.profile,
                _picture = user.picture,
                _website = user.website,
                _gender = user.gender,
                _birthdate = user.birthdate,
                _zoneinfo = user.zoneinfo,
                _locale = user.locale,
                _phoneNumber = user.phoneNumber,
                _phoneNumberVerified = user.phoneNumberVerified,
                _updatedAt = user.getUpdatedAtEpoch(),
                _authorities = authorities,
                attributes = attributes,
                idToken = idToken,
                userInfo = userInfo
            )
        }
    }
}

package com.vvise.auth.security

/**
 * OAuth2/OIDC user info extraction from provider attributes.
 * Extracts standard OIDC claims where available from each provider.
 */
sealed class OAuth2UserInfo(
    val attributes: Map<String, Any>
) {
    // Required fields
    abstract val id: String
    abstract val name: String
    abstract val email: String?

    // OIDC standard claims (optional)
    open val givenName: String? get() = attributes["given_name"] as? String
    open val familyName: String? get() = attributes["family_name"] as? String
    open val middleName: String? get() = attributes["middle_name"] as? String
    open val nickname: String? get() = attributes["nickname"] as? String
    open val preferredUsername: String? get() = attributes["preferred_username"] as? String
    open val profile: String? get() = attributes["profile"] as? String
    open val picture: String? get() = attributes["picture"] as? String
    open val website: String? get() = attributes["website"] as? String
    open val emailVerified: Boolean get() = attributes["email_verified"] as? Boolean ?: false
    open val gender: String? get() = attributes["gender"] as? String
    open val birthdate: String? get() = attributes["birthdate"] as? String
    open val zoneinfo: String? get() = attributes["zoneinfo"] as? String
    open val locale: String? get() = (attributes["locale"] as? String)
    open val phoneNumber: String? get() = attributes["phone_number"] as? String
    open val phoneNumberVerified: Boolean get() = attributes["phone_number_verified"] as? Boolean ?: false

    class GoogleOAuth2UserInfo(attributes: Map<String, Any>) : OAuth2UserInfo(attributes) {
        override val id: String get() = attributes["sub"] as String
        override val name: String get() = attributes["name"] as String
        override val email: String? get() = attributes["email"] as? String
        override val emailVerified: Boolean get() = attributes["email_verified"] as? Boolean ?: false
        // Google provides given_name, family_name, picture, locale directly in standard format
    }

    class GithubOAuth2UserInfo(attributes: Map<String, Any>) : OAuth2UserInfo(attributes) {
        override val id: String get() = (attributes["id"] as Int).toString()
        override val name: String get() {
            val n = attributes["name"] as? String
            return if (n.isNullOrEmpty()) attributes["login"] as String else n
        }
        override val email: String? get() = attributes["email"] as? String
        override val picture: String? get() = attributes["avatar_url"] as? String
        override val preferredUsername: String? get() = attributes["login"] as? String
        override val profile: String? get() = attributes["html_url"] as? String
        override val website: String? get() = attributes["blog"] as? String
        // GitHub doesn't provide email_verified, assume verified if email is present
        override val emailVerified: Boolean get() = email != null
    }

    class MicrosoftOAuth2UserInfo(attributes: Map<String, Any>) : OAuth2UserInfo(attributes) {
        override val id: String get() = attributes["sub"] as String
        override val name: String get() = attributes["name"] as String
        override val email: String? get() = attributes["email"] as? String
        override val emailVerified: Boolean get() = attributes["email_verified"] as? Boolean ?: false
        override val picture: String? get() = null // Microsoft doesn't provide image URL in OIDC userinfo
        // Microsoft provides given_name, family_name in standard format
    }
}

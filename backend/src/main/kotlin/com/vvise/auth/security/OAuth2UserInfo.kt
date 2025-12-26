package com.vvise.auth.security

sealed class OAuth2UserInfo(
    val attributes: Map<String, Any>
) {
    abstract val id: String
    abstract val name: String
    abstract val email: String?
    abstract val imageUrl: String?

    class GoogleOAuth2UserInfo(attributes: Map<String, Any>) : OAuth2UserInfo(attributes) {
        override val id: String get() = attributes["sub"] as String
        override val name: String get() = attributes["name"] as String
        override val email: String? get() = attributes["email"] as? String
        override val imageUrl: String? get() = attributes["picture"] as? String
    }

    class GithubOAuth2UserInfo(attributes: Map<String, Any>) : OAuth2UserInfo(attributes) {
        override val id: String get() = (attributes["id"] as Int).toString()
        override val name: String get() {
            val n = attributes["name"] as? String
            return if (n.isNullOrEmpty()) attributes["login"] as String else n
        }
        override val email: String? get() = attributes["email"] as? String
        override val imageUrl: String? get() = attributes["avatar_url"] as? String
    }

    class MicrosoftOAuth2UserInfo(attributes: Map<String, Any>) : OAuth2UserInfo(attributes) {
        override val id: String get() = attributes["sub"] as String
        override val name: String get() = attributes["name"] as String
        override val email: String? get() = attributes["email"] as? String
        override val imageUrl: String? get() = null // Microsoft doesn't provide image URL in OIDC userinfo
    }
}

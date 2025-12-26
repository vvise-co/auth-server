package com.vvise.auth.security

import com.vvise.auth.entity.User

object OAuth2UserInfoFactory {

    fun getOAuth2UserInfo(registrationId: String, attributes: Map<String, Any>): OAuth2UserInfo {
        return when {
            registrationId.equals(User.AuthProvider.GOOGLE.name, ignoreCase = true) ->
                OAuth2UserInfo.GoogleOAuth2UserInfo(attributes)
            registrationId.equals(User.AuthProvider.GITHUB.name, ignoreCase = true) ->
                OAuth2UserInfo.GithubOAuth2UserInfo(attributes)
            registrationId.equals(User.AuthProvider.MICROSOFT.name, ignoreCase = true) ->
                OAuth2UserInfo.MicrosoftOAuth2UserInfo(attributes)
            else -> throw RuntimeException("Login with $registrationId is not supported.")
        }
    }

    fun getAuthProvider(registrationId: String): User.AuthProvider =
        User.AuthProvider.valueOf(registrationId.uppercase())
}

package com.vvise.auth.security

import com.vvise.auth.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service

@Service
class CustomOidcUserService(
    private val userService: UserService
) : OidcUserService() {

    private val log = LoggerFactory.getLogger(CustomOidcUserService::class.java)

    override fun loadUser(userRequest: OidcUserRequest): OidcUser {
        val oidcUser = super.loadUser(userRequest)

        return try {
            processOidcUser(userRequest, oidcUser)
        } catch (ex: Exception) {
            log.error("OIDC authentication error", ex)
            throw OAuth2AuthenticationException(ex.message)
        }
    }

    private fun processOidcUser(userRequest: OidcUserRequest, oidcUser: OidcUser): OidcUser {
        val registrationId = userRequest.clientRegistration.registrationId
        val oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
            registrationId,
            oidcUser.attributes
        )

        var email = oAuth2UserInfo.email
        if (email.isNullOrBlank()) {
            // Try to get email from OIDC claims
            email = oidcUser.email
        }

        if (email.isNullOrBlank()) {
            throw RuntimeException("Email not found from OAuth2 provider")
        }

        val provider = OAuth2UserInfoFactory.getAuthProvider(registrationId)

        val user = userService.createOrUpdateOAuth2User(
            email = email,
            name = oAuth2UserInfo.name,
            imageUrl = oAuth2UserInfo.imageUrl,
            provider = provider,
            providerId = oAuth2UserInfo.id
        )

        return UserPrincipal.createOidc(user, oidcUser.attributes, oidcUser.idToken, oidcUser.userInfo)
    }
}

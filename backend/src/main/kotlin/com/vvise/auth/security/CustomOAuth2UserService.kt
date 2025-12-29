package com.vvise.auth.security

import com.vvise.auth.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class CustomOAuth2UserService(
    private val userService: UserService
) : DefaultOAuth2UserService() {

    private val log = LoggerFactory.getLogger(CustomOAuth2UserService::class.java)

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        log.info("=== Loading OAuth2 User ===")
        log.info("Provider: ${userRequest.clientRegistration.registrationId}")

        val oAuth2User = super.loadUser(userRequest)
        log.info("OAuth2 user loaded from provider: ${oAuth2User.name}")

        return try {
            processOAuth2User(userRequest, oAuth2User)
        } catch (ex: Exception) {
            log.error("OAuth2 authentication error", ex)
            throw InternalAuthenticationServiceException(ex.message, ex.cause)
        }
    }

    private fun processOAuth2User(userRequest: OAuth2UserRequest, oAuth2User: OAuth2User): OAuth2User {
        val registrationId = userRequest.clientRegistration.registrationId
        val oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
            registrationId,
            oAuth2User.attributes
        )

        val email = oAuth2UserInfo.email
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

        return UserPrincipal.create(user, oAuth2User.attributes)
    }
}

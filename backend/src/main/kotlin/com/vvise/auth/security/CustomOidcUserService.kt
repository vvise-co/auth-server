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

        // Parse birthdate from OIDC user or attributes
        val birthdate = oidcUser.birthdate?.let {
            try {
                java.time.LocalDate.parse(it)
            } catch (e: Exception) {
                null
            }
        } ?: oAuth2UserInfo.birthdate?.let {
            try {
                java.time.LocalDate.parse(it)
            } catch (e: Exception) {
                null
            }
        }

        // Prefer OIDC standard claims from oidcUser, fall back to attributes
        val user = userService.createOrUpdateOAuth2User(
            email = email,
            name = oAuth2UserInfo.name,
            provider = provider,
            providerId = oAuth2UserInfo.id,
            givenName = oidcUser.givenName ?: oAuth2UserInfo.givenName,
            familyName = oidcUser.familyName ?: oAuth2UserInfo.familyName,
            middleName = oidcUser.middleName ?: oAuth2UserInfo.middleName,
            nickname = oidcUser.nickName ?: oAuth2UserInfo.nickname,
            preferredUsername = oidcUser.preferredUsername ?: oAuth2UserInfo.preferredUsername,
            profile = oidcUser.profile ?: oAuth2UserInfo.profile,
            picture = oidcUser.picture ?: oAuth2UserInfo.picture,
            website = oidcUser.website ?: oAuth2UserInfo.website,
            emailVerified = oidcUser.emailVerified ?: oAuth2UserInfo.emailVerified,
            gender = oidcUser.gender ?: oAuth2UserInfo.gender,
            birthdate = birthdate,
            zoneinfo = oidcUser.zoneInfo ?: oAuth2UserInfo.zoneinfo,
            locale = oidcUser.locale ?: oAuth2UserInfo.locale,
            phoneNumber = oidcUser.phoneNumber ?: oAuth2UserInfo.phoneNumber,
            phoneNumberVerified = oidcUser.phoneNumberVerified ?: oAuth2UserInfo.phoneNumberVerified
        )

        return UserPrincipal.createOidc(user, oidcUser.attributes, oidcUser.idToken, oidcUser.userInfo)
    }
}

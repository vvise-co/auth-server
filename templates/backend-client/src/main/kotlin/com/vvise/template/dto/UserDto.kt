package com.vvise.template.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Data Transfer Object for user data from the auth server.
 * Compliant with OpenID Connect Core 1.0 Standard Claims.
 * @see <a href="https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims">OIDC Standard Claims</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class UserDto(
    // OIDC: sub - Subject Identifier
    val sub: String,

    // OIDC: name - Full name
    val name: String,

    // OIDC: given_name
    @JsonProperty("given_name")
    val givenName: String? = null,

    // OIDC: family_name
    @JsonProperty("family_name")
    val familyName: String? = null,

    // OIDC: middle_name
    @JsonProperty("middle_name")
    val middleName: String? = null,

    // OIDC: nickname
    val nickname: String? = null,

    // OIDC: preferred_username
    @JsonProperty("preferred_username")
    val preferredUsername: String? = null,

    // OIDC: profile
    val profile: String? = null,

    // OIDC: picture
    val picture: String? = null,

    // OIDC: website
    val website: String? = null,

    // OIDC: email
    val email: String,

    // OIDC: email_verified
    @JsonProperty("email_verified")
    val emailVerified: Boolean = false,

    // OIDC: gender
    val gender: String? = null,

    // OIDC: birthdate
    val birthdate: String? = null,

    // OIDC: zoneinfo
    val zoneinfo: String? = null,

    // OIDC: locale
    val locale: String? = null,

    // OIDC: phone_number
    @JsonProperty("phone_number")
    val phoneNumber: String? = null,

    // OIDC: phone_number_verified
    @JsonProperty("phone_number_verified")
    val phoneNumberVerified: Boolean = false,

    // OIDC: address
    val address: AddressDto? = null,

    // OIDC: updated_at (Unix timestamp)
    @JsonProperty("updated_at")
    val updatedAt: Long? = null,

    // Application-specific fields
    val roles: Set<String>? = null,
    val provider: String? = null
)

/**
 * OIDC Address Claim structure.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class AddressDto(
    val formatted: String? = null,
    @JsonProperty("street_address")
    val streetAddress: String? = null,
    val locality: String? = null,
    val region: String? = null,
    @JsonProperty("postal_code")
    val postalCode: String? = null,
    val country: String? = null
)

/**
 * Minimal user info for embedding in other DTOs.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class UserSummary(
    val sub: String,
    val name: String,
    val email: String,
    val picture: String? = null
)

package com.vvise.auth.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.vvise.auth.entity.User

/**
 * User DTO compliant with OpenID Connect Core 1.0 Standard Claims.
 * Uses OIDC claim names (snake_case) for JSON serialization.
 * @see <a href="https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims">OIDC Standard Claims</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserDto(
    // OIDC: sub - Subject Identifier (unique identifier for the user)
    val sub: String,

    // OIDC: name - Full name
    val name: String,

    // OIDC: given_name - Given name(s) or first name(s)
    @JsonProperty("given_name")
    val givenName: String? = null,

    // OIDC: family_name - Surname(s) or last name(s)
    @JsonProperty("family_name")
    val familyName: String? = null,

    // OIDC: middle_name - Middle name(s)
    @JsonProperty("middle_name")
    val middleName: String? = null,

    // OIDC: nickname - Casual name
    val nickname: String? = null,

    // OIDC: preferred_username - Shorthand name
    @JsonProperty("preferred_username")
    val preferredUsername: String? = null,

    // OIDC: profile - URL of the user's profile page
    val profile: String? = null,

    // OIDC: picture - URL of the user's profile picture
    val picture: String? = null,

    // OIDC: website - URL of the user's web page or blog
    val website: String? = null,

    // OIDC: email - Preferred e-mail address
    val email: String,

    // OIDC: email_verified - True if the email has been verified
    @JsonProperty("email_verified")
    val emailVerified: Boolean = false,

    // OIDC: gender - Gender
    val gender: String? = null,

    // OIDC: birthdate - Birthday (YYYY-MM-DD format as string per OIDC spec)
    val birthdate: String? = null,

    // OIDC: zoneinfo - Time zone
    val zoneinfo: String? = null,

    // OIDC: locale - Locale
    val locale: String? = null,

    // OIDC: phone_number - Preferred telephone number
    @JsonProperty("phone_number")
    val phoneNumber: String? = null,

    // OIDC: phone_number_verified - True if the phone number has been verified
    @JsonProperty("phone_number_verified")
    val phoneNumberVerified: Boolean = false,

    // OIDC: address - Preferred postal address (as structured JSON object)
    val address: AddressDto? = null,

    // OIDC: updated_at - Time the user's information was last updated (Unix timestamp)
    @JsonProperty("updated_at")
    val updatedAt: Long? = null,

    // Application-specific (non-OIDC) fields
    val roles: Set<String>? = null,
    val provider: String? = null
) {
    companion object {
        fun fromEntity(user: User): UserDto = UserDto(
            sub = user.getSub(),
            name = user.name,
            givenName = user.givenName,
            familyName = user.familyName,
            middleName = user.middleName,
            nickname = user.nickname,
            preferredUsername = user.preferredUsername,
            profile = user.profile,
            picture = user.picture,
            website = user.website,
            email = user.email,
            emailVerified = user.emailVerified,
            gender = user.gender,
            birthdate = user.birthdate?.toString(),
            zoneinfo = user.zoneinfo,
            locale = user.locale,
            phoneNumber = user.phoneNumber,
            phoneNumberVerified = user.phoneNumberVerified,
            address = user.address?.let { AddressDto.fromJson(it) },
            updatedAt = user.getUpdatedAtEpoch(),
            roles = user.roles.map { it.name.replace("ROLE_", "") }.toSet(),
            provider = user.provider.name.lowercase()
        )
    }
}

/**
 * OIDC Address Claim structure.
 * @see <a href="https://openid.net/specs/openid-connect-core-1_0.html#AddressClaim">OIDC Address Claim</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class AddressDto(
    // Full mailing address, formatted for display
    val formatted: String? = null,

    // Full street address (may contain house number, street name, etc.)
    @JsonProperty("street_address")
    val streetAddress: String? = null,

    // City or locality
    val locality: String? = null,

    // State, province, prefecture, or region
    val region: String? = null,

    // Zip code or postal code
    @JsonProperty("postal_code")
    val postalCode: String? = null,

    // Country name
    val country: String? = null
) {
    companion object {
        fun fromJson(json: String): AddressDto? {
            return try {
                val mapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
                mapper.readValue(json, AddressDto::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun toJson(): String {
        val mapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
        return mapper.writeValueAsString(this)
    }
}

package com.vvise.template.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Response from the auth server's token introspection endpoint.
 * Uses OIDC-compliant field names.
 */
data class TokenIntrospectionResponse(
    val active: Boolean,
    val sub: String? = null,
    val email: String? = null,
    val name: String? = null,
    val roles: String? = null,
    val exp: Long? = null,
    val iat: Long? = null,
    // OIDC: picture - URL of the user's profile picture
    val picture: String? = null,
    // OIDC: email_verified
    @JsonProperty("email_verified")
    val emailVerified: Boolean? = null,
    // OIDC: given_name
    @JsonProperty("given_name")
    val givenName: String? = null,
    // OIDC: family_name
    @JsonProperty("family_name")
    val familyName: String? = null
) {
    /**
     * Parse roles string into a list.
     */
    fun getRolesList(): List<String> {
        return roles?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    }
}

package com.vvise.auth.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Response for token introspection endpoint.
 * Follows RFC 7662 Token Introspection standard.
 */
data class TokenIntrospectionResponse(
    /**
     * Whether the token is active (valid and not expired)
     */
    val active: Boolean,

    /**
     * User ID (subject)
     */
    val sub: String? = null,

    /**
     * User email
     */
    val email: String? = null,

    /**
     * User name
     */
    val name: String? = null,

    /**
     * User roles (comma-separated)
     */
    val roles: String? = null,

    /**
     * Token expiration timestamp (seconds since epoch)
     */
    val exp: Long? = null,

    /**
     * Token issued at timestamp (seconds since epoch)
     */
    val iat: Long? = null,

    /**
     * Image URL
     */
    @JsonProperty("image_url")
    val imageUrl: String? = null
) {
    companion object {
        fun inactive() = TokenIntrospectionResponse(active = false)
    }
}

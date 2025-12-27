package com.vvise.template.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Response from the auth server's token introspection endpoint.
 */
data class TokenIntrospectionResponse(
    val active: Boolean,
    val sub: String? = null,
    val email: String? = null,
    val name: String? = null,
    val roles: String? = null,
    val exp: Long? = null,
    val iat: Long? = null,
    @JsonProperty("image_url")
    val imageUrl: String? = null
) {
    /**
     * Parse roles string into a list.
     */
    fun getRolesList(): List<String> {
        return roles?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    }
}

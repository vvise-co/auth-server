package com.vvise.auth.security

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component("userSecurity")
class UserSecurity {

    fun isCurrentUser(userId: Long): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: return false

        if (!authentication.isAuthenticated) {
            return false
        }

        val principal = authentication.principal
        return if (principal is UserPrincipal) {
            principal.id == userId
        } else {
            false
        }
    }
}

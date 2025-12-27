package com.vvise.template.controller

import com.vvise.template.security.AuthenticatedUser
import com.vvise.template.security.CurrentUser
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Example controller demonstrating authentication patterns.
 * Replace or extend this with your own endpoints.
 */
@RestController
@RequestMapping("/api")
class ExampleController {

    /**
     * Public endpoint - no authentication required.
     */
    @GetMapping("/public/info")
    fun publicInfo(): Map<String, Any> {
        return mapOf(
            "message" to "This is a public endpoint",
            "authenticated" to false
        )
    }

    /**
     * Protected endpoint - requires authentication.
     * Uses @CurrentUser annotation to inject the authenticated user.
     */
    @GetMapping("/me")
    fun getCurrentUser(@CurrentUser user: AuthenticatedUser): Map<String, Any> {
        return mapOf(
            "id" to user.id,
            "email" to user.email,
            "name" to user.name,
            "roles" to user.roles
        )
    }

    /**
     * Admin-only endpoint - requires ADMIN role.
     */
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    fun adminDashboard(@CurrentUser user: AuthenticatedUser): Map<String, Any> {
        return mapOf(
            "message" to "Welcome to the admin dashboard",
            "adminUser" to user.name
        )
    }

    /**
     * Example of checking roles programmatically.
     */
    @GetMapping("/profile")
    fun getProfile(@CurrentUser user: AuthenticatedUser): Map<String, Any> {
        val profile = mutableMapOf<String, Any>(
            "id" to user.id,
            "email" to user.email,
            "name" to user.name
        )

        // Add admin-specific data if user is admin
        if (user.isAdmin()) {
            profile["adminFeatures"] = listOf("user-management", "system-settings")
        }

        return profile
    }
}

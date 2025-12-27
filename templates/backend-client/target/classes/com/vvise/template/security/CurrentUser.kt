package com.vvise.template.security

import org.springframework.security.core.annotation.AuthenticationPrincipal

/**
 * Annotation to inject the current authenticated user into controller methods.
 *
 * Usage:
 * ```kotlin
 * @GetMapping("/profile")
 * fun getProfile(@CurrentUser user: AuthenticatedUser): UserDto {
 *     return userService.getProfile(user.id)
 * }
 * ```
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@AuthenticationPrincipal
annotation class CurrentUser

package com.vvise.auth.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * User entity compliant with OpenID Connect Core 1.0 Standard Claims.
 * @see <a href="https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims">OIDC Standard Claims</a>
 */
@Entity
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["email"]),
        UniqueConstraint(columnNames = ["provider", "provider_id"])
    ]
)
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    // OIDC: sub - Subject Identifier (unique identifier for the user)
    // Using provider + providerId as the subject identifier
    @Column(name = "provider_id", nullable = false)
    var providerId: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var provider: AuthProvider = AuthProvider.GOOGLE,

    // OIDC: name - Full name
    @Column(nullable = false)
    var name: String = "",

    // OIDC: given_name - Given name(s) or first name(s)
    @Column(name = "given_name")
    var givenName: String? = null,

    // OIDC: family_name - Surname(s) or last name(s)
    @Column(name = "family_name")
    var familyName: String? = null,

    // OIDC: middle_name - Middle name(s)
    @Column(name = "middle_name")
    var middleName: String? = null,

    // OIDC: nickname - Casual name
    var nickname: String? = null,

    // OIDC: preferred_username - Shorthand name by which the user wishes to be referred to
    @Column(name = "preferred_username")
    var preferredUsername: String? = null,

    // OIDC: profile - URL of the user's profile page
    var profile: String? = null,

    // OIDC: picture - URL of the user's profile picture
    var picture: String? = null,

    // OIDC: website - URL of the user's web page or blog
    var website: String? = null,

    // OIDC: email - Preferred e-mail address
    @Column(nullable = false)
    var email: String = "",

    // OIDC: email_verified - True if the email has been verified
    @Column(name = "email_verified", nullable = false)
    var emailVerified: Boolean = false,

    // OIDC: gender - Gender
    var gender: String? = null,

    // OIDC: birthdate - Birthday (YYYY-MM-DD format)
    var birthdate: LocalDate? = null,

    // OIDC: zoneinfo - Time zone (e.g., "Europe/Paris" or "America/Los_Angeles")
    var zoneinfo: String? = null,

    // OIDC: locale - Locale (e.g., "en-US")
    var locale: String? = null,

    // OIDC: phone_number - Preferred telephone number (E.164 format recommended)
    @Column(name = "phone_number")
    var phoneNumber: String? = null,

    // OIDC: phone_number_verified - True if the phone number has been verified
    @Column(name = "phone_number_verified", nullable = false)
    var phoneNumberVerified: Boolean = false,

    // OIDC: address - Preferred postal address (stored as JSON)
    @Column(columnDefinition = "TEXT")
    var address: String? = null,

    // Application-specific fields
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    var roles: MutableSet<Role> = mutableSetOf(),

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    // OIDC: updated_at - Time the user's information was last updated (as Unix timestamp)
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
) {
    /**
     * Returns the OIDC subject identifier (sub claim).
     * Format: provider:providerId (e.g., "google:123456789")
     */
    fun getSub(): String = "${provider.name.lowercase()}:$providerId"

    /**
     * Returns updated_at as Unix timestamp (seconds since epoch) per OIDC spec.
     */
    fun getUpdatedAtEpoch(): Long? = updatedAt?.let {
        it.atZone(java.time.ZoneOffset.UTC).toEpochSecond()
    }

    enum class AuthProvider {
        GOOGLE,
        GITHUB,
        MICROSOFT
    }
}

package com.vvise.template.security

import com.vvise.template.config.AuthProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import javax.crypto.SecretKey

/**
 * Validates JWT tokens issued by the central auth server.
 * The JWT_SECRET must match the auth server's secret.
 */
@Component
class JwtTokenValidator(
    private val authProperties: AuthProperties
) {
    private val logger = LoggerFactory.getLogger(JwtTokenValidator::class.java)

    private val key: SecretKey by lazy {
        val secretBytes = authProperties.jwtSecret.toByteArray(StandardCharsets.UTF_8)
        if (secretBytes.size < 32) {
            throw IllegalArgumentException("JWT secret must be at least 256 bits (32 bytes)")
        }
        Keys.hmacShaKeyFor(secretBytes)
    }

    /**
     * Validates a JWT token and returns the claims if valid.
     */
    fun validateToken(token: String): Claims? {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (ex: SignatureException) {
            logger.error("Invalid JWT signature")
            null
        } catch (ex: MalformedJwtException) {
            logger.error("Invalid JWT token")
            null
        } catch (ex: ExpiredJwtException) {
            logger.error("JWT token is expired")
            null
        } catch (ex: UnsupportedJwtException) {
            logger.error("JWT token is unsupported")
            null
        } catch (ex: IllegalArgumentException) {
            logger.error("JWT claims string is empty")
            null
        }
    }

    /**
     * Extracts user ID from token claims.
     */
    fun getUserId(claims: Claims): Long? {
        return claims.subject?.toLongOrNull()
    }

    /**
     * Extracts email from token claims.
     */
    fun getEmail(claims: Claims): String? {
        return claims["email"] as? String
    }

    /**
     * Extracts username/name from token claims.
     */
    fun getName(claims: Claims): String? {
        return claims["name"] as? String
    }

    /**
     * Extracts roles from token claims.
     */
    @Suppress("UNCHECKED_CAST")
    fun getRoles(claims: Claims): List<String> {
        return (claims["roles"] as? List<String>) ?: emptyList()
    }
}

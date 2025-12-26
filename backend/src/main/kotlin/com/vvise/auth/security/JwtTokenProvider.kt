package com.vvise.auth.security

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider {

    private val log = LoggerFactory.getLogger(JwtTokenProvider::class.java)

    @Value("\${app.jwt.secret}")
    private lateinit var jwtSecret: String

    @Value("\${app.jwt.access-token-expiration}")
    private var accessTokenExpiration: Long = 0

    private fun getSigningKey(): SecretKey {
        val keyBytes = jwtSecret.toByteArray(StandardCharsets.UTF_8)

        // Ensure the key is at least 256 bits (32 bytes) for HS256
        return if (keyBytes.size < 32) {
            val paddedKey = ByteArray(32)
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.size)
            Keys.hmacShaKeyFor(paddedKey)
        } else {
            Keys.hmacShaKeyFor(keyBytes)
        }
    }

    fun generateAccessToken(authentication: Authentication): String {
        val userPrincipal = authentication.principal as UserPrincipal
        return generateAccessToken(userPrincipal)
    }

    fun generateAccessToken(userPrincipal: UserPrincipal): String {
        val now = Date()
        val expiryDate = Date(now.time + accessTokenExpiration)

        val roles = userPrincipal.authorities.joinToString(",") { it.authority }

        return Jwts.builder()
            .subject(userPrincipal.id.toString())
            .claim("email", userPrincipal.getEmail())
            .claim("name", userPrincipal.name)
            .claim("roles", roles)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey())
            .compact()
    }

    fun getUserIdFromToken(token: String): Long {
        val claims = Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .payload

        return claims.subject.toLong()
    }

    fun getClaimsFromToken(token: String): Claims {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .payload
    }

    fun validateToken(authToken: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(authToken)
            true
        } catch (ex: MalformedJwtException) {
            log.error("Invalid JWT token")
            false
        } catch (ex: ExpiredJwtException) {
            log.error("Expired JWT token")
            false
        } catch (ex: UnsupportedJwtException) {
            log.error("Unsupported JWT token")
            false
        } catch (ex: IllegalArgumentException) {
            log.error("JWT claims string is empty")
            false
        } catch (ex: SecurityException) {
            log.error("Invalid JWT signature")
            false
        }
    }

    fun getAccessTokenExpiration(): Long = accessTokenExpiration
}

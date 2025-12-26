package com.vvise.auth.service

import com.vvise.auth.entity.RefreshToken
import com.vvise.auth.entity.User
import com.vvise.auth.repository.RefreshTokenRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository
) {
    private val log = LoggerFactory.getLogger(RefreshTokenService::class.java)

    @Value("\${app.jwt.refresh-token-expiration}")
    private var refreshTokenExpiration: Long = 0

    @Transactional
    fun createRefreshToken(user: User): RefreshToken {
        val refreshToken = RefreshToken(
            user = user,
            token = UUID.randomUUID().toString(),
            expiryDate = Instant.now().plusMillis(refreshTokenExpiration)
        )
        return refreshTokenRepository.save(refreshToken)
    }

    @Transactional(readOnly = true)
    fun findByToken(token: String): RefreshToken? = refreshTokenRepository.findByToken(token)

    @Transactional
    fun verifyExpiration(token: RefreshToken): RefreshToken {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token)
            throw RuntimeException("Refresh token was expired. Please sign in again.")
        }
        return token
    }

    @Transactional
    fun deleteByUser(user: User) {
        refreshTokenRepository.deleteByUser(user)
    }

    @Transactional
    fun deleteByToken(token: String) {
        refreshTokenRepository.findByToken(token)?.let {
            refreshTokenRepository.delete(it)
        }
    }

    @Scheduled(cron = "0 0 */6 * * *") // Run every 6 hours
    @Transactional
    fun cleanupExpiredTokens() {
        log.info("Cleaning up expired refresh tokens")
        refreshTokenRepository.deleteExpiredTokens(Instant.now())
    }
}

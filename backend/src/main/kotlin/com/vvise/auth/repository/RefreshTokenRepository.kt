package com.vvise.auth.repository

import com.vvise.auth.entity.RefreshToken
import com.vvise.auth.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun findByToken(token: String): RefreshToken?

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.user = :user")
    fun deleteByUser(user: User)

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiryDate < :now")
    fun deleteExpiredTokens(now: Instant)
}

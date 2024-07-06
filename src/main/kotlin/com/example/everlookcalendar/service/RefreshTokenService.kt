package com.example.everlookcalendar.service

import com.example.everlookcalendar.data.RefreshToken
import com.example.everlookcalendar.repository.RefreshTokenRepo
import com.example.everlookcalendar.repository.UserRepo
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.ResponseStatus
import java.time.Instant
import java.util.*


@Service
class RefreshTokenService(
    @Autowired val refreshTokenRepository: RefreshTokenRepo,
    @Autowired val userRepository: UserRepo
) {
    val expirationTime: String = System.getenv("EXPIRATION_TIME");

    fun findByToken(token: String): Optional<RefreshToken>? {
        return refreshTokenRepository.findByToken(token)
    }

    fun createRefreshToken(userId: Int): RefreshToken {
        // create refresh token based on user id
        var refreshToken = RefreshToken(
            userRepository.findById(userId).get(),
            UUID.randomUUID().toString(),
            Instant.now().plusSeconds(expirationTime.toLong()*480)
        )
        // saved refresh token in db
        refreshToken = refreshTokenRepository.save(refreshToken)
        return refreshToken
    }

    fun verifyExpiration(token: RefreshToken): RefreshToken {
        if (token.expiryDate < Instant.now()) {
            refreshTokenRepository.delete(token)
            throw TokenRefreshException(token.token, "Refresh token was expired. signin again")
        }

        return token
    }

    @Transactional // if any delete end up failing; rollback the entire transaction (ensuring consistency)
    fun deleteByUserId(userId: Int): Int {
        return refreshTokenRepository.deleteByUser(userRepository.findById(userId).get())
    }
}

@ResponseStatus(HttpStatus.FORBIDDEN)
class TokenRefreshException(token: String?, message: String?) :
    RuntimeException(String.format("Failed for [%s]: %s", token, message)) {
    companion object {
        private const val serialVersionUID = 1L
    }
}
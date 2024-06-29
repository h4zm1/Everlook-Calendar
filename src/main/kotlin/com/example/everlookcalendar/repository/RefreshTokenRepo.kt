package com.example.everlookcalendar.repository

import com.example.everlookcalendar.data.Authority
import com.example.everlookcalendar.data.RefreshToken
import com.example.everlookcalendar.data.UserAuthority
import com.example.everlookcalendar.data.UserCred
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*


@Repository
interface RefreshTokenRepo : CrudRepository<RefreshToken, Int> {
    fun findByToken(token: String?): Optional<RefreshToken>

    @Modifying
    fun deleteByUser(user: UserCred):Int
}
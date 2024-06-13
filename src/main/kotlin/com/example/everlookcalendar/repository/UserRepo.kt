package com.example.everlookcalendar.repository

import com.example.everlookcalendar.data.UserCred
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepo : CrudRepository<UserCred, Int> {
    fun findByEmail(email: String?): Optional<UserCred?>?
}
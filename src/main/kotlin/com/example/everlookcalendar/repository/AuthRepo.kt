package com.example.everlookcalendar.repository

import com.example.everlookcalendar.data.Authority
import com.example.everlookcalendar.data.UserAuthority
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AuthRepo : CrudRepository<UserAuthority, Int>

@Repository
interface RoleRepo : CrudRepository<Authority, Int> {
    fun findByName(role: String?): Optional<Authority?>
}
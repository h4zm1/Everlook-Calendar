package com.example.everlookcalendar.service

import com.example.everlookcalendar.data.UserCred
import com.example.everlookcalendar.repository.UserRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service


@Service
class AuthService(@Autowired val userRepo: UserRepo) {
    private val authenticationManager: AuthenticationManager? = null

    fun authenticate(input: UserCred): UserCred {
//        authenticationManager!!.authenticate(
//            UsernamePasswordAuthenticationToken(
//                input.email,
//                input.password
//            )
//        )

        return userRepo.findByEmail(input.email)
            ?.orElseThrow()!!
    }
}
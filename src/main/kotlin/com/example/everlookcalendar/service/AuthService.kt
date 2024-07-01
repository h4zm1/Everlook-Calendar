package com.example.everlookcalendar.service

import com.example.everlookcalendar.data.UserCred
import com.example.everlookcalendar.repository.UserRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service


@Service
class AuthService(@Autowired val userRepo: UserRepo, @Autowired val authenticationManager: AuthenticationManager) {

    fun authenticate(input: UserCred): UserCred {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                input.username,
                input.password
            )
        )
        SecurityContextHolder.getContext().authentication = authentication;
        return userRepo.findByEmail(input.username)
            ?.orElseThrow()!!
    }
}
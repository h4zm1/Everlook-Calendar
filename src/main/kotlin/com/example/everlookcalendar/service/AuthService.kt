package com.example.everlookcalendar.service

import com.example.everlookcalendar.data.UserCred
import com.example.everlookcalendar.repository.UserRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service


@Service
class AuthService(@Autowired val userRepo: UserRepo, @Autowired val authenticationManager: AuthenticationManager) {

    fun authenticate(input: UserCred): UserCred {
        // authenticationManager.authenticate will call userDetailsService() in SecurityConfig
        // to load users and specifily the one that implements UserDetails (UserCred)
        // without this password validation won't happen and can login with anypassword
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                input.username,
                input.password
            )
        )

        // Fetch the user with authorities
        val user = userRepo.findByEmail(input.username)
            ?.orElseThrow { UsernameNotFoundException("User not found") }!!



        // You can delete this line - not needed for JWT-based auth
        // SecurityContextHolder.getContext().authentication = authentication

        return user
//        return userRepo.findByEmail(input.username)
//            ?.orElseThrow()!!
    }
}
package com.example.everlookcalendar.controller

import com.example.everlookcalendar.data.UserCred
import com.example.everlookcalendar.repository.UserRepo
import com.example.everlookcalendar.service.AuthService
import com.example.everlookcalendar.service.JwtService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController


@RestController
class AuthController(
    @Autowired val userRepo: UserRepo,
    @Autowired val authService: AuthService,
    @Autowired val jwtService: JwtService
) {
    private val authenticationManager: AuthenticationManager? = null

    @PostMapping("/auth/login")
    fun login(@RequestBody user: UserCred): ResponseEntity<String> {
        val authenticatedUser: UserCred = authService.authenticate(user)

        val jwtToken: String = jwtService.generateToken(authenticatedUser)

        println("login mail " + user.email + " pass " + user.password)
        return ResponseEntity.ok(jwtToken)
    }

    @PostMapping("/auth/register")
    fun register(@RequestBody cred: UserCred) {
        println("reg mail " + cred.email + " pass " + cred.password)
    }

}
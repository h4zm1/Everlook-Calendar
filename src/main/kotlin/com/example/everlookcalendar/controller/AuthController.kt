package com.example.everlookcalendar.controller

import com.example.everlookcalendar.data.UserAuthority
import com.example.everlookcalendar.data.UserCred
import com.example.everlookcalendar.repository.AuthRepo
import com.example.everlookcalendar.repository.RoleRepo
import com.example.everlookcalendar.repository.UserRepo
import com.example.everlookcalendar.service.AuthService
import com.example.everlookcalendar.service.JwtService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController


@RestController
class AuthController(
    @Autowired val userRepo: UserRepo,
    @Autowired val roleRepo: RoleRepo,
    @Autowired val authService: AuthService,
    @Autowired val jwtService: JwtService,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val authRepo: AuthRepo
) {

    @PostMapping("/auth/login")
    fun login(@RequestBody user: UserCred): ResponseEntity<Map<String, Any>> {
        val authenticatedUser: UserCred = authService.authenticate(user)
        val jwtToken: String = jwtService.generateToken(authenticatedUser)

        println("login mail " + user.username + " role " + authenticatedUser.authorities)
        val data = mapOf(
            "token" to jwtToken,
            "expiresIn" to jwtService.expirationTime
        )
        return ResponseEntity.ok(data)
    }

    @PostMapping("/auth/register")
    fun register(@RequestBody cred: UserCred): ResponseEntity<String> {
        println("reg")
        val user = UserCred(cred.username, passwordEncoder.encode(cred.password))
        val savedUser = userRepo.save(user)
        authRepo.save(UserAuthority(savedUser,roleRepo.findByName("ROLE_GUEST").get()))
        return ResponseEntity.ok("User registered successfully!");

    }

}
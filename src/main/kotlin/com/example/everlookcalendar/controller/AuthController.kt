package com.example.everlookcalendar.controller

import com.example.everlookcalendar.data.RefreshToken
import com.example.everlookcalendar.data.UserAuthority
import com.example.everlookcalendar.data.UserCred
import com.example.everlookcalendar.repository.AuthRepo
import com.example.everlookcalendar.repository.RoleRepo
import com.example.everlookcalendar.repository.UserRepo
import com.example.everlookcalendar.service.AuthService
import com.example.everlookcalendar.service.JwtService
import com.example.everlookcalendar.service.RefreshTokenService
import com.example.everlookcalendar.service.TokenRefreshException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/auth")
class AuthController(
    @Autowired val userRepo: UserRepo,
    @Autowired val roleRepo: RoleRepo,
    @Autowired val authService: AuthService,
    @Autowired val jwtService: JwtService,
    @Autowired val refreshTokenService: RefreshTokenService,
    @Autowired val userDetailsService: UserDetailsService,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val authRepo: AuthRepo
) {

    @PostMapping("/login")
    fun login(@RequestBody user: UserCred): ResponseEntity<Any> {
        val authenticatedUser: UserCred = authService.authenticate(user)
        val jwtCookie = jwtService.generateJwtCookie(authenticatedUser)
        // can't call transactional method from same class, so I had to do it here
        // making sure all previous refresh tokens are deleted upon login
        refreshTokenService.deleteByUserId(authenticatedUser.id)
        val refreshToken = refreshTokenService.createRefreshToken(authenticatedUser.id)
        val jwtRefreshCookie = jwtService.generateRefreshJwtCookie(refreshToken.token)

        println("login mail " + user.username + " role " + authenticatedUser.authorities)
        val data = mapOf(
//            "token" to jwt  for Token,
//            "expiresIn" to jwtService.expirationTime
            "roles" to authenticatedUser.authorities
        )

        val headers = HttpHeaders()
        headers.add("Set-Cookie", jwtCookie.toString())
        headers.add("Set-Cookie", jwtRefreshCookie.toString())

        return ResponseEntity<Any>(data, headers, HttpStatus.OK)
    }

    @PostMapping("/register")
    fun register(@RequestBody cred: UserCred): ResponseEntity<String> {
        val user = UserCred(cred.username, passwordEncoder.encode(cred.password))
        val savedUser = userRepo.save(user)
        authRepo.save(UserAuthority(savedUser, roleRepo.findByName("ROLE_GUEST").get()))
        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/refreshtoken")
    fun refreshToken(request: HttpServletRequest): ResponseEntity<*> {
        println("INSIDE REFRESH")
        // ?: = if null
        val refreshToken = jwtService.getJwtRefreshFromCookies(request) ?: return ResponseEntity.badRequest()
            .body("Refresh Token is empty!")

        val storedRefreshToken = refreshTokenService.findByToken(refreshToken) ?: throw TokenRefreshException(
            refreshToken, "Refresh Token is not in database!"
        )

        val verifiedToken = refreshTokenService.verifyExpiration(storedRefreshToken.get())

        val jwtCookie = jwtService.generateJwtCookie(verifiedToken.user)
        println("jwt cookie info::::: age "+jwtCookie.maxAge)
//        println("jwt cookie info::::: "+jwtService.getJ)

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
            .body("Token is refreshed successfully!")
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_GUEST')")
    @GetMapping("/status")
    fun checkAuthStatus(request: HttpServletRequest): ResponseEntity<Boolean> {
        println("INSIDE STATUS")
        // return 403 if there's no jwt cookie/token in the request
        jwtService.getJwtFromCookies(request) ?: return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false)
        return ResponseEntity.ok(true)
    }

    /// needs reworks, this function won't receive any data from the client
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/signout")
    fun logoutUser(request: HttpServletRequest): ResponseEntity<*> {
        val token = jwtService.getJwtFromCookies(request)

        val userEmail = jwtService.extractUsername(token);
        val userId = jwtService.extractUserId(token);

        refreshTokenService.deleteByUserId(userId)

        val jwtCookie: ResponseCookie = jwtService.getCleanJwtToken()
        val jwtRefreshCookie: ResponseCookie = jwtService.getCleanRefreshToken()

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
            .header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString()).body<Any>(("You've been signed out!"))
    }


}


package com.example.everlookcalendar.controller

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
import org.springframework.http.*
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.*


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
    fun login(@RequestBody user: UserCred, request: HttpServletRequest): ResponseEntity<Any> {

        val authenticatedUser: UserCred = try {
            authService.authenticate(user)
        } catch (e: BadCredentialsException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    mapOf(
                        "error" to "Invalid email or password",
                        "errorCode" to "BAD_CREDENTIALS",
                        "type" to "authentication"
                    )
                )
        }
        // check role
        if (authenticatedUser.authorities.first().authority == "ROLE_USER") {
            //println("USER tried to log in")
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    mapOf(
                        "error" to "You don't have access yet. Wait for an email confirmation.",
                        "errorCode" to "ROLE_RESTRICTED",
                        "type" to "authorization"
                    )
                )
        }
        val jwtCookie = jwtService.generateJwtCookie(authenticatedUser)
        // can't call transactional method from same class, so I had to do it here
        // making sure all previous refresh tokens are deleted upon login
        refreshTokenService.deleteByUserId(authenticatedUser.id)

        val refreshToken = refreshTokenService.createRefreshToken(authenticatedUser.id)

        val jwtRefreshCookie = jwtService.generateRefreshJwtCookie(refreshToken.token)

        //println("login mail " + user.username + " role " + authenticatedUser.authorities)
        val data = mapOf(
//            "token" to jwt  for Token,
//            "expiresIn" to jwtService.expirationTime
            "roles" to authenticatedUser.authorities,
            "email" to authenticatedUser.username
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
        authRepo.save(UserAuthority(savedUser, roleRepo.findByName("ROLE_USER").get()))
        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/refreshtoken")
    fun refreshToken(request: HttpServletRequest): ResponseEntity<*> {
        //println("INSIDE REFRESH")
        // ?: = if null
        val refreshToken = jwtService.getJwtRefreshFromCookies(request) ?: return ResponseEntity.badRequest()
            .body("Refresh Token is empty!")

        val storedRefreshToken = refreshTokenService.findByToken(refreshToken) ?: throw TokenRefreshException(
            refreshToken, "Refresh Token is not in database!"
        )

        val verifiedToken = refreshTokenService.verifyExpiration(storedRefreshToken.get())

        val jwtCookie = jwtService.generateJwtCookie(verifiedToken.user)
        //println("jwt cookie info::::: age " + jwtCookie.maxAge)
//        //println("jwt cookie info::::: "+jwtService.getJ)

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
            .body("Token is refreshed successfully!")
    }


    // used for auto login
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_GUEST')")
    @GetMapping("/status")
    fun checkAuthStatus(request: HttpServletRequest): ResponseEntity<Any> {
        //println("INSIDE STATUS")
        val jwtFromCookie = jwtService.getJwtFromCookies(request)
//        val data = mapOf(
//            "status" to !jwtFromCookie.isNullOrBlank(),
//            "email" to jwtService.extractUsername(jwtFromCookie)
//        )
        val data = buildMap {
            put("status", !jwtFromCookie.isNullOrBlank())
            if (jwtFromCookie.isNullOrBlank())
                put("email", "")
            if (!jwtFromCookie.isNullOrBlank())
                put("email", jwtService.extractUsername(jwtFromCookie))

//            else
        }
//        return ResponseEntity<Any>(data, HttpStatus.OK)

//         return 403 if there's no jwt cookie/token in the request
        jwtFromCookie ?: return ResponseEntity<Any>(data, HttpStatus.FORBIDDEN)
//        jwtService.getJwtFromCookies(request) ?: return  ResponseEntity.ok(false)
        return ResponseEntity<Any>(data, HttpStatus.OK)
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_GUEST')")
    @PostMapping("/signout")
    fun logoutUser(request: HttpServletRequest): ResponseEntity<*> {
        val token = jwtService.getJwtFromCookies(request)

        val userId = jwtService.extractUserId(token);

        refreshTokenService.deleteByUserId(userId)

        val jwtCookie: ResponseCookie = jwtService.getCleanJwtToken()
        val jwtRefreshCookie: ResponseCookie = jwtService.getCleanRefreshToken()

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
            .header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString())
            .body("You've been signed out!")
    }


}


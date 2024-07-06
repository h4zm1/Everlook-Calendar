package com.example.everlookcalendar.service

import com.example.everlookcalendar.data.UserCred
import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseCookie
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import org.springframework.web.util.WebUtils
import java.security.Key
import java.util.*

@Service
class JwtService {
    val logger: Logger = LoggerFactory.getLogger(JwtService::class.java)

    val secretKey: String = System.getenv("SECRET_KEY");
    val expirationTime: String = System.getenv("EXPIRATION_TIME");

    fun extractUsername(token: String?): String {
        return extractClaim<String>(token, Claims::getSubject)
    }

    fun extractUserId(token: String?): Int {
        return extractClaim<String>(token, Claims::getId).toInt()
    }

    //
    fun extractExpiration(token: String?): Date {
        return extractClaim<Date>(token, Claims::getExpiration)
    }

    fun getCleanJwtToken(): ResponseCookie {
        val responseCookie = ResponseCookie.from("jwt", "").path("/auth").build()
        return responseCookie
    }

    fun getCleanRefreshToken(): ResponseCookie {
        val responseCookie = ResponseCookie.from("jwt-refresh", "").path("/auth/refreshtoken").build()
        return responseCookie
    }

    fun generateJwtCookie(user: UserCred): ResponseCookie {
        val jwt = generateToken(user)
        return generateCookie("jwt", jwt, "/", expirationTime.toLong())
    }

    fun generateRefreshJwtCookie(refreshToken: String): ResponseCookie {
        return generateCookie("jwt-refresh", refreshToken, "/",expirationTime.toLong()*480)
    }

    fun generateCookie(name: String, value: String, path: String, age:Long): ResponseCookie {

        val responseCookie =
            ResponseCookie.from(name, value).path(path).maxAge(age).httpOnly(true).secure(true).sameSite("Strict")
                .build()
        return responseCookie
    }

    fun validateJwtToken(token: String): Boolean {
        try {
            Jwts.parser().setSigningKey(getSignInKey()).build().parse(token)
            return true
        } catch (e: MalformedJwtException) {
            logger.error("Invalid JWT token: {}", e.message)
        } catch (e: ExpiredJwtException) {
            logger.error("JWT token is expired: {}", e.message);
        } catch (e: UnsupportedJwtException) {
            logger.error("JWT token is unsupported: {}", e.message);
        } catch (e: IllegalArgumentException) {
            logger.error("JWT claims string is empty: {}", e.message);
        }
        return false
    }

    fun <T> extractClaim(token: String?, claimsResolver: (Claims) -> T): T {
        val claims: Claims = extractAllClaims(token)
        return claimsResolver(claims)
    }

    private fun extractAllClaims(token: String?): Claims {
        return Jwts.parser().setSigningKey(getSignInKey())
            .build()
            .parseSignedClaims(token)
            .payload
    }

    fun generateToken(user: UserCred): String {
        val claims: Map<String, Any> = HashMap()
        return createToken(claims, user.username, user.id.toString())
    }


    private fun createToken(claims: Map<String, Any>, subject: String, id: String): String {
        return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .id(id)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + expirationTime.toInt()))
            .signWith(getSignInKey(), io.jsonwebtoken.SignatureAlgorithm.HS256)
            .compact()
    }

    fun getSignInKey(): Key {
        val keyBytes = Decoders.BASE64.decode(secretKey)
        return Keys.hmacShaKeyFor(keyBytes)
    }

    fun isTokenExpired(token: String?): Boolean {
        return extractExpiration(token).before(Date())
    }

    fun validateToken(token: String?, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return (username == userDetails.username && !isTokenExpired(token))
    }

    fun getJwtFromCookies(request: HttpServletRequest): String? {
        return getCookieValueByName(request, "jwt")
    }

    fun getJwtRefreshFromCookies(request: HttpServletRequest): String? {
        return getCookieValueByName(request, "jwt-refresh")
    }


    private fun getCookieValueByName(request: HttpServletRequest, name: String): String? {
        val cookie = WebUtils.getCookie(request, name)
        if (cookie != null) {
            return cookie.value
        } else {
            logger.error("no cookie found by the name " + name)
            return null
        }
    }
}



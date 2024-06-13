package com.example.everlookcalendar.service

import com.example.everlookcalendar.data.UserCred
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.security.Key
import java.util.*

@Service
class JwtService {
    val secretKey = System.getenv("SECRET_KEY");
    val expirationTime = 900;

    fun extractUsername(token: String?): String {
        return extractClaim<String>(token, Claims::getSubject)
    }

    //
    fun extractExpiration(token: String?): Date {
        return extractClaim<Date>(token, Claims::getExpiration)
    }

    //
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

    fun generateToken(userDetails: UserCred): String {
        val claims: Map<String, Any> = HashMap()
        return createToken(claims, userDetails.email)
    }


    private fun createToken(claims: Map<String, Any>, subject: String): String {
        return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + expirationTime))
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

}



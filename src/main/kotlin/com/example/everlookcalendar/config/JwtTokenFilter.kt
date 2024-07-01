package com.example.everlookcalendar.config

import com.example.everlookcalendar.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter


@Component
class JwtTokenFilter(
    @Autowired val jwtService: JwtService,
    @Autowired val userDetailsService: UserDetailsService
) : OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        var userEmail = "";
        var jwtToken = "";

        // try and get jwt from cookie, .toString to work around the null check
        jwtToken = jwtService.getJwtFromCookies(request).toString()

        if (jwtToken.isNotEmpty()) {
            try {
                userEmail = jwtService.extractUsername(jwtToken)
                println("Extracting username from token: $userEmail")

                // Set the JWT in the request attributes for Spring Security to use
                request.setAttribute("Authorization", "Bearer $jwtToken")

            } catch (e: Exception) {
                // Handle token extraction/validation errors
                println("Error extracting username from token:" + e.message)
            }
        }

        if (userEmail.isNotEmpty() && SecurityContextHolder.getContext().authentication == null) {
            val userDetails: UserDetails = userDetailsService.loadUserByUsername(userEmail)
            if (jwtService.validateToken(jwtToken, userDetails)) {
                val authenticationToken = UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.authorities
                )

                authenticationToken.details = WebAuthenticationDetailsSource().buildDetails(request)

                SecurityContextHolder.getContext().authentication = authenticationToken
            }
        }

        chain.doFilter(request, response)
    }
}

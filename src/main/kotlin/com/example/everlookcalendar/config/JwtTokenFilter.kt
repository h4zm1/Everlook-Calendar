package com.example.everlookcalendar.config

import com.example.everlookcalendar.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter


@Component
class JwtTokenFilter(
    @Autowired val jwtService: JwtService
) : OncePerRequestFilter() {

    override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain) {
        val authorizationHeader = req.getHeader("Authorization");
        var username = "";
        var jwtToken = "";

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwtToken = authorizationHeader.substring(7);
            try {
                username = jwtService.extractUsername(jwtToken);
            } catch (e: Exception) {
                // Handle token extraction/validation errors
                System.out.println("Error extracting username from token: " + e.message);
            }
        }
      

        chain.doFilter(req, res)
    }
}

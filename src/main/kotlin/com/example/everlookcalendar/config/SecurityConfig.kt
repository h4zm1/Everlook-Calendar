package com.example.everlookcalendar.config

import com.example.everlookcalendar.repository.UserRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(@Autowired val env: Environment, @Autowired val userRepo: UserRepo) {
    private final val allowedOrigins = env.getProperty("CORS-ORIGINS", String::class.java)
    val allowedOriginsList = allowedOrigins?.split(",")?.map { it.trim() } ?: listOf()

    @Bean
    fun filterChain(
        http: HttpSecurity,
        jwtTokenFilter: JwtTokenFilter,
        rateLimitFilter: RateLimiterFilter
    ): SecurityFilterChain {
        http {
            csrf {
                disable()
            }
            cors {
                // will use a Bean by the name of corsConfigurationSource or corsFilter (by default)
            }
            authorizeRequests {
                authorize("/zgenchants", permitAll)
                authorize("/css/**", permitAll)
                authorize("/user/**", hasAuthority("ROLE_USER"))
                authorize("/config", hasAuthority("ROLE_USER"))
                authorize("/logout", permitAll)
                authorize("/auth/login", permitAll)
                authorize("/auth/checkAuthStatus", authenticated)
                authorize("/auth/signout", authenticated)
                authorize("/conf/updateStartDate", authenticated, hasAuthority("ROLE_USER"))
                authorize("/conf/getToggle", authenticated)
                authorize("/conf/setToggle", authenticated)
                authorize("/conf/testAuth", authenticated)
                authorize("/conf/updateTwentyMan", authenticated)
                authorize("/actuator/**", hasAuthority("ROLE_ADMIN"))
            }

            // handling processing incoming requests for token verification
            addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter::class.java)
            // ratelimit filter happen after any authentication logic (for auth work to work)
            addFilterAfter(rateLimitFilter, JwtTokenFilter::class.java)
            headers {

            }
        }
        return http.build()
    }


    @Bean
    fun corsFilter(): CorsFilter {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()
        config.allowCredentials = true
        config.allowedOrigins = allowedOriginsList
        config.addAllowedHeader("*")
        config.addAllowedMethod("*")
        source.registerCorsConfiguration("/**", config)
        return CorsFilter(source)
    }


    @Bean
    fun userDetailsService(): UserDetailsService {
        return UserDetailsService { username: String? ->
            userRepo.findByEmail(username)?.orElseThrow { UsernameNotFoundException("User not found") }
        }
    }

    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    @Throws(Exception::class)
    fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager {
        return authConfig.authenticationManager
    }

}
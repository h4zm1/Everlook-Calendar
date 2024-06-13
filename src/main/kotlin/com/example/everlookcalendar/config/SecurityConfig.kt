package com.example.everlookcalendar.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.JdbcUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
import javax.sql.DataSource

@Configuration
@EnableWebSecurity
class SecurityConfig(@Autowired val env: Environment) {
    val clearSiteData = HeaderWriterLogoutHandler(ClearSiteDataHeaderWriter(ClearSiteDataHeaderWriter.Directive.ALL))
    private final val allowedOrigins = env.getProperty("CORS-ORIGINS", String::class.java)
    val allowedOriginsList = allowedOrigins?.split(",")?.map { it.trim() } ?: listOf()

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf{
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
                authorize("/api/update", permitAll)
                authorize("/auth/login", permitAll)
                authorize("/api/setToggle", permitAll)
                authorize("/api/getToggle", permitAll)
                authorize("/api/updateTwentyMan", permitAll)
            }
            formLogin {
                defaultSuccessUrl("/config", true)
                loginPage = "/login"
                permitAll()
            }
            logout {
                addLogoutHandler(clearSiteData)
                logoutSuccessUrl = "/login"
                invalidateHttpSession = true
            }
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

    // retrieving users from datasource
    @Bean
    fun users(dataSource: DataSource): UserDetailsService {
        return JdbcUserDetailsManager(dataSource)
    }
}
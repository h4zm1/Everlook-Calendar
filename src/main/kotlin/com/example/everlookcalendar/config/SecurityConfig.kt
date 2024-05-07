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
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
//import org.springframework.web.cors.reactive.CorsConfigurationSource
//import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import javax.sql.DataSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    @Autowired val environment: Environment,
) {
    val clearSiteData = HeaderWriterLogoutHandler(ClearSiteDataHeaderWriter(ClearSiteDataHeaderWriter.Directive.ALL))

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            cors {
                // will use a Bean by the name of corsConfigurationSource (by default)
            }
            authorizeRequests {
                authorize("/css/**", permitAll)
                authorize("/user/**", hasAuthority("ROLE_USER"))
                authorize("/config", hasAuthority("ROLE_USER"))
                authorize("/logout", permitAll)
                authorize("/api/update", permitAll)
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
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration()
//        if (environment.activeProfiles.isNotEmpty()){
        if (environment.activeProfiles[0] == "dev") {
            config.allowedOrigins = listOf("*") // Allow all origins
            config.allowedMethods = listOf("*") // Allow all methods
            config.allowedHeaders = listOf("*") // Allow all headers
        }
//        val source = UrlBasedCorsConfigurationSource()
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return source
    }


    // retrieving users from datasource
    @Bean
    fun users(dataSource: DataSource): UserDetailsService {
        return JdbcUserDetailsManager(dataSource)
    }
}
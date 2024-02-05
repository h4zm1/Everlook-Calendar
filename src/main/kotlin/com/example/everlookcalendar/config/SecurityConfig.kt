package com.example.everlookcalendar.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.JdbcUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter
import javax.sql.DataSource

@Configuration
@EnableWebSecurity
class SecurityConfig {
    val clearSiteData = HeaderWriterLogoutHandler(ClearSiteDataHeaderWriter(ClearSiteDataHeaderWriter.Directive.ALL))

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeRequests {
                authorize("/css/**", permitAll)
                authorize("/user/**", hasAuthority("ROLE_USER"))
                authorize("/config", hasAuthority("ROLE_USER"))
                authorize("/logout", permitAll)
                authorize("/api/update", permitAll)
                authorize("/api/updateTwentyMan", permitAll)
            }
            formLogin {
                defaultSuccessUrl("/config",true)
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


    // retrieving users from datasource
    @Bean
    fun users(dataSource: DataSource): UserDetailsService {
        return JdbcUserDetailsManager(dataSource)
    }
}
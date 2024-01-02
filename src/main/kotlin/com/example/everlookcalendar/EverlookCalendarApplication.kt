package com.example.everlookcalendar

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity

@SpringBootApplication
@EnableWebSecurity
class EverlookCalendarApplication

fun main(args: Array<String>) {
    runApplication<EverlookCalendarApplication>(*args)
}

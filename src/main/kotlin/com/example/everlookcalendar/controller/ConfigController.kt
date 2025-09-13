package com.example.everlookcalendar.controller

import com.example.everlookcalendar.data.ConfigValues
import com.example.everlookcalendar.data.ToggleDebug
import com.example.everlookcalendar.data.TwentyManDate
import com.example.everlookcalendar.repository.ConfigRepo
import com.example.everlookcalendar.repository.StartDateRepo
import com.example.everlookcalendar.repository.ToggleDebugRepo
import com.example.everlookcalendar.repository.TwentyDateRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException


@EnableScheduling
@RestController
@RequestMapping("/conf")
class ConfigController(
    @Autowired private val startDateRepo: StartDateRepo,
    @Autowired val twentyDateRepo: TwentyDateRepo,
    @Autowired val toggleDebugRepo: ToggleDebugRepo,
    private val configRepo: ConfigRepo,
) {
    companion object {
        var toggleState = "off"
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/updateTwentyMan")
    fun updateTwentyManDate(@RequestParam value: String) {
        val twentyDateRaw = twentyDateRepo.findAll().first()
        val twentyDate = LocalDate.parse(twentyDateRaw.date, DateTimeFormatter.ISO_LOCAL_DATE)
        val newDate = TwentyManDate()
        newDate.date = twentyDate.plusDays(value.toInt().toLong()).toString()
        twentyDateRepo.deleteAll()
        twentyDateRepo.save(newDate)
    }

    @PreAuthorize("hasAuthority('ROLE_GUEST')")
    @PostMapping("/testAuth")
    fun testAuth(@RequestBody data: String): String {
        println("AUTHORIZED " + data)
        return data
    }


    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/updateConfig")
    fun updateConfig(@RequestBody configvalues: ConfigValues): ResponseEntity<Any> {
        println("UPDATED CONFIG " + configvalues)
        configRepo.deleteAll()
        configRepo.save(configvalues)

        return ResponseEntity.ok("config updated successfully")
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/getConfig")
    fun getConfig(): ConfigValues {
        println("loading config")
        val config = configRepo.findFirstBy()
        return config
    }


    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/getToggle")
    fun getToggle(): String {
        val temp = toggleDebugRepo.findAll().first()
        val outVal = if (temp.debug) "on" else "off"
        toggleState = outVal
        return outVal
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/setToggle")
    fun setToggle(): String {
        toggleState = if (toggleState == "on") "off" else "on"
        toggleDebugRepo.deleteAll();
        val newToggle = ToggleDebug()
        newToggle.debug = if (toggleState == "on") true else false
        toggleDebugRepo.save(newToggle)
        return toggleState
    }

}
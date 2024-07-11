package com.example.everlookcalendar.controller

import com.example.everlookcalendar.data.StartDate
import com.example.everlookcalendar.data.ToggleDebug
import com.example.everlookcalendar.data.TwentyManDate
import com.example.everlookcalendar.repository.StartDateRepo
import com.example.everlookcalendar.repository.ToggleDebugRepo
import com.example.everlookcalendar.repository.TwentyDateRepo
import kotlinx.coroutines.newSingleThreadContext
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
    @PostMapping("/updateStartDate")
    fun updateStartingDate(@RequestBody dateWrapper: StartDate): ResponseEntity<Any> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dateFormatted: LocalDate?
        try {
           dateFormatted =  LocalDate.parse(dateWrapper.date, formatter)
        } catch (e: DateTimeParseException) {
            return ResponseEntity.badRequest().body("Invalid date format")
        }
        // The emulation starts at 2023-12-01 so anything before that will mess things up.
        if(dateFormatted.isBefore(LocalDate.parse("2023-12-01",formatter)))
            return ResponseEntity.badRequest().body("The date must be after 2023-12-01")

        // making sure only 1 date exists in the database all the time
        startDateRepo.deleteAll()
        startDateRepo.save(dateWrapper)

        return ResponseEntity.ok("Date updated successfully")
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
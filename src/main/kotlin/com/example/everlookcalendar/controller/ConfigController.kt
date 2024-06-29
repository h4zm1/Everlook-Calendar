package com.example.everlookcalendar.controller

import com.example.everlookcalendar.data.StartDate
import com.example.everlookcalendar.data.ToggleDebug
import com.example.everlookcalendar.data.TwentyManDate
import com.example.everlookcalendar.repository.StartDateRepo
import com.example.everlookcalendar.repository.ToggleDebugRepo
import com.example.everlookcalendar.repository.TwentyDateRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter


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

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/testAuth")
    fun testAuth(@RequestBody data: String) {
        println("AUTHORIZED " + data)
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/update")
    fun updateStartingDate(@RequestParam date: String) {
        val newDate = StartDate()
        newDate.date = date
        // making sure only 1 date exists in the database all the time
        startDateRepo.deleteAll()
        startDateRepo.save(newDate)
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
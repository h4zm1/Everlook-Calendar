package com.example.everlookcalendar

import io.github.wimdeblauwe.hsbt.mvc.HxRequest
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@SpringBootApplication
class EverlookCalendarApplication

fun main(args: Array<String>) {
    runApplication<EverlookCalendarApplication>(*args)
}

@RestController
class DemoController {
    @GetMapping("/hello")
    fun hello(): String {
        return "Hello mom"
    }

    @GetMapping("/api/users")
    fun getUsers(): List<User> {
//        return listOf(
//                User("John Doe", "john.doe@example.com"),
//                User("Jane Doe", "jane.doe@example.com")
//        )
        return userList
    }

    @PostMapping("/api/validateName")
    fun validateUserName(@RequestParam name: String): String {
        println(name)
//        Thread.sleep(5000)
        return if (name.length < 3) "Invalid input" else "ggggggggg"
    }

    @GetMapping("/api/time")
    fun getTime(): String {
//        return listOf(
//                User("John Doe", "john.doe@example.com"),
//                User("Jane Doe", "jane.doe@example.com")
//        )
        val currentTime = LocalTime.now().minusHours(1)
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")
        return currentTime.format(formatter)
    }

    @GetMapping("/api/zgboss")
    fun getZgBoss(): String {
        var boss = ""
        val currentDate = LocalDate.now()
        var foundBoss = false
        var searching = true
        eventList.forEach(action = { event ->
            val parseableEventDate = event.date.substring(4, event.date.length)
            if (event.madness > 0 && searching) {
                boss = event.madnessBoss
                foundBoss = true
            }
            val eventDate = LocalDate.parse(parseableEventDate)
            // If today is older than event day or didn't reach an event newer than today
            if (currentDate <= eventDate && searching) {
                event.old = 0
                searching = false
            }

        })

        return boss
    }

    @GetMapping("/api/event")
    fun getEvent(): Event {
        return Event(1, 0, 0, 1, 0, 1, "", "WSG", 1, "xxxxxxxxx")
    }

    @GetMapping("/api/events")
    @HxRequest // Prevent getting called from url directly
    fun getEvents(): List<Event> {
        var changeAll = false
        val currentDate = LocalDate.now()
        eventList.forEach(action = { event ->
            // Decoding pvp string only when their value is default
            // to avoid concatenation of old decoded values
            if (event.pvp.isNotEmpty() && (!event.pvp.contains("start") && !event.pvp.contains("ends")))
                event.pvp = getPvpString(event.pvp)
            val parseableEventDate = event.date.substring(4, event.date.length)
//            println(event.date)
//            println(parseableEventDate)
            val eventDate = LocalDate.parse(parseableEventDate)
            // If an even date is the same or still didn't come
            if (currentDate <= eventDate || changeAll) {
                event.old = 0
                changeAll = true
            }
            // Adding small day name in front of every date
            event.date = eventDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toString() + " " + parseableEventDate
        })
        return eventList
    }
}

fun getPvpString(value: String): String {
    if (value.substring(value.length - 1, value.length) == "s")
        return value.substring(0, value.length - 1).uppercase() + " weekend start"
    else
        return value.substring(0, value.length - 1).uppercase() + " weekend ends"

}

@Controller
@RequestMapping("/")
class HomeController {
    @GetMapping
    fun home(model: Model): String {
        model.addAttribute("users", userList)
        return "home"
    }
}

@Controller
@RequestMapping("/views/users")
class UserViewController {
    //    @GetMapping("/test")
//    @HxRequest
//    fun userList(model: Model): String {
//        model.addAttribute("users", userList)
//        return "test"
//    }
    @GetMapping("/frontend")
//    @HxRequest
    fun userList(model: Model): List<User> {
//        model.addAttribute("users", userList)
        return userList
    }
}

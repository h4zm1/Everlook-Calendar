package com.example.everlookcalendar

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@SpringBootApplication
class EverlookCalendarApplication

fun main(args: Array<String>) {
    runApplication<EverlookCalendarApplication>(*args)
}

@RestController
class DemoController{
    @GetMapping("/hello")
    fun hello():String{
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

    @GetMapping("/api/time")
    fun getTime(): String {
//        return listOf(
//                User("John Doe", "john.doe@example.com"),
//                User("Jane Doe", "jane.doe@example.com")
//        )
        val currentTime = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        return currentTime.format(formatter)
    }
    @GetMapping("/api/event")
    fun getEvent(): Event {
        return Event(1,0,0,1,0,1,"xxxxxxxxx")
    }
    @GetMapping("/api/events")
    fun getEvents(): List<Event> {
        return eventList
    }
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
class UserViewController{
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

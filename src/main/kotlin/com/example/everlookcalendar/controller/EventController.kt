package com.example.everlookcalendar.controller

import com.example.everlookcalendar.data1.Event
import com.example.everlookcalendar.service.EventService
import io.github.wimdeblauwe.hsbt.mvc.HxRequest
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import reactor.core.publisher.Flux
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*


@Controller
class MainController {
    @GetMapping
    @RequestMapping("/")
    fun home(model: Model): String {
        return "raidtime"
    }

    @GetMapping
    @RequestMapping("/zgenchants")
    fun enchants(model: Model): String {

        // This will redirect to zgenchants page
        return "zgenchants"
    }
}

@EnableScheduling
@RestController
class RaidController(private val service: EventService) {

    @GetMapping("/api/time", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getOrderStatus(): SseEmitter {
        val currentTime = LocalTime.now().minusHours(1)
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")
        val sseEmitter = SseEmitter()
        sseEmitter.send(SseEmitter.event().name("message").data(currentTime.format(formatter), MediaType.TEXT_EVENT_STREAM))
        sseEmitter.onError { println("error") }
        sseEmitter.onTimeout {
            sseEmitter.complete()
        }
        return sseEmitter
    }

    // TODO: DELETE
    // For streaming!!!
    @GetMapping("/api/time22", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamEvents(): Flux<ServerSentEvent<String?>?>? {
        val currentTime = LocalTime.now().minusHours(1)
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")
        return Flux.interval(Duration.ofSeconds(1))
                .map {
                    ServerSentEvent.builder<String>()
                            .event("message")
                            .data(currentTime.format(formatter))
                            .build()
                }
    }


    @GetMapping("/api/zgboss")
    fun getZgBoss(): String {
        var boss = ""
        val currentDate = LocalDate.now()
        var foundBoss = false
        var searching = true
//        service.saveEvents(eventList)

            for (event in   service.getAllEvents()) {
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
        }
        return boss
    }


    @GetMapping("/api/events")
    @HxRequest // Prevent getting called from url directly
    fun getEvents(): List<Event> {
        var changeAll = false
        val currentDate = LocalDate.now()
        val eventList: List<Event> = service.getAllEvents().sortedBy { it.date }
//        eventList.forEach(action = { event ->
        for (event in eventList) {
            // Decoding pvp string only when their value is default
            // to avoid concatenation of old decoded values
            if (event.pvp.isNotEmpty() && (!event.pvp.contains("start") && !event.pvp.contains("ends")))
                event.pvp = getPvpString(event.pvp)
            val parseableEventDate = event.date.substring(4, event.date.length)
            val eventDate = LocalDate.parse(parseableEventDate)

            // If an even date is the same or still didn't come
            if (currentDate <= eventDate || changeAll) {
                event.old = 0
                changeAll = true
            }

            // Adding small day name in front of every date
            event.date = eventDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toString() + " " + parseableEventDate
        }
        return eventList
    }
}

fun getPvpString(value: String): String {
    return if (value.substring(value.length - 1, value.length) == "s")
        value.substring(0, value.length - 1).uppercase() + " weekend start"
    else
        value.substring(0, value.length - 1).uppercase() + " weekend ends"

}

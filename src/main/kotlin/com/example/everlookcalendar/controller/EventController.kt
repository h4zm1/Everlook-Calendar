package com.example.everlookcalendar.controller

import com.example.everlookcalendar.data1.Event
import com.example.everlookcalendar.service.EventService
import io.github.wimdeblauwe.hsbt.mvc.HxRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*


@Controller
class MainController {
    @GetMapping
    @RequestMapping("/") // This will be http://localhost:8080/ in local machine
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
class RaidController(private val service: EventService, @Autowired val environment: Environment) {
//    @Autowired
//    private val environment: Environment? = null

    @GetMapping("/api/time", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getOrderStatus(): SseEmitter {

//        val currentTime = LocalTime.now().minusHours(1)
        val currentTime = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")
        val sseEmitter = SseEmitter()
        sseEmitter.send(SseEmitter.event().name("message").data(currentTime.format(formatter), MediaType.TEXT_EVENT_STREAM))
        sseEmitter.onError { println("error") }
        sseEmitter.onTimeout {
            sseEmitter.complete()
        }
        return sseEmitter
    }


    final fun generateEvents(): List<Event> {
        var eventList = mutableListOf<Event>()
        //2 months, we check the month we are in, get the days in it, plus the days in next month, this how long the list is
        //if we are at the last days of the 2nd month w
        val thisMonthDate = LocalDate.now()
        var days = 0
        for (i in 0..2) {
            val nextMonth = thisMonthDate.plusMonths(i.toLong())
            days += nextMonth.lengthOfMonth()
        }

        // these are the first reset days of raids in november 2023
        // TODO:: make a ui for inserting these dates instead of hardcoded
        var zgReset = LocalDate.of(2023, 11, 1)
        var onyReset = LocalDate.of(2023, 11, 4)
        var dayCounter = LocalDate.of(2023, 11, 1)

        val f: NumberFormat = DecimalFormat("00")
        var eventUp = false // tracking if there's events
        var newMonth = false
        var firstFriday = false
        var dmfInMulgor = true
        var dmfUp = false
        var raidUp = false
        var dmfDayCounter = 0
        for (i in 1..days) {
            val event = Event()
            val dmfEvent = Event()

            // if day == first friday in a month then dmf will be the following monday
            if (dayCounter.dayOfMonth == 1)
                newMonth = true
            val dayName = dayCounter.dayOfWeek.name
            if (dayName.equals("FRIDAY") && newMonth)
                firstFriday = true
            if (dayName.equals("MONDAY") && firstFriday) {
                firstFriday = false
                newMonth = false
                eventUp = true
                dmfUp = true
                if (dmfInMulgor) {
                    dmfEvent.dmf = "+mulgore"
                } else {
                    dmfEvent.dmf = "+elwynn"
                }
            }
            if (dmfUp) {
                dmfDayCounter++
                if (dmfDayCounter == 7) {
                    eventUp = true
                    if (dmfInMulgor) {
                        dmfEvent.dmf = "-mulgore"
                        dmfInMulgor = false
                        dmfUp = false
                        dmfDayCounter = 0
                    } else {
                        dmfEvent.dmf = "-elwynn"
                        dmfInMulgor = true
                        dmfUp = false
                        dmfDayCounter = 0

                    }
                }
            }
            // zgReset.get(Calendar.day of month) will return what day of the month the zgReset date corresponds to
            // we compare that value to what day the counter is at right now
            // the zgReset date will get incremented if the condition is met and the day counter is increased at the end bellow.
//            if (zgReset.get(Calendar.DAY_OF_MONTH).equals(dayCounter.get(Calendar.DAY_OF_MONTH))) {
            if (zgReset.dayOfMonth == dayCounter.dayOfMonth) {
                eventUp = true
                raidUp = true
                event.zg = 1
                // 1st day of november is a reset day, so in the 3rd day of november will be another reset
                // 2 days interval
                zgReset =  zgReset.plusDays(3)
            }
            if (onyReset.dayOfMonth == dayCounter.dayOfMonth) {
                eventUp = true
                raidUp = true
                event.ony = 1
                // 4 days interval
                onyReset = onyReset.plusDays(5)
            }
//            val event = Event(0, 0, 0, 0, 1, "mulgore", 0, "", "", 1, "abc 2023-07-01")
            if (eventUp) {
                // Setting up event date
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM")
                val formattedDate = dayCounter.format(formatter)
                // abc is just a placeholder for day of the week
                event.date = "abc $formattedDate-${f.format(dayCounter.dayOfMonth)}"
                event.old = 1
                if (dmfEvent.dmf.length > 2) {
                    dmfEvent.date = "abc $formattedDate-${f.format(dayCounter.dayOfMonth)}"
                    dmfEvent.old = 1
                    eventList.add(dmfEvent)
                }
                if (raidUp)
                    eventList.add(event)
                eventUp = false
                raidUp = false
            }
            dayCounter = dayCounter.plusDays(1)
        }
        return eventList
    }

    @GetMapping("/api/zgboss")
    fun getZgBoss(): String {
        var boss = ""
        val currentDate = LocalDate.now()
        var foundBoss = false
        var searching = true
//        service.saveEvents(eventList)
//        val eventList: List<Event> = service.getAllEvents().sortedBy { it.date }
        val eventList = generateEvents().sortedBy { it.date }
        for (event in eventList) {
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
//        val eventList: List<Event> = service.getAllEvents().sortedBy { it.date }
        val eventList = generateEvents().sortedBy { it.date }

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

            // Setting up dmf
            if (event.dmf.length > 2) {
                if (event.dmf[0].toString().first() == '+') {
                    if (event.dmf.contains("mulgore"))
                        event.dmf = "Darkmoon Faire - Mulgore<br>(not sure about exact time)"
                    else
                        event.dmf = "Darkmoon Faire - Elwynn Forest<br>(not sure about exact time)"
                } else {
                    event.dmf = "Darkmoon Faire ends"
                }
            }

            // Adding small day name in front of every date
            event.date = eventDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toString() + " " + parseableEventDate + " 04:00 ST"
        }
        return eventList
    }
}

fun getPvpString(value: String): String {
    return if (value.substring(value.length - 1, value.length) == "s") value.substring(0, value.length - 1).uppercase() + " weekend start"
    else value.substring(0, value.length - 1).uppercase() + " weekend ends"

}

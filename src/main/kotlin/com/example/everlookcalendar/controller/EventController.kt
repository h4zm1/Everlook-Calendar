package com.example.everlookcalendar.controller

import com.example.everlookcalendar.data.Event
import com.example.everlookcalendar.data.StartDate
import com.example.everlookcalendar.data.TwentyManDate
import com.example.everlookcalendar.repository.StartDateRepo
import com.example.everlookcalendar.repository.TwentyDateRepo
import com.example.everlookcalendar.service.EventService
import io.github.wimdeblauwe.hsbt.mvc.HxRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import javax.swing.text.DateFormatter


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


    @GetMapping("/login")
    fun login(): String {
        return "login"
    }

    @GetMapping
    @RequestMapping("/config")
    fun config(model: Model): String {

        return "config"
    }

}

@EnableScheduling
@RestController
class RaidController(@Autowired private val startDateRepo: StartDateRepo, @Autowired val twentyDateRepo: TwentyDateRepo, @Autowired val environment: Environment) {

    @PostMapping("/api/updateTwentyMan")
    fun updateTwentyManDate(@RequestParam value: String) {
        val twentyDateRaw = twentyDateRepo.findAll().first()
        val twentyDate = LocalDate.parse(twentyDateRaw.date, DateTimeFormatter.ISO_LOCAL_DATE)
        val newDate = TwentyManDate()
        newDate.date = twentyDate.plusDays(value.toInt().toLong()).toString()
        twentyDateRepo.deleteAll()
        twentyDateRepo.save(newDate)
    }

    @PostMapping("/api/update")
    fun updateStartingDate(@RequestParam date: String) {
        val newDate = StartDate()
        newDate.date = date
        // making sure only 1 date exists in the database all the time
        startDateRepo.deleteAll()
        startDateRepo.save(newDate)
    }


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

    enum class Madness {
        Hazzarah, Renataki, Wushoolay, Grilek
    }

    enum class Battlegrounds {
        AV, WSG, AB
    }

    // after user select a starting date in /config, inside the event loop check if we reached that date
    // so the loop will keep  going till it reaches that date
    // when we reach that date a new countdown will start ticking inside the loop
    // and any event that happen starting from now will start getting registered into the final list
    fun generateEvents(): List<Event> {
        // retrieving date from database
        // there's only and can only be 1 date in db
        val startDateFromDb = startDateRepo.findAll().first()
        val twentyDateFromDb = twentyDateRepo.findAll().first()

        val eventList = mutableListOf<Event>()
        var days = 90
        val startingDate = LocalDate.parse(startDateFromDb.date, DateTimeFormatter.ISO_LOCAL_DATE)
        var zgReset = LocalDate.parse(twentyDateFromDb.date, DateTimeFormatter.ISO_LOCAL_DATE)
        var aq20Reset = LocalDate.parse(twentyDateFromDb.date, DateTimeFormatter.ISO_LOCAL_DATE)
        // these are the first reset days of raids in november 2023
        // TODO:: make a ui for inserting these dates instead of hardcoded
        var onyReset = LocalDate.of(2023, 11, 4)
        var bwlReset = LocalDate.of(2023, 11, 22)
        var mcReset = LocalDate.of(2023, 11, 22)
        var aq40Reset = LocalDate.of(2023, 11, 22)
        var naxxReset = LocalDate.of(2023, 11, 22)
        var dayCounter = LocalDate.of(2023, 11, 1)
        var madnessReset = LocalDate.of(2023, 11, 7)

        var madnessIndex = 0
        val f: NumberFormat = DecimalFormat("00")
        var eventUp = false // Tracking if there's events
        var newMonth = false
        var registeringEvents = false
        var firstFriday = false
        var dmfInMulgor = true
        var dmfUp = false
        var dmfOver = false
        var raidUp = false
        var dmfDayCounter = 0
        var pvpWeekend = ""
        var pvpIndex = 0

        while (true) {
            // startingDate is retrieved from database
            // so the loop will keep on going till it reaches that day
            if (dayCounter.isEqual(startingDate))
                registeringEvents = true


            val event = Event()
            val dmfEvent = Event()
            val madnessEvent = Event()
            val pvpEvent = Event()


            // Changing madness bosses every 2 weeks in order
            if (madnessReset.dayOfMonth == dayCounter.dayOfMonth) {
                eventUp = true
                madnessEvent.madness = 1
                madnessEvent.madnessBoss = Madness.entries[madnessIndex].name
                // This will be 0 -> 1 -> 2 -> 3 -> 0 ...
                madnessIndex = (madnessIndex + 1) % Madness.entries.size
                madnessReset = madnessReset.plusDays(14)
            }

            // If dmf in mulgore then day == first friday in a month then dmf will be the following monday
            // if dmf in elwynn then day = first monday of month
            if (dayCounter.dayOfMonth == 1) {
                newMonth = true
                dmfOver= false
            }
            val dayName = dayCounter.dayOfWeek.name
            if (dmfInMulgor) { // if dmf should be in mulgore
                if (dayName.equals("FRIDAY") && newMonth)
                    firstFriday = true
                if (dayName.equals("MONDAY") && firstFriday) {
                    firstFriday = false
                    newMonth = false
                    eventUp = true
                    dmfUp = true
                    dmfEvent.dmf = "+mulgore"
                }
            } else { // if dmf should be in elwynn
                if (dayName.equals("MONDAY") && !dmfOver) {
                    newMonth = false
                    eventUp = true
                    dmfUp = true
                    dmfEvent.dmf = "+elwynn"
                }
            }
            // creating dmf ending event
            if (dmfUp) {
                dmfDayCounter++
                if (dmfDayCounter == 7) {
                    eventUp = true
                    if (dmfInMulgor) {
                        dmfEvent.dmf = "-mulgore"
                        dmfInMulgor = false
                        dmfUp = false
                        dmfDayCounter = 0
                        dmfOver = true
                    } else {
                        dmfEvent.dmf = "-elwynn"
                        dmfInMulgor = true
                        dmfUp = false
                        dmfDayCounter = 0

                    }
                }
            }
            // PVP
            if (dayName.equals("THURSDAY")) {
                eventUp = true
                pvpEvent.pvp = Battlegrounds.entries[pvpIndex].name + "s"
                if (Battlegrounds.entries[pvpIndex].name == "none")
                    pvpEvent.pvp = ""
                pvpWeekend = Battlegrounds.entries[pvpIndex].name
                // This will be 0 -> 1 -> 2 -> 3 -> 0 ...
                pvpIndex = (pvpIndex + 1) % Battlegrounds.entries.size

            }
            if (dayName.equals("MONDAY") && pvpWeekend.isNotEmpty()) {
                eventUp = true
                pvpEvent.pvp = pvpWeekend + "e"
                pvpWeekend = ""
            }
            // zgReset.get(Calendar.day of month) will return what day of the month the zgReset date corresponds to
            // we compare that value to what day the counter is at right now
            // the zgReset date will get incremented if the condition is met and the day counter is increased at the end bellow.
            if (zgReset.dayOfMonth == dayCounter.dayOfMonth) {
                eventUp = true
                raidUp = true
                event.zg = 1
                // 1st day of november is a reset day, so in the 3rd day of november will be another reset
                // 3 days interval
                zgReset = zgReset.plusDays(3)
            }
            if (onyReset.dayOfMonth == dayCounter.dayOfMonth) {
                eventUp = true
                raidUp = true
                event.ony = 1
                // 5 days interval
                onyReset = onyReset.plusDays(5)
            }
            if (aq20Reset.dayOfMonth == dayCounter.dayOfMonth) {
                eventUp = true
                raidUp = true
                event.aq20 = 1
                // 3 days interval
                aq20Reset = aq20Reset.plusDays(3)
            }
            if (bwlReset.dayOfMonth == dayCounter.dayOfMonth) {
                eventUp = true
                raidUp = true
                event.bwl = 1
                // 7 days interval
                bwlReset = bwlReset.plusDays(7)
            }
            if (mcReset.dayOfMonth == dayCounter.dayOfMonth) {
                eventUp = true
                raidUp = true
                event.mc = 1
                // 7 days interval
                mcReset = mcReset.plusDays(7)
            }
            if (aq40Reset.dayOfMonth == dayCounter.dayOfMonth) {
                eventUp = true
                raidUp = true
                event.aq40 = 1
                // 7 days interval
                aq40Reset = aq40Reset.plusDays(7)
            }
            if (naxxReset.dayOfMonth == dayCounter.dayOfMonth) {
                eventUp = true
                raidUp = true
                event.naxx = 1
                // 7 days interval
                naxxReset = naxxReset.plusDays(7)
            }

//            val event = Event(0, 0, 0, 0, 1, "mulgore", 0, "", "", 1, "abc 2023-07-01")

            // this will get triggered only when there's an event
            // this means that some days may end up with no events in them ofc
            // also won't get triggered if the loop didn't reach the starting date
            if (eventUp && registeringEvents) {

                // Setting up event date
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM")
                val formattedDate = dayCounter.format(formatter)
                // abc is just a placeholder for day of the week
                // f.format to force a 1-digit number into 2-digit number
                event.date = "abc $formattedDate-${f.format(dayCounter.dayOfMonth)}"
                event.old = 1
                // Separating dmf entries from the rest
                if (dmfEvent.dmf.length > 2) {
                    dmfEvent.date = "abc $formattedDate-${f.format(dayCounter.dayOfMonth)}"
                    dmfEvent.old = 1
                    eventList.add(dmfEvent)
                }
                // Separating madness entries from the rest
                if (madnessEvent.madnessBoss.isNotEmpty()) {
                    madnessEvent.date = "abc $formattedDate-${f.format(dayCounter.dayOfMonth)}"
                    madnessEvent.old = 1
                    eventList.add(madnessEvent)
                }
                //
                if (pvpEvent.pvp.isNotEmpty()) {
                    pvpEvent.date = "abc $formattedDate-${f.format(dayCounter.dayOfMonth)}"
                    pvpEvent.old = 1
                    eventList.add(pvpEvent)
                }
                if (raidUp)
                    eventList.add(event)
            }
            eventUp = false
            raidUp = false
            // daycounter is how we track what day the loop is at
            dayCounter = dayCounter.plusDays(1)
            // days = 90 so the loop will stop after 90 cycles
            // and the countdown only start ticking after reaching the starting date
            if (registeringEvents) {
                days--
                if (days == -1)
                    break
            }
        }
        return eventList
    }

    @GetMapping("/api/zgboss")
    fun getZgBoss(): String {
        var boss = ""
        val currentDate = LocalDate.now()
        var searching = true
//        service.saveEvents(eventList)
//        val eventList: List<Event> = service.getAllEvents().sortedBy { it.date }
        val eventList = generateEvents().sortedBy { it.date }
        for (event in eventList) {
            val parseableEventDate = event.date.substring(4, event.date.length)
            if (event.madness > 0 && searching) {

                boss = event.madnessBoss
            }
            val eventDate = LocalDate.parse(parseableEventDate)
            // If today is older than event day or didn't reach an event newer than today
            if (currentDate <= eventDate && searching) {
                event.old = 0
                searching = false
            }
        }
        if (boss == "Hazzarah")
            boss = "Hazza'rah"
        if (boss == "Grilek")
            boss = "Gri'lek"
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
                        event.dmf = "Darkmoon Faire - Mulgore"
                    else
                        event.dmf = "Darkmoon Faire - Elwynn Forest"
                } else {
                    event.dmf = "Darkmoon Faire ends"
                }
            }

            // Fixing madness names
            if (event.madnessBoss == "Hazzarah")
                event.madnessBoss = "Hazza'rah"
            if (event.madnessBoss == "Grilek")
                event.madnessBoss = "Gri'lek"

            // Adding small day name in front of every date
            event.date = eventDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toString() + " " + parseableEventDate + " 03:00 ST"
        }
        return eventList
    }
}

fun getPvpString(value: String): String {
    return if (value.substring(value.length - 1, value.length) == "s") value.substring(0, value.length - 1).uppercase() + " weekend start"
    else value.substring(0, value.length - 1).uppercase() + " weekend ends"

}

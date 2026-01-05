package com.example.everlookcalendar.controller

import com.example.everlookcalendar.data.Event
import com.example.everlookcalendar.data.StartDate
import com.example.everlookcalendar.repository.ConfigRepo
import com.example.everlookcalendar.repository.StartDateRepo
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger


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
class RaidController(
    @Autowired private val startDateRepo: StartDateRepo,
    private val configRepo: ConfigRepo
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    // track active connections per ip
    private val activeConnections = ConcurrentHashMap<String, AtomicInteger>()
    // and limit only to 2 connection per ip
    private val maxConnectionsPerIP = 2

    @GetMapping("/api/time", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getServerTime(request: HttpServletRequest): ResponseEntity<SseEmitter> {
        val userKey = request.remoteAddr
        val userConnections = activeConnections.computeIfAbsent(userKey) { AtomicInteger(0) }

        // manual rate limiting
        // ckeck if user has too many active connections
        if (userConnections.get() >= maxConnectionsPerIP) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build()
        }

        // increment active connections
        userConnections.incrementAndGet()

        val formatter = DateTimeFormatter.ofPattern("hh:mm a")
        // 0L will prevent connect from timing out
        val sseEmitter = SseEmitter(0L)
        val executor = Executors.newSingleThreadScheduledExecutor()

        // this will keep the connectio alive and emitting every 1 minute
        executor.scheduleWithFixedDelay({
            try {
                sseEmitter.send(
                    SseEmitter.event()
                        .name("message")
                        .data(LocalTime.now().format(formatter), MediaType.TEXT_EVENT_STREAM)
                )
            } catch (e: Exception) {
                executor.shutdown()
                sseEmitter.complete()
            }
        }, 0, 1, TimeUnit.MINUTES)

        // decrement when connection closes
        val cleanup = {
            userConnections.decrementAndGet()
            executor.shutdown()
        }
        sseEmitter.onCompletion(cleanup)
        sseEmitter.onError { cleanup() }
        sseEmitter.onTimeout { cleanup() }

        return ResponseEntity.ok(sseEmitter)
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

    // we display 90 days on the list
    // maybe save the event list in db? and update it whenever the current day change? maybe this will make it generate faster?
    fun generateEvents(): List<Event> {
        // retrieving date from database
        // there's only and can only be 1 date in db
        //TODO: remove starting date and change it with reset date
        val config = configRepo.findFirstBy()
        val startDateFromDb = config.startDate
//        if (startDateFromDb == null) {
//            startDateFromDb = StartDate(date = LocalDate.now().toString())
//            startDateRepo.save(startDateFromDb)
////            //println("startDateFromDb is ${startDateFromDb.date}")
//        }
//        twentyDateRepo.findAll().first()
//        var dayCounter2 = LocalDate.parse(startDateFromDb.date)
        //println("startDateFromDb is ${startDateFromDb}")
//        //println("startDateFromDb REAL is "+ dayCounter2)

        val eventList = mutableListOf<Event>()
        var days = 90


        var dayCounter = LocalDate.parse(startDateFromDb.take(10))
        var m20ResetDate = LocalDate.now()  // temp value just for init, will get replaced with actual reset date
        var onyResetDate = LocalDate.now()  // temp value just for init, will get replaced with actual reset date
        var madnessReset = LocalDate.now()  // temp value just for init, will get replaced with actual reset date
        var k10ResetDate = LocalDate.now()


        var foundM20 = false // marking the first occurrence of aq20/zg reset day
        var foundOny = false // marking the first occurrence of ony reset day
        var foundMadness = false // marking the first occurrence of madness start day
        var foundDmf = false // marking the first occurrence of madness start day
        var foundK10 = false
        var tempDayHolderForDMF = ""

        var madnessIndex = config.madnessBoss.toInt()
        var madnessBoss = ""
        val f: NumberFormat = DecimalFormat("00")
        var eventUp = false // Tracking if there's events
        var newMonth = false
        var dmfInMulgor = true
        var dmfOver = false
        var raidUp = false
        ""

        while (true) {
            //println("***************************************")
            val event = Event()
            val dmfEvent = Event()
            val madnessEvent = Event()
            Event()

            if (dayCounter.dayOfMonth == 1) {
                newMonth = true
                dmfOver = false
            }
            val dayName = dayCounter.dayOfWeek.name

            if (config.madnessStart.equals(dayName.take(2), ignoreCase = true) && !foundMadness) {
                foundMadness = true
                eventUp = true
                madnessEvent.madness = 1
                madnessEvent.madnessBoss = Madness.entries[madnessIndex].name
                // This will be 0 -> 1 -> 2 -> 3 -> 0 ...
                madnessIndex = (madnessIndex + 1) % Madness.entries.size
                madnessReset = madnessReset.plusDays(14)
                madnessBoss = madnessEvent.madnessBoss
            }
            // Changing madness bosses every 2 weeks in order
            if (madnessReset.dayOfMonth == dayCounter.dayOfMonth && foundMadness) {
                eventUp = true
                madnessEvent.madness = 1
                madnessEvent.madnessBoss = Madness.entries[madnessIndex].name
                // This will be 0 -> 1 -> 2 -> 3 -> 0 ...
                madnessIndex = (madnessIndex + 1) % Madness.entries.size
                madnessReset = madnessReset.plusDays(14)
                madnessBoss = madnessEvent.madnessBoss
            }

            //dmf move out every wednesday
            if (config.dmf.equals(dayName.take(2), ignoreCase = true)) {
                tempDayHolderForDMF = dayCounter.dayOfWeek.plus(1).name
                eventUp = true
                dmfEvent.dmf = "Darkmoon Faire moving out"
            }
            if (tempDayHolderForDMF == dayName && !foundDmf) {
                eventUp = true
                dmfEvent.dmf = config.dmfLocation
                dmfInMulgor = config.dmfLocation != "elwynn"
                foundDmf = true
            }
            if (tempDayHolderForDMF == dayName && foundDmf) {
                eventUp = true
                dmfEvent.dmf = if (dmfInMulgor) "mulgore" else "elwynn"
                dmfInMulgor = !dmfInMulgor
            }


            // PVP
//            if (dayName.equals("THURSDAY")) {
//                eventUp = true
//                pvpEvent.pvp = Battlegrounds.entries[pvpIndex].name + "s"
//                if (Battlegrounds.entries[pvpIndex].name == "none")
//                    pvpEvent.pvp = ""
//                pvpWeekend = Battlegrounds.entries[pvpIndex].name
//                // This will be 0 -> 1 -> 2 -> 3 -> 0 ...
//                pvpIndex = (pvpIndex + 1) % Battlegrounds.entries.size
//
//            }
//            if (dayName.equals("MONDAY") && pvpWeekend.isNotEmpty()) {
//                eventUp = true
//                pvpEvent.pvp = pvpWeekend + "e"
//                pvpWeekend = ""
//            }

            // registering the first reset date based on "next reset" in the ui
            if (config.m20.equals(dayName.take(2), ignoreCase = true) && !foundM20) {
                foundM20 = true
                eventUp = true
                raidUp = true
                event.zg = 1
                event.aq20 = 1
                // 5 days interval
                m20ResetDate = dayCounter.plusDays(3)
                //println(m20ResetDate.dayOfMonth.toString() + "inside M20 " + dayCounter.dayOfMonth)
            }
            if (m20ResetDate.dayOfMonth == dayCounter.dayOfMonth && foundM20) { // apparently straight up comparing doesn't work? so had to do .dayOfMonth
                //println("inside M20")
                eventUp = true
                raidUp = true
                event.zg = 1
                event.aq20 = 1
                m20ResetDate = dayCounter.plusDays(3)
            }
            // same as above but for ony
            if (config.ony.equals(dayName.take(2), ignoreCase = true) && !foundOny) {
                foundOny = true
                eventUp = true
                raidUp = true
                event.ony = 1
                // 5 days interval
                onyResetDate = dayCounter.plusDays(5)
            }
            if (onyResetDate.dayOfMonth == dayCounter.dayOfMonth && foundOny) { // apparently straight up comparing doesn't work? so had to do .dayOfMonth
                //println("inside M20")
                eventUp = true
                raidUp = true
                event.ony = 1
                onyResetDate = dayCounter.plusDays(5)
            }

            // for k10
            if (config.k10.equals(dayName.take(2), ignoreCase = true) && !foundK10) {
                foundK10 = true;
                eventUp = true
                raidUp = true
                event.k10 = 1
                // 5 days interval
                k10ResetDate = dayCounter.plusDays(5)
            }
            if (k10ResetDate.dayOfMonth == dayCounter.dayOfMonth && foundK10) {
                eventUp = true
                raidUp = true
                event.k10 = 1
                k10ResetDate = dayCounter.plusDays(5)
            }

            //  since this resets every 7 days we can just set the exact day
            if (config.m40.equals(dayName.take(2), ignoreCase = true)) {
                //println("inside M40")
                eventUp = true
                raidUp = true
                event.mc = 1
                event.bwl = 1
                event.aq40 = 1
                event.k40 = 1
                event.naxx = 1
                event.es = 1
            }


/////            val event = Event(0, 0, 0, 0, 1, "mulgore", 0, "", "", 1, "abc 2023-07-01")

            // this will get triggered only when there's an event
            // this means that some days may end up with no events in them ofc
            // also won't get triggered if the loop didn't reach the starting date
            if (eventUp) {
                event.madnessBoss = madnessBoss
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
//                // Separating madness entries from the rest
//                if (madnessEvent.madnessBoss.isNotEmpty()) {
////                    madnessEvent.date = "abc $formattedDate-${f.format(dayCounter.dayOfMonth)}"
//                    madnessEvent.old = 1
//                    eventList.add(madnessEvent)
//                }
//                //
//                if (pvpEvent.pvp.isNotEmpty()) {
////                    pvpEvent.date = "abc $formattedDate-${f.format(dayCounter.dayOfMonth)}"
//                    pvpEvent.old = 1
//                    eventList.add(pvpEvent)
//                }
                if (raidUp)
                    eventList.add(event)
            }
            eventUp = false
            raidUp = false

            //println(dayName + "daycounter " + dayCounter)
            //println("evenlist " + eventList.size)


            // daycounter is how we track what day the loop is at
            dayCounter = dayCounter.plusDays(1)
            // days = 90 so the loop will stop after 90 cycles
            // and the countdown only start ticking after reaching the starting date
//            if (registeringEvents) {
//            //println("day "+ (90-days))

            days--
            if (days == 0)
                break
//            }
        }
        return eventList
    }

    @GetMapping("/api/zgboss")
    @Cacheable(value = ["zgboss"])
    fun getZgBoss(): String {
        var boss = ""
        val currentDate = LocalDate.now()
        var searching = true
//        service.saveEvents(eventList)
//        val eventList: List<Event> = service.getAllEvents().sortedBy { it.date }
        val eventList = generateEvents().sortedBy { it.date }
        for (event in eventList) {
            val parseableEventDate = event.date.substring(4, event.date.length)
            if (event.madnessBoss.isNotEmpty() && searching) {

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
    @Cacheable(value = ["events"], key = "T(java.time.LocalDate).now().toString()")
    fun getEvents(): List<Event> {
        logger.info("cache MISS - generating events")  // this should only appear on cached generation
        var changeAll = false
        var idCounter = -1
        val currentDate = LocalDate.now()
//        val eventList: List<Event> = service.getAllEvents().sortedBy { it.date }
        val eventList = generateEvents().sortedBy { it.date }

        for (event in eventList) {
            event.id = idCounter++
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
                if (event.dmf.contains("mulgore"))
                    event.dmf = "Darkmoon Faire - Mulgore"
                else if (event.dmf.contains("elwynn"))
                    event.dmf = "Darkmoon Faire - Elwynn Forest"
                else
                    event.dmf = "DMF moving out"
            }

            // Fixing madness names
            if (event.madnessBoss == "Hazzarah")
                event.madnessBoss = "Hazza'rah"
            if (event.madnessBoss == "Grilek")
                event.madnessBoss = "Gri'lek"

            // Adding small day name in front of every date
            event.date = eventDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                .toString() + " " + parseableEventDate + " 03:00 ST"
        }
        return eventList
    }
}

fun getPvpString(value: String): String {
    return if (value.substring(value.length - 1, value.length) == "s") value.substring(0, value.length - 1)
        .uppercase() + " weekend start"
    else value.substring(0, value.length - 1).uppercase() + " weekend ends"

}

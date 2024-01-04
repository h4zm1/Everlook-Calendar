package com.example.everlookcalendar

import com.example.everlookcalendar.data.Event
import com.example.everlookcalendar.service.EventService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat

@SpringBootTest
class EverlookCalendarApplicationTests(@Autowired private val service: EventService) {

    @Test
    fun contextLoads() {
    }

    @Test
    fun `should be 2 days between zg`() {
        var firstDateInList: LocalDate? = null
        val eventList: List<Event> = service.getAllEvents().sortedBy { it.date }
        for (event in eventList) {
            val parseableEventDate = event.date.substring(4, event.date.length)
            val eventDate = LocalDate.parse(parseableEventDate)
            // We pick and save first ZG date,
            if (firstDateInList == null) {
                if (event.zg == 1) {
                    println("1" + event.id)
                    firstDateInList = eventDate
                }
            }
            // then see the difference between it and the next zg date
            else {
                if (event.zg == 1) {
                    println("2" + event.id)
                    val dayDifference = eventDate.toEpochDay() - firstDateInList.toEpochDay()
                    println("date difference " + dayDifference)
                    firstDateInList = eventDate
                    assertThat(dayDifference).isEqualTo(3)
                }
            }
        }
    }

}

package com.example.everlookcalendar.service

import com.example.everlookcalendar.data.Event
import com.example.everlookcalendar.repository.EventRepo
import org.springframework.stereotype.Service

@Service
//@Transactional
class EventService(private val repository: EventRepo) {

    fun getAllEvents(): List<Event> =
            repository.queryAllEvents()

}
package com.example.everlookcalendar.service

import com.example.everlookcalendar.data.Event
import com.example.everlookcalendar.repository.EventRepo
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class EventService(private val repository: EventRepo) {

    fun getAllEvents(): List<Event> =
            repository.queryAllEvents()

    fun saveEvents(list:List<Event>){
        repository.saveAll(list)
    }
    fun saveEvent(event : Event){
        repository.save(event)
    }
}
package com.example.everlookcalendar.repository

import com.example.everlookcalendar.data.Event
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface EventRepo : JpaRepository<Event, Long> {

    @Query(value = "select * from everevent", nativeQuery = true)
    fun queryAllEvents(): List<Event>


}
package com.example.everlookcalendar.repository

import com.example.everlookcalendar.data.Event
import com.example.everlookcalendar.data.ToggleDebug
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface EventRepo : JpaRepository<Event, Long> {

    @Query(value = "select * from everevent", nativeQuery = true)
    fun queryAllEvents(): List<Event>

}

@Repository
interface ToggleDebugRepo : CrudRepository<ToggleDebug, Int> {

}

package com.example.everlookcalendar


import java.util.*

data class Event(
        val ony: Int,
        val mc: Int,
        val bwl: Int,
        val zg: Int,
        val dmf: Int,
        val old: Int,
        val date: String,
)

var eventList = mutableListOf(
        Event(1,1,1,1,1,0,""),
        Event(0,0,1,1,1,0,""),
        Event(0,1,0,1,1,0,""),
        Event(1,1,1,0,1,0,""),
        Event(1,0,1,1,1,0,""),
        Event(1,1,1,1,1,0,""),
        Event(1,0,1,0,1,0,""),
        Event(0,1,1,1,1,0,""),
        Event(1,1,1,1,1,0,""),
        Event(1,1,1,1,1,0,""),
        Event(1,1,1,1,1,0,""),
        Event(1,1,1,1,1,0,"")
)
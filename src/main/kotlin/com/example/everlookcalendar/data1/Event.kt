package com.example.everlookcalendar.data1


import jakarta.persistence.*
import kotlinx.serialization.Serializable


@Serializable
@Entity
@Table(name = "everevent")
class Event(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Int = -1,
        var ony: Int = 0,
        val mc: Int = 0,
        val bwl: Int= 0,
        var zg: Int= 0,
        var dmf: String= "",
        val madness: Int= 0,
        @Column(name = "madness_boss")
        val madnessBoss: String = "",
        var pvp: String ="",
        var old: Int=0,
        var date: String="",
)

//var eventList = mutableListOf(
//        // Hardcoded for initial testing
//
//        //Jul
//        Event(0,0, 0, 0, 1, 0, 0, "", "", 1, "abc 2023-07-01"),
//        Event(0,1, 0, 0, 0, 0, 0, "", "", 1, "abc 2023-07-02"),
//        Event(0,0, 0, 0, 0, 0, 0, "", "AVe", 1, "abc 2023-07-03"),
//        Event(0,0, 0, 0, 0, 0, 1, "Gri'lek", "", 1, "abc 2023-07-04"),
//        Event(0,0, 0, 0, 1, 0, 0, "", "", 1, "abc 2023-07-04"),
//        Event(0,0, 1, 1, 0, 0, 0, "", "", 1, "abc 2023-07-05"),
//        Event(0,1, 0, 0, 1, 0, 0, "", "", 1, "abc 2023-07-07"),
//        Event(0,0, 0, 0, 0, 0, 0, "", "WSGs", 1, "abc 2023-07-07"),
//        Event(0,0, 0, 0, 0, 0, 0, "", "WSGs", 1, "abc 2023-07-08"),
//        Event(0,0, 0, 0, 1, 0, 0, "", "", 1, "abc 2023-07-10"),
//        Event(0,0, 0, 0, 0, 0, 0, "", "WSGe", 1, "abc 2023-07-10"),
//        Event(0,1, 1, 1, 0, 0, 0, "", "", 1, "abc 2023-07-12"),
//        Event(0,0, 0, 0, 1, 0, 0, "", "", 1, "abc 2023-07-13"),
//        Event(0,0, 0, 0, 0, 0, 0, "", "ABs", 1, "abc 2023-07-14"),
//        Event(0,0, 0, 0, 1, 0, 0, "", "", 1, "abc 2023-07-16"),
//        Event(0,1, 0, 0, 0, 0, 0, "", "", 1, "abc 2023-07-17"),
//        Event(0,0, 0, 0, 0, 0, 0, "", "ABe", 1, "abc 2023-07-17"),
//        Event(0,0, 0, 0, 0, 0, 1, "Hazza'rah", "", 1, "abc 2023-07-18"),
//        Event(0,0, 1, 1, 1, 0, 0, "", "", 1, "abc 2023-07-19"),
//        Event(0,0, 0, 0, 0, 0, 0, "", "AVs", 1, "abc 2023-07-21"),
//        Event(0,1, 0, 0, 1, 0, 0, "", "", 1, "abc 2023-07-22"),
//        Event(0,0, 0, 0, 0, 0, 0, "", "AVe", 1, "abc 2023-07-24"),
//        Event(0,0, 0, 0, 1, 0, 0, "", "", 1, "abc 2023-07-25"),
//        Event(0,0, 1, 1, 0, 0, 0, "", "", 1, "abc 2023-07-26"),
//        Event(0,1, 0, 0, 0, 0, 0, "", "", 1, "abc 2023-07-27"),
//        Event(0,0, 0, 0, 1, 0, 0, "", "", 1, "abc 2023-07-28"),
//        Event(0,0, 0, 0, 0, 0, 0, "", "WSGs", 1, "abc 2023-07-28"),
//        Event(0,0, 0, 0, 1, 0, 0, "", "", 1, "abc 2023-07-31"),
//        Event(0,0, 0, 0, 0, 0, 0, "", "WSGe", 1, "abc 2023-07-31"),
//        //Aug
//        Event(0,0, 0, 0, 0, 0, 1, "Renataki", "", 1, "abc 2023-08-01"),
//        Event(0,1, 0, 0, 0, 0, 0, "", "", 1, "abc 2023-08-01"),
//        Event(0,0, 1, 1, 0, 0, 0, "", "", 1, "abc 2023-08-02"),
//        Event(0,0, 0, 0, 1, 0, 0, "", "", 1, "abc 2023-08-03"),
//        Event(0,0, 0, 0, 0, 0, 0, "", "ABs", 1, "abc 2023-08-04"),
//        Event(0,1, 0, 0, 1, 0, 0, "", "", 1, "abc 2023-08-06"),
//        Event(0,0, 0, 0, 0, 0, 0, "", "ABe", 1, "abc 2023-08-07"),
//        Event(0,0, 1, 1, 1, 0, 0, "", "", 1, "abc 2023-08-09"),
//        Event(0,1, 0, 0, 0, 0, 0, "", "", 1, "abc 2023-08-11"),
//        Event(0,0, 0, 0, 0, 0, 0, "", "AVs", 1, "abc 2023-08-11"),
//        Event(0,0, 0, 0, 1, 0, 0, "", "", 1, "abc 2023-08-12"),
//        Event(0,0, 0, 0, 0, 0, 0, "", "AVe", 1, "abc 2023-08-14"),
//        Event(0,0, 0, 0, 0, 0, 1, "Wushoolay", "", 1, "abc 2023-08-15"),
//        Event(0,0, 0, 0, 1, 0, 0, "", "", 1, "abc 2023-08-15"),
//        Event(0,1, 1, 1, 0, 0, 0, "", "", 1, "abc 2023-08-16"),
//        Event(0,0, 0, 0, 1, 0, 0, "", "", 1, "abc 2023-08-18"),
//        Event(0,0, 0, 0, 0, 0, 0, "", "WSGs", 1, "abc 2023-08-18"),
//        Event(0,1, 0, 0, 1, 0, 0, "", "", 1, "abc 2023-08-21"),
//        Event(0,0, 0, 0, 0, 0, 0, "", "WSGe", 1, "abc 2023-08-21"),
//        Event(0,0, 1, 1, 0, 0, 0, "", "", 1, "abc 2023-08-23"),
//        Event(0,0, 0, 0, 1, 0, 0, "", "", 1, "abc 2023-08-24"),
//        Event(0,0, 0, 0, 0, 0, 0, "", "ABs", 1, "abc 2023-08-25"),
//        Event(0,1, 0, 0, 0, 0, 0, "", "", 1, "abc 2023-08-26"),
//        Event(0,0, 0, 0, 1, 0, 0, "", "", 1, "abc 2023-08-27"),
//        Event(0,0, 0, 0, 0, 0, 0, "", "ABe", 1, "abc 2023-08-28"),
//        Event(0,0, 0, 0, 0, 0, 1, "Gri'lek", "", 1, "abc 2023-08-29"),
//        Event(0,0, 1, 1, 1, 0, 0, "", "", 1, "abc 2023-08-30"),
//        Event(0,1, 0, 0, 0, 0, 0, "", "", 1, "abc 2023-08-31"),
//        //Sep
//        Event(0,0, 0, 0, 0, 0, 0, "", "AVs", 1, "abc 2023-09-01"),
//        Event(0,0, 0, 0, 1, 0, 0, "", "", 1, "abc 2023-09-02"),
//        Event(0,0, 0, 0, 0, 0, 0, "", "AVe", 1, "abc 2023-09-04"),
//        Event(0,1, 0, 0, 1, 0, 0, "", "", 1, "abc 2023-09-05"),
//        Event(0,0, 1, 1, 0, 0, 0, "", "", 1, "abc 2023-09-06"),
//        Event(0,0, 0, 0, 1, 0, 0, "", "", 1, "abc 2023-09-08"),
//        Event(0,0, 0, 0, 0, 0, 0, "", "WSGs", 1, "abc 2023-09-08"),
//        Event(0,1, 0, 0, 0, 0, 0, "", "", 1, "abc 2023-09-10"),
//        Event(0,0, 0, 0, 1, 0, 0, "", "", 1, "abc 2023-09-11"),
//        Event(0,0, 0, 0, 0, 0, 0, "", "WSGe", 1, "abc 2023-09-11"),
//        Event(0,0, 0, 0, 0, 0, 1, "Hazza'rah", "", 1, "abc 2023-09-12"),
//        Event(0,0, 1, 1, 0, 0, 0, "", "", 1, "abc 2023-09-13"),
//        Event(0,0, 0, 0, 1, 0, 0, "", "", 1, "abc 2023-09-14"),
//        Event(0,1, 0, 0, 0, 0, 0, "", "", 1, "abc 2023-09-15"),
//        Event(0,0, 0, 0, 0, 0, 0, "", "ABs", 1, "abc 2023-09-15"),
//        Event(0,0, 0, 0, 1, 0, 0, "", "", 1, "abc 2023-09-17"),
//        Event(0,0, 0, 0, 0, 0, 0, "", "ABe", 1, "abc 2023-09-18"),
//        Event(0,1, 1, 1, 1, 0, 0, "", "", 1, "abc 2023-09-20"),
//        Event(0,0, 0, 0, 0, 0, 0, "", "AVs", 1, "abc 2023-09-22"),
//        Event(0,0, 0, 0, 1, 0, 0, "", "", 1, "abc 2023-09-23"),
//        Event(0,1, 0, 0, 0, 0, 0, "", "", 1, "abc 2023-09-25"),
//        Event(0,0, 0, 0, 0, 0, 0, "", "AVe", 1, "abc 2023-09-25"),
//        Event(0,0, 0, 0, 0, 0, 1, "Renataki", "", 1, "abc 2023-09-26"),
//        Event(0,0, 0, 0, 1, 0, 0, "", "", 1, "abc 2023-09-26"),
//        Event(0,0, 1, 1, 0, 0, 0, "", "", 1, "abc 2023-09-27"),
//        Event(0,0, 0, 0, 1, 0, 0, "", "", 1, "abc 2023-09-29"),
//        Event(0,0, 0, 0, 0, 0, 0, "", "WSGs", 1, "abc 2023-09-29"),
//        Event(0,1, 0, 0, 0, 0, 0, "", "", 1, "abc 2023-09-30"),
//
//        //Oct
////        Event(0, 0, 0, 0, 0, 0, "", "WSGs", 1, "abc 2023-10-06"),
////        Event(0, 0, 0, 0, 0, 0, "", "WSGe", 1, "abc 2023-10-09"),
////        Event(0, 0, 0, 0, 0, 0, "", "ABs", 1, "abc 2023-10-13"),
////        Event(0, 0, 0, 0, 0, 0, "", "ABe", 1, "abc 2023-10-16"),
////        Event(0, 0, 0, 0, 0, 0, "", "AVs", 1, "abc 2023-10-20"),
////        Event(0, 0, 0, 0, 0, 0, "", "AVe", 1, "abc 2023-10-23"),
////        Event(0, 0, 0, 0, 0, 0, "", "AVs", 1, "abc 2023-10-27"),
////        Event(0, 0, 0, 0, 0, 0, "", "AVe", 1, "abc 2023-10-30"),
//
//)
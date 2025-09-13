package com.example.everlookcalendar.data


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
        var mc: Int = 0,
        var bwl: Int= 0,
        var naxx: Int= 0,
        var kara10: Int= 0,
        var kara40: Int= 0,
        var es: Int= 0,
        var zg: Int= 0,
        var aq20: Int=0,
        var aq40: Int=0,
        var dmf: String= "",
        var madness: Int= 0,
        @Column(name = "madness_boss")
        var madnessBoss: String = "",
        var pvp: String ="",
        var old: Int=0,
        var date: String="",
)

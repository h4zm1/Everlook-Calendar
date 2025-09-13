package com.example.everlookcalendar.data


import jakarta.persistence.*


@Entity
@Table(name = "configvalues", schema = "public", catalog = "postgres")
data class ConfigValues(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Int = -1,
        var m40: String = "",
        var m20: String = "",
        var ony: String = "",
        var dmf: String = "",
        var dmfLocation: String = "",
        var madnessBoss: String = "",
        var madnessWeek: String = "",
        var resetTime: String = ""
)

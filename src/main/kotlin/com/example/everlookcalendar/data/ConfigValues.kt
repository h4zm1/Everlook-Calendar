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
//        @Column(name = "\"dmfLocation\"")  // Note the escaped quotes
        var dmfLocation: String = "",
//        @Column(name = "\"madnessBoss\"")
        var madnessBoss: String = "",
//        @Column(name = "\"madnessWeek\"")
        var madnessWeek: String = ""
)

package com.example.everlookcalendar.data


import jakarta.persistence.*


@Entity
@Table(name = "toggledebug", schema = "public", catalog = "postgres")
class ToggleDebug(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Int = -1,
        var debug: Boolean = false
)

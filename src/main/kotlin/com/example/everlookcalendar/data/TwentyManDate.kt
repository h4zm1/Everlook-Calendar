package com.example.everlookcalendar.data


import jakarta.persistence.*


@Entity
@Table(name = "twentymandate", schema = "public", catalog = "postgres")
class TwentyManDate(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Int = -1,
        var date: String = ""
)

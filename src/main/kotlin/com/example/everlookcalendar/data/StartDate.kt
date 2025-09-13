package com.example.everlookcalendar.data


import jakarta.persistence.*


@Entity
@Table(name = "startingdate", schema = "public", catalog = "postgres")
class StartDate(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = -1,
    var date: String = ""
)

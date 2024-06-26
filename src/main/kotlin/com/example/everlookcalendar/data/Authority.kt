package com.example.everlookcalendar.data

import jakarta.persistence.*

@Entity
@Table(name = "authorities")
class Authority(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = -1,
    val name: String
)
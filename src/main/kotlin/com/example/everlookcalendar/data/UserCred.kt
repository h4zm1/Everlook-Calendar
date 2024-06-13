package com.example.everlookcalendar.data

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table


@Entity
@Table(name="usercred")
class UserCred(
    @Id
    val id: Int = -1,
    val email: String,
    val password: String
)
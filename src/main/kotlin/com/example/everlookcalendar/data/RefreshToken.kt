package com.example.everlookcalendar.data

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "refreshtoken")
class RefreshToken(
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    val user: UserCred,
    val token: String,
    @Column(name="expiry_date")
    val expiryDate: Instant
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = -1
}

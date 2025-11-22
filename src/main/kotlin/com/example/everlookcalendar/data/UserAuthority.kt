package com.example.everlookcalendar.data

import jakarta.persistence.*

@Entity
@Table(name = "user_authority")
class UserAuthority(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = -1,
    @ManyToOne
    @JoinColumn(name = "user_id")
    var user: UserCred,
    @ManyToOne
    @JoinColumn(name = "authority_id")
    var authority: Authority
) {
    constructor(user: UserCred, authority: Authority?) : this(-1, user, authority!!)
}
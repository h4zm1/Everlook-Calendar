package com.example.everlookcalendar.data

import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Entity
@Table(name = "usercred")
class UserCred(
    private var email: String = "",
    private var password: String = "",
) : UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = -1

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_authority",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "authority_id")]
    )
    private val authorities: MutableSet<Authority> = mutableSetOf()


    override fun getAuthorities(): Collection<GrantedAuthority> {
        return authorities.map { SimpleGrantedAuthority(it.name) }
    }

    override fun getPassword(): String {
        return password
    }

    /**
     * This actually return the email
     */
    override fun getUsername(): String {
        return email
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }
}
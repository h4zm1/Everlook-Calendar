package com.example.everlookcalendar.service

import com.example.everlookcalendar.data.UserCred
import com.example.everlookcalendar.repository.UserRepo
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service


@Service
interface UserService : UserDetailsService {
    var userRepository: UserRepo

    fun UserService(userRepository: UserRepo) {
        this.userRepository = userRepository
    }

    fun allUsers(): List<UserCred> {
        val users = mutableListOf<UserCred>()

        userRepository.findAll().forEach { users.add(it) }

        return users
    }
}
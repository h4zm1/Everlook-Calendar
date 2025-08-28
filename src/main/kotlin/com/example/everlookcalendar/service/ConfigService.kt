package com.example.everlookcalendar.service

import com.example.everlookcalendar.data.ConfigValues
import com.example.everlookcalendar.data.Event
import com.example.everlookcalendar.repository.ConfigRepo
import com.example.everlookcalendar.repository.EventRepo
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ConfigService(private val repository: ConfigRepo) {

    fun getConfig(): ConfigValues =
            repository.findFirstBy()


    fun saveConfig(config : ConfigValues) {
        repository.save(config)
    }
}
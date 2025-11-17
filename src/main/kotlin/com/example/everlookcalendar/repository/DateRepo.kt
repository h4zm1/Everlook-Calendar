package com.example.everlookcalendar.repository

import com.example.everlookcalendar.data.ConfigValues
import com.example.everlookcalendar.data.StartDate
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface StartDateRepo : CrudRepository<StartDate, Int>


package com.example.everlookcalendar.repository

import com.example.everlookcalendar.data.ConfigValues
import com.example.everlookcalendar.data.TwentyManDate
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface StartDateRepo : CrudRepository<ConfigValues, Int> {

}
@Repository
interface TwentyDateRepo : CrudRepository<TwentyManDate, Int> {

}

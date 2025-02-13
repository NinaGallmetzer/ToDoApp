package com.example.todoapp.utils

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class Common {

    fun getTimeStamp(): String {
        return ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }

}
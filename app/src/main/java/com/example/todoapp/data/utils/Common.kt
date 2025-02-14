package com.example.todoapp.data.utils

import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class Common {

    fun getSupabaseTimeStamp(): String {
        return ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }

    fun getFileTimeStamp(): String {
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        val calendar = Calendar.getInstance()
        return dateTimeFormat.format(calendar.time)
    }

}
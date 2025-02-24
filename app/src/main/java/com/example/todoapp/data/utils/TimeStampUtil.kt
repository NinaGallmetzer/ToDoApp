package com.example.todoapp.data.utils

import android.content.Context
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Calendar
import java.util.Locale

class TimeStampUtil {

    fun getSupabaseTimeStamp(): String {
        return Instant.now().toString()
    }

    fun getFileTimeStamp(): String {
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        val calendar = Calendar.getInstance()
        return dateTimeFormat.format(calendar.time)
    }

    fun getLastSyncTime(context: Context): Instant {
        val sharedPref = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
        val lastSyncString = sharedPref.getString("sync_time_stamp", null)

        return if (lastSyncString != null) {
            Instant.parse(lastSyncString) // Convert back to Instant
        } else {
            Instant.EPOCH // Default to 1970-01-01T00:00:00Z if no sync found
        }
    }

    fun saveNewSyncTime(context: Context, thisSync: String) {
        val sharedPref = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("sync_time_stamp", thisSync) // Save as string
            apply()
        }
    }

}
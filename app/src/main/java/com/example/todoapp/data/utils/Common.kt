package com.example.todoapp.data.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.todoapp.data.models.room.Note
import com.example.todoapp.ui.viewmodels.InjectorUtils
import com.example.todoapp.ui.viewmodels.notes.NotesViewModel
import com.example.todoapp.workers.SyncWorker
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Calendar
import java.util.Locale

class Common {

    fun getSupabaseTimeStamp(): String {
        return Instant.now().toString()
    }

    fun getFileTimeStamp(): String {
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        val calendar = Calendar.getInstance()
        return dateTimeFormat.format(calendar.time)
    }

    fun saveLastFetchTime(context: Context) {
        val sharedPref = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("last_sync_time", getSupabaseTimeStamp()) // Save as string
            apply()
        }
    }

    fun getLastFetchTime(context: Context): Instant {
        val sharedPref = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
        val lastSyncString = sharedPref.getString("last_sync_time", null)

        return if (lastSyncString != null) {
            Instant.parse(lastSyncString) // Convert back to Instant
        } else {
            Instant.EPOCH // Default to 1970-01-01T00:00:00Z if no sync found
        }
    }

    fun startSyncWorker(context: Context) {
        val workRequest = OneTimeWorkRequest.Builder(SyncWorker::class.java).build()
        WorkManager.getInstance(context.applicationContext).enqueue(workRequest)
    }

    @Composable
    fun noteIdToNote(noteId: String): Note {
        val currentContext = LocalContext.current
        val notesViewModel: NotesViewModel = viewModel(factory = InjectorUtils.provideNotesViewModelFactory(
            context = currentContext))
        return notesViewModel.getNoteById(noteId).collectAsState(Note()).value
    }

}
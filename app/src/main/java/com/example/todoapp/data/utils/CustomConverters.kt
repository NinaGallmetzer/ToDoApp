package com.example.todoapp.data.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.TypeConverter
import com.example.todoapp.data.models.room.Note
import com.example.todoapp.ui.viewmodels.InjectorUtils
import com.example.todoapp.ui.viewmodels.notes.NotesAddEditViewModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class CustomConverters {

    private val formatPatterns = listOf(
        "yyyy-MM-dd",
        "EEE MMM dd HH:mm:ss zzz yyyy"
        // Add more format patterns if needed
    )
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    @TypeConverter
    fun dateToString(date: Date): String {
        return dateFormat.format(date)
    }

    @TypeConverter
    fun stringToDate(dateString: String): Date {
        for (pattern in formatPatterns) {
            try {
                val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
                return dateFormat.parse(dateString)
                    ?: throw IllegalArgumentException("Invalid date format: $dateString")
            } catch (e: ParseException) {
                // Continue to the next format pattern
            }
        }
        throw IllegalArgumentException("Unrecognized date format: $dateString")
    }

    @TypeConverter
    @Composable
    fun idToNote(id: String): Note {
        val currentContext = LocalContext.current
        val notesAddEditViewModel: NotesAddEditViewModel = viewModel(factory = InjectorUtils.provideNotesAddEditViewModelFactory(
            context = currentContext, noteId = id))
        return notesAddEditViewModel.note
    }

}
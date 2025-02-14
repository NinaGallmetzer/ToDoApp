package com.example.todoapp.ui.viewmodels

import android.content.Context
import com.example.todoapp.data.ToDoDatabase
import com.example.todoapp.data.repositories.NoteRepository
import com.example.todoapp.data.utils.ExportDbUtil
import com.example.todoapp.ui.viewmodels.notes.NotesAddEditViewModelFactory
import com.example.todoapp.ui.viewmodels.notes.NotesViewModelFactory

object InjectorUtils {
    private fun getNoteRepository(context: Context): NoteRepository {
        return NoteRepository.getInstance(
            ToDoDatabase.getDatabase(context.applicationContext).noteDao(), context.applicationContext)
    }
    fun provideNotesViewModelFactory(context: Context): NotesViewModelFactory {
        val repository = getNoteRepository(context)
        return NotesViewModelFactory(repository)
    }
    fun provideNotesAddEditViewModelFactory(context: Context, noteId: String): NotesAddEditViewModelFactory {
        val repository = getNoteRepository(context)
        return NotesAddEditViewModelFactory(repository, noteId)
    }

    fun provideDataScreenViewModelFactory(exportDbUtil: ExportDbUtil): DataScreenViewModelFactory {
        return DataScreenViewModelFactory(exportDbUtil)
    }

}

@file:Suppress("UNCHECKED_CAST")

package com.example.todoapp.viewmodels.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.todoapp.repositories.NoteRepository

class NotesAddEditViewModelFactory(private val repository: NoteRepository, private val noteId: String): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(NotesAddEditViewModel::class.java)){
            return NotesAddEditViewModel(noteRepository = repository, noteId = noteId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

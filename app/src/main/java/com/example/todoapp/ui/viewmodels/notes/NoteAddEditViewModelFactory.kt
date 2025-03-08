@file:Suppress("UNCHECKED_CAST")

package com.example.todoapp.ui.viewmodels.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.todoapp.data.repositories.NoteRepository

class NoteAddEditViewModelFactory(private val repository: NoteRepository, private val noteId: String): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(NoteAddEditViewModel::class.java)){
            return NoteAddEditViewModel(noteRepository = repository, noteId = noteId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

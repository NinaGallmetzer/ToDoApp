package com.example.todoapp.ui.viewmodels.notes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.models.room.Note
import com.example.todoapp.data.repositories.NoteRepository
import kotlinx.coroutines.launch

class NoteAddEditViewModel(
    private val noteRepository: NoteRepository,
    private val noteId: String = ""
): ViewModel() {

    var note by mutableStateOf(Note())
        private set

    init {
        viewModelScope.launch {
            if (noteId == "") {
                note = Note()
            } else {
                noteRepository.getNoteById(noteId).collect { note ->
                    this@NoteAddEditViewModel.note = note
                }
            }
        }
    }

    fun updateViewNote(note: Note) {
        this.note = note
    }

    suspend fun saveNote(){
        if(noteId == "") {
            noteRepository.addToRoom(note)
        } else {
            noteRepository.updateInRoom(note)
        }
    }
}
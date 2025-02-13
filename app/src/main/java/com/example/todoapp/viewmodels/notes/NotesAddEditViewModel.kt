package com.example.todoapp.viewmodels.notes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.models.room.Note
import com.example.todoapp.repositories.NoteRepository
import kotlinx.coroutines.launch

class NotesAddEditViewModel(
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
                    this@NotesAddEditViewModel.note = note
                }
            }
        }
    }

    fun updateNote(newNote: Note) {
        note = newNote
    }

    suspend fun saveNote(){
        if(noteId == "") {
            noteRepository.add(note)
        } else {
            noteRepository.update(note)
        }
    }
}
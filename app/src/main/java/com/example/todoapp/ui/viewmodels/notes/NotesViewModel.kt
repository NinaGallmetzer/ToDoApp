package com.example.todoapp.ui.viewmodels.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.models.enums.SyncType
import com.example.todoapp.data.models.room.Note
import com.example.todoapp.data.repositories.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotesViewModel(private val noteRepository: NoteRepository) : ViewModel() {

    private val _notes = MutableStateFlow(listOf<Note>())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    init {
        viewModelScope.launch {
            noteRepository.getAllNotes().collect{ notes ->
                _notes.value = notes.filter { it.syncType != SyncType.delete }
            }
        }
    }

    suspend fun markDeletedInRoom(note: Note) {
        noteRepository.markDeletedInRoom(note)
    }

}
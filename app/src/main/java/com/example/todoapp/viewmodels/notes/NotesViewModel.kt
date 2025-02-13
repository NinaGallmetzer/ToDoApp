package com.example.todoapp.viewmodels.notes

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.models.enums.SyncType
import com.example.todoapp.models.room.Note
import com.example.todoapp.repositories.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotesViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _notes = MutableStateFlow(listOf<Note>())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllNotes().collect{ notes ->
                _notes.value = notes.filter { it.syncType != SyncType.delete }
            }
        }
    }

    suspend fun delete(note: Note) {
        repository.delete(note)
    }

    suspend fun syncData(context: Context) {
        repository.updateRoom(context)
        repository.updateSupabase(context)
    }

}
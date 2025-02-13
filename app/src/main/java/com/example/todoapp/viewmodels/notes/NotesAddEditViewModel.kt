package com.example.todoapp.viewmodels.notes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.models.room.Note
import com.example.todoapp.models.supabase.SupabaseNote
import com.example.todoapp.repositories.NoteRepository
import com.example.todoapp.supabase
import com.example.todoapp.utils.Common
import io.github.jan.supabase.postgrest.from
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

    suspend fun saveNoteOnline(){
        if(noteId == "") {
            supabase.from("note").insert(note.toSupabaseNote())
        } else {
            supabase.from("note").update(
                {
                    SupabaseNote::title setTo note.title
                    SupabaseNote::content setTo note.content
                    SupabaseNote::updated_at setTo Common().getTimeStamp()
                }
            ) {
                filter {
                    eq("id", noteId)
                }
            }
        }
    }
}
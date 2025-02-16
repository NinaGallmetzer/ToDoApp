package com.example.todoapp.data.repositories

import android.content.Context
import com.example.todoapp.data.daos.NoteDao
import com.example.todoapp.data.models.enums.SyncType
import com.example.todoapp.data.models.room.Note
import com.example.todoapp.data.models.supabase.SupabaseNote
import com.example.todoapp.supabase
import com.example.todoapp.data.utils.Common
import com.example.todoapp.workers.NetworkChecker
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao, context: Context) {

    private val networkChecker = NetworkChecker(context)

    companion object {
        // For Singleton instantiation
        @Volatile private var instance: NoteRepository? = null

        fun getInstance(dao: NoteDao, context: Context) =
            instance ?: synchronized(this) {
                instance ?: NoteRepository(dao, context = context).also { instance = it }
            }
    }

    fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes()
    }
    fun getNoteById(noteId: String): Flow<Note> {
        return noteDao.getNoteById(noteId)
    }

    suspend fun addToRoom(note: Note) {
        note.syncType = SyncType.add
        noteDao.add(note)
    }

    suspend fun updateInRoom(note: Note) {
        note.syncType = SyncType.update
        noteDao.update(note)
    }

    suspend fun markDeletedInRoom(note: Note) {
        note.syncType = SyncType.delete
        noteDao.update(note)
    }

    private suspend fun fetchUnsyncedNotesSupabase(context: Context): List<SupabaseNote> {
        val lastFetch = Common().getLastFetchTime(context)
        val notes = supabase.from("note")
            .select {
                filter {
                    or {
                        gt("synced_at", lastFetch)
                        exact("synced_at", null)
                    }
                }
            }
            .decodeList<SupabaseNote>()
        Common().saveLastFetchTime(context)
        return notes
    }

    private suspend fun fetchUnsyncedNotesRoom(): List<Note> {
        return noteDao.getUnsyncedNotes()
    }

    private fun mergeNotes(roomNotes: List<Note>, supabaseNotes: List<SupabaseNote>): List<Note> {
        val roomNotesMap = roomNotes
            .associateBy { it.noteId }
        val supabaseNotesMap = supabaseNotes
            .map { it.toRoomNote(SyncType.newFromSupabase) }
            .associateBy { it.noteId }

        val mergedNotes = mutableListOf<Note>()

        // Merge logic
        val allNoteIds = roomNotesMap.keys + supabaseNotesMap.keys

        for (noteId in allNoteIds) {
            val roomNote = roomNotesMap[noteId]
            val supabaseNote = supabaseNotesMap[noteId]

            val mergedNote = when {
                roomNote != null && supabaseNote != null -> {
                    // Conflict resolution: Prioritize roomNote if it has syncType update or delete
                    if (roomNote.syncType == SyncType.update || roomNote.syncType == SyncType.delete) {
                        roomNote
                    } else {
                        supabaseNote
                    }
                }
                roomNote != null -> roomNote
                supabaseNote != null -> supabaseNote
                else -> null
            }

            mergedNote?.let { mergedNotes.add(it) }
        }
        return mergedNotes
    }

    suspend fun syncNotes(context: Context) {
        if (networkChecker.isConnected()) {
            val supabaseNotes = fetchUnsyncedNotesSupabase(context)
            val roomNotes = fetchUnsyncedNotesRoom()
            val notesToSync = mergeNotes(roomNotes, supabaseNotes)
            val syncTime = Common().getSupabaseTimeStamp()
            updateBothDatabases(notesToSync, syncTime)
        }
    }

    private suspend fun updateBothDatabases(notesToSync: List<Note>, syncTime: String) {
        notesToSync.forEach { noteToSync ->
            when (noteToSync.syncType) {
                SyncType.add -> addToSupabaseAndUpdateInRoom(noteToSync, syncTime)
                SyncType.update -> updateInSupabaseAndRoom(noteToSync, syncTime)
                SyncType.delete -> deleteFromSupabaseAndRoom(noteToSync)
                SyncType.synced -> {}
                SyncType.newFromSupabase -> {}
            }
        }
    }

    private suspend fun addToSupabaseAndUpdateInRoom(note: Note, syncTime: String) {
        note.syncedAt = syncTime
        note.syncType = SyncType.synced
        supabase.from("note").insert(note.toSupabaseNote())
        noteDao.update(note)
    }

    private suspend fun updateInSupabaseAndRoom(note: Note, syncTime: String) {
        note.syncedAt = syncTime
        note.syncType = SyncType.synced
        supabase.from("note").update(
            {
                set("title", note.title)
                set("content", note.content)
                set("created_at", note.createdAt)
                set("synced_at", note.syncedAt)
            }
        ) {
            filter {
                eq("note_id", note.noteId)
            }
        }
        noteDao.update(note)
    }

    private suspend fun deleteFromSupabaseAndRoom(note: Note) {
        supabase.from("note").delete {
            filter {
                eq("note_id", note.noteId)
            }
        }
        noteDao.delete(note)
    }
}
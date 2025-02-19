package com.example.todoapp.data.repositories

import android.content.Context
import android.util.Log
import com.example.todoapp.data.daos.NoteDao
import com.example.todoapp.data.models.enums.SyncType
import com.example.todoapp.data.models.room.Note
import com.example.todoapp.data.models.supabase.SupabaseNote
import com.example.todoapp.data.utils.TimeStampUtil
import com.example.todoapp.supabase
import com.example.todoapp.workers.NetworkChecker
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import java.time.OffsetDateTime

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
        note.updatedAt = TimeStampUtil().getSupabaseTimeStamp()
        note.syncType = SyncType.add
        noteDao.add(note)
    }

    suspend fun updateInRoom(note: Note) {
        note.updatedAt = TimeStampUtil().getSupabaseTimeStamp()
        // notes marked add = unsynced with supabase > keep syncType = add
        if (note.syncType == SyncType.add) {
            noteDao.update(note)
        } else {
            note.syncType = SyncType.update
            noteDao.update(note)
        }
    }

    suspend fun markDeletedInRoom(note: Note) {
        note.updatedAt = TimeStampUtil().getSupabaseTimeStamp()
        note.syncType = SyncType.delete
        noteDao.update(note)
    }

    private suspend fun fetchNotesSupabase(): List<SupabaseNote> {
        val notes = supabase.from("note").select()
            .decodeList<SupabaseNote>()
        return notes
    }

    private fun fetchNotesRoom(): List<Note> {
        val notes = noteDao.getAllNotesAsList()
        return notes
    }

    private suspend fun mergeNotes(context: Context, roomNotes: List<Note>, supabaseNotes: List<SupabaseNote>): List<Note> {
        val lastSync = TimeStampUtil().getLastSyncTimeNotes(context)
        Log.d("notesSync", "lastSync: $lastSync")
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
                    val updateRoom = OffsetDateTime.parse(roomNote.updatedAt).toInstant()
                    val updateSupabase = OffsetDateTime.parse(supabaseNote.updatedAt).toInstant()
                    if (updateRoom.isAfter(lastSync)) {
                        if (updateSupabase.isAfter(lastSync)) {
                            if (updateRoom.isAfter(updateSupabase)) {
                                roomNote
                            } else {
                                supabaseNote
                            }
                        } else {
                            roomNote
                        }
                    } else { // if updateRoom isBefore
                        if (updateSupabase.isAfter(lastSync)) {
                            supabaseNote
                        } else {
                            if (updateSupabase.isAfter(updateSupabase)) {
                                roomNote
                            } else {
                                supabaseNote
                            }
                        }
                    }
                }
                roomNote != null -> {
                    val updateRoom = OffsetDateTime.parse(roomNote.updatedAt).toInstant()
                    if (updateRoom.isAfter(lastSync)) {
                        if (roomNote.syncType == SyncType.add) {
                            roomNote
                        } else if (roomNote.syncType == SyncType.update) {
                            roomNote.syncType = SyncType.add
                            roomNote
                        } else if (roomNote.syncType == SyncType.delete) {
                            noteDao.delete(roomNote)
                            null
                        } else {
                            null
                        }
                    } else {
                        noteDao.delete(roomNote)
                        null
                    }

                }
                supabaseNote != null -> supabaseNote
                else -> null
            }

            mergedNote?.let { mergedNotes.add(it) }
        }
        return mergedNotes
    }

    suspend fun syncNotes(context: Context) {
        if (networkChecker.isConnected()) {
            Log.d("notesSync","starting Sync")
            val supabaseNotes = fetchNotesSupabase()
            Log.d("notesSync", "supabaseNotes: $supabaseNotes")
            val roomNotes = fetchNotesRoom()
            Log.d("notesSync", "roomNotes: $roomNotes")
            val notesToSync = mergeNotes(context, roomNotes, supabaseNotes)
            Log.d("notesSync", "mergedNotes: $notesToSync")
            val syncTime = TimeStampUtil().getSupabaseTimeStamp()
            updateBothDatabases(notesToSync, syncTime)
            TimeStampUtil().saveLastSyncTimeNotes(context)
        }
    }

    private suspend fun updateBothDatabases(notesToSync: List<Note>, syncTime: String) {
        notesToSync.forEach { noteToSync ->
            when (noteToSync.syncType) {
                SyncType.add -> addToSupabaseAndUpdateInRoom(noteToSync, syncTime)
                SyncType.update -> updateInSupabaseAndRoom(noteToSync, syncTime)
                SyncType.delete -> deleteFromSupabaseAndRoom(noteToSync)
                SyncType.synced -> {}
                SyncType.newFromSupabase -> addFromSupabaseToRoom(noteToSync, syncTime)
            }
        }
    }

    private suspend fun addToSupabaseAndUpdateInRoom(note: Note, syncTime: String) {
        note.syncedAt = syncTime
        note.syncType = SyncType.synced
        noteDao.update(note)
        supabase.from("note").insert(note.toSupabaseNote())
    }

    private suspend fun updateInSupabaseAndRoom(note: Note, syncTime: String) {
        note.syncedAt = syncTime
        note.syncType = SyncType.synced
        supabase.from("note").update(
            {
                set("title", note.title)
                set("content", note.content)
                set("created_at", note.createdAt)
                set("updated_at", note.updatedAt)
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

    private suspend fun addFromSupabaseToRoom(note: Note, syncTime: String) {
        note.syncedAt = syncTime
        note.syncType = SyncType.synced
        noteDao.add(note)
    }
}
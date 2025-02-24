package com.example.todoapp.data.repositories

import com.example.todoapp.data.daos.NoteDao
import com.example.todoapp.data.models.enums.SyncType
import com.example.todoapp.data.models.room.Note
import com.example.todoapp.data.models.supabase.SupabaseNote
import com.example.todoapp.data.utils.TimeStampUtil
import com.example.todoapp.supabase
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.OffsetDateTime

class NoteRepository(private val noteDao: NoteDao) {

    companion object {
        // For Singleton instantiation
        @Volatile private var instance: NoteRepository? = null

        fun getInstance(dao: NoteDao) =
            instance ?: synchronized(this) {
                instance ?: NoteRepository(dao).also { instance = it }
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

    suspend fun fetchSupabaseNotes(): List<SupabaseNote> {
        val notes = supabase.from("note").select()
            .decodeList<SupabaseNote>()
        return notes
    }

    suspend fun fetchRoomNotes(): List<Note> {
        val notes = noteDao.getAllNotesAsList()
        return notes
    }

    suspend fun mergeNotes(
        roomNotes: List<Note>,
        supabaseNotes: List<SupabaseNote>,
        lastSyncTime: Instant,
    ): List<Note> {
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
                    if (updateRoom.isAfter(lastSyncTime)) {
                        if (updateSupabase.isAfter(lastSyncTime)) {
                            if (updateRoom.isAfter(updateSupabase)) {
                                roomNote
                            } else {
                                supabaseNote
                            }
                        } else {
                            roomNote
                        }
                    } else { // if updateRoom isBefore or equal
                        if (updateSupabase.isAfter(lastSyncTime)) {
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
                    if (updateRoom.isAfter(lastSyncTime)) {
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

    suspend fun updateBothDatabases(notesToSync: List<Note>, newSyncTime: String) {
        notesToSync.forEach { noteToSync ->
            when (noteToSync.syncType) {
                SyncType.add -> addToSupabaseAndUpdateInRoom(noteToSync, newSyncTime)
                SyncType.update -> updateInSupabaseAndRoom(noteToSync, newSyncTime)
                SyncType.delete -> deleteFromSupabaseAndRoom(noteToSync)
                SyncType.synced -> {}
                SyncType.newFromSupabase -> addFromSupabaseToRoom(noteToSync, newSyncTime)
            }
        }
    }

    private suspend fun addToSupabaseAndUpdateInRoom(note: Note, newSyncTime: String) {
        note.syncedAt = newSyncTime
        note.syncType = SyncType.synced
        supabase.from("note").insert(note.toSupabaseNote())
        noteDao.update(note)
    }

    private suspend fun updateInSupabaseAndRoom(note: Note, newSyncTime: String) {
        note.syncedAt = newSyncTime
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

    private suspend fun addFromSupabaseToRoom(note: Note, newSyncTime: String) {
        note.syncedAt = newSyncTime
        note.syncType = SyncType.synced
        noteDao.add(note)
    }
}
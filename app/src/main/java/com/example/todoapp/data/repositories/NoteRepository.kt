package com.example.todoapp.data.repositories

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.todoapp.data.daos.NoteDao
import com.example.todoapp.data.models.enums.SyncType
import com.example.todoapp.data.models.room.Note
import com.example.todoapp.data.models.supabase.SupabaseNote
import com.example.todoapp.supabase
import com.example.todoapp.data.utils.Common
import com.example.todoapp.workers.NetworkChecker
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import java.time.Instant

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

    suspend fun add(note: Note) {
        note.syncType = SyncType.add
        noteDao.add(note)
        if (networkChecker.isConnected()) {
            note.syncedAt = Common().getSupabaseTimeStamp()
            note.syncType = SyncType.synced
            supabase.from("note").insert(note.toSupabaseNote())
            noteDao.update(note)
        }
    }

    suspend fun update(note: Note) {
        note.syncType = SyncType.update
        noteDao.update(note)
        if (networkChecker.isConnected()) {
            note.syncedAt = Common().getSupabaseTimeStamp()
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
    }

    suspend fun delete(note: Note) {
        note.syncType = SyncType.delete
        noteDao.update(note)
        if (networkChecker.isConnected()) {
            supabase.from("note").delete {
                filter {
                    eq("note_id", note.noteId)
                }
            }
            noteDao.delete(note)
        }
    }

    suspend fun updateRoom(context: Context) {
        Common().getLastFetchTime(context)
        val lastSync = Instant.parse("2025-02-14T00:00:00Z") // Example timestamp
        if (networkChecker.isConnected()) {
            supabase.from("note")
                .select {
                    filter {
                        or {
                            gt("synced_at", lastSync)
                            exact("synced_at", null)
                        }
                    }
                }
                .decodeList<SupabaseNote>()
                .forEach { note ->
                    noteDao.add(note.toRoomNote())
                }
            Common().saveLastFetchTime(context)
        } else {
            Toast.makeText(context, "Sync not possible while offline", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun updateSupabase(context: Context) {
        if (networkChecker.isConnected()) {
            val notesToSync = noteDao.getUnsyncedNotes()
            val noOfNotesToSync = notesToSync.size
            if (noOfNotesToSync == 0) {
                Toast.makeText(context, "all notes up-to-date", Toast.LENGTH_SHORT).show()
            } else {
                var noOfNotesSynced = 0
                notesToSync.forEach { noteToSync ->
                    Log.d("noteToSync", noteToSync.noteId + " - " + noteToSync.title)
                    when (noteToSync.syncType) {
                        SyncType.add -> add(noteToSync)
                        SyncType.update -> update(noteToSync)
                        SyncType.delete -> delete(noteToSync)
                        SyncType.synced -> TODO()
                    }
                    noOfNotesSynced += 1
                }
                Toast.makeText(context, "$noOfNotesSynced of $noOfNotesToSync note(s) synced", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Sync not possible while offline", Toast.LENGTH_SHORT).show()
        }
    }
}
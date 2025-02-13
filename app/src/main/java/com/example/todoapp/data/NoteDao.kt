package com.example.todoapp.data

import androidx.room.*
import com.example.todoapp.models.enums.SyncType
import com.example.todoapp.models.room.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM Note")
    fun getAllNotes(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT * FROM Note WHERE noteId=:noteId")
    fun getNoteById(noteId: String): Flow<Note>

    @Query("SELECT * FROM Note WHERE syncType != :syncedType")
    suspend fun getUnsyncedNotes(syncedType: SyncType = SyncType.synced): List<Note>

}
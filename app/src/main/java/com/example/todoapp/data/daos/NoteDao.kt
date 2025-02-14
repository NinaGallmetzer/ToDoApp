package com.example.todoapp.data.daos

import androidx.room.*
import com.example.todoapp.data.models.enums.SyncType
import com.example.todoapp.data.models.room.Note
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
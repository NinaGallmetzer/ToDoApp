package com.example.todoapp.data.daos

import androidx.room.*
import com.example.todoapp.data.models.room.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT * FROM Note WHERE noteId=:noteId")
    fun getNoteById(noteId: String): Flow<Note>

    @Query("SELECT * FROM Note ORDER BY title ASC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM Note")
    suspend fun getAllNotesAsList(): List<Note>

}
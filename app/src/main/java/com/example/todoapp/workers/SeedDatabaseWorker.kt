package com.example.todoapp.workers

import android.content.Context
import androidx.room.RoomDatabase
import com.example.todoapp.data.ToDoDatabase
import com.example.todoapp.data.daos.NoteDao
import com.example.todoapp.data.repositories.NoteRepository

class SeedDatabaseWorker : RoomDatabase.Callback() {
    private lateinit var noteDao: NoteDao
    private lateinit var noteRepository: NoteRepository

    suspend fun seedDatabase(database: ToDoDatabase, context: Context) {
        noteDao = database.noteDao()
        noteRepository = NoteRepository(noteDao, context)
        noteRepository.updateRoom(context)
    }
}

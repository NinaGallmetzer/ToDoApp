package com.example.todoapp.workers

import android.content.Context
import androidx.room.RoomDatabase
import com.example.todoapp.data.*
import com.example.todoapp.repositories.*
import com.example.todoapp.data.ToDoDatabase
import com.example.todoapp.models.room.Note
import java.io.BufferedReader
import java.io.InputStreamReader

class SeedDatabaseWorker : RoomDatabase.Callback() {
    private lateinit var noteDao: NoteDao
    private lateinit var noteRepository: NoteRepository

    suspend fun seedDatabase(database: ToDoDatabase, context: Context) {
        noteDao = database.noteDao()
        noteRepository = NoteRepository(noteDao, context)

        noteRepository.updateRoom(context)
    }
}

fun readCSVFromResources(context: Context, sourceFile: Int, hasHeader: Boolean): List<List<String>> {
    val toDrop = if (hasHeader) 1 else 0
    val reader = BufferedReader(InputStreamReader(context.resources.openRawResource(sourceFile)))
    val lines: List<String> = reader.readLines()
    val csvData: List<List<String>> = lines
        .drop(toDrop)
        .map { line ->
        line.split(",") // Customize this based on your CSV file format
    }

    return csvData
}


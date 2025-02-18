package com.example.todoapp.workers

import androidx.room.RoomDatabase
import com.example.todoapp.data.ToDoDatabase
import com.example.todoapp.data.daos.ItemDao
import com.example.todoapp.data.daos.NoteDao
import com.example.todoapp.data.models.enums.SyncType
import com.example.todoapp.data.models.supabase.SupabaseItem
import com.example.todoapp.data.models.supabase.SupabaseNote
import com.example.todoapp.supabase
import io.github.jan.supabase.postgrest.from

class SeedDatabaseWorker : RoomDatabase.Callback() {
    private lateinit var noteDao: NoteDao
    private lateinit var itemDao: ItemDao

    suspend fun seedDatabase(database: ToDoDatabase) {
        noteDao = database.noteDao()
        itemDao = database.itemDao()
        seedDatabase()
    }

    private suspend fun seedDatabase() {
        supabase.from("note")
            .select()
            .decodeList<SupabaseNote>()
            .forEach { note ->
                noteDao.add(note.toRoomNote(SyncType.synced))
            }
        supabase.from("item")
            .select()
            .decodeList<SupabaseItem>()
            .forEach { item ->
                itemDao.add(item.toRoomItem(SyncType.synced))
            }
    }

}

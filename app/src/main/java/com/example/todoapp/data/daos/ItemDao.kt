package com.example.todoapp.data.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.todoapp.data.models.room.Item
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(item: Item)

    @Update
    suspend fun update(item: Item)

    @Delete
    suspend fun delete(item: Item)

    @Query("SELECT * FROM Item WHERE itemId=:itemId")
    fun getItemById(itemId: String): Flow<Item>

    @Query("SELECT * FROM Item WHERE noteId=:noteId")
    fun getItemsByNoteId(noteId: String): Flow<List<Item>>

    @Query("SELECT * FROM Item WHERE noteId=:noteId AND checked")
    suspend fun getCheckedItemsOfNote(noteId: String): List<Item>

    @Query("SELECT * FROM Item")
    suspend fun getAllItemsAsList(): List<Item>

}
package com.example.todoapp.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.todoapp.data.ToDoDatabase
import com.example.todoapp.data.repositories.ItemRepository
import com.example.todoapp.data.repositories.NoteRepository
import com.example.todoapp.data.utils.TimeStampUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class SyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    private val noteRepository = getNoteRepository(context)
    private val itemRepository = getItemRepository(context)

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                syncData()
                Result.success()
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }

    private suspend fun syncData() {
        coroutineScope {
            val lastSyncTime = TimeStampUtil().getLastSyncTime(applicationContext)
            val newSyncTime = TimeStampUtil().getSupabaseTimeStamp()
            val supabaseNotes = noteRepository.fetchSupabaseNotes()
            Log.d("notesSync", "sbNotes: $supabaseNotes")
            val roomNotes = noteRepository.fetchRoomNotes()
            Log.d("notesSync", "rmNotes: $roomNotes")
            val mergedNotes = noteRepository.mergeNotes(roomNotes, supabaseNotes, lastSyncTime)
            Log.d("notesSync", "mdNotes: $mergedNotes")
            val supabaseItems = itemRepository.fetchSupabaseItems()
            Log.d("itemsSync", "sbItems: $supabaseItems")
            val roomItems = itemRepository.fetchRoomItems()
            Log.d("itemsSync", "rmItems: $roomItems")
            val mergedItems = itemRepository.mergeItems(roomItems, supabaseItems, lastSyncTime)
            Log.d("itemsSync", "mdItems: $mergedItems")
            noteRepository.updateBothDatabases(mergedNotes, newSyncTime)
            itemRepository.updateBothDatabases(mergedItems, newSyncTime)
            TimeStampUtil().saveNewSyncTime(applicationContext, newSyncTime)
        }
    }

    private fun getNoteRepository(context: Context): NoteRepository {
        return NoteRepository.getInstance(
            ToDoDatabase.getDatabase(context.applicationContext).noteDao()
        )
    }

    private fun getItemRepository(context: Context): ItemRepository {
        return ItemRepository.getInstance(
            ToDoDatabase.getDatabase(context.applicationContext).itemDao()
        )
    }

}

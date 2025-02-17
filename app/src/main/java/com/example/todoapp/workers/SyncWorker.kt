package com.example.todoapp.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.todoapp.data.ToDoDatabase
import com.example.todoapp.data.repositories.ItemRepository
import com.example.todoapp.data.repositories.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    private val currentContext = context
    private val noteRepository = getNoteRepository(context)
    private val itemRepository = getItemRepository(context)

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                noteRepository.syncNotes(context = currentContext)
                itemRepository.syncItems(context = currentContext)
                Result.success()
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }

    private fun getNoteRepository(context: Context): NoteRepository {
        return NoteRepository.getInstance(
            ToDoDatabase.getDatabase(context.applicationContext).noteDao(), context.applicationContext)
    }

    private fun getItemRepository(context: Context): ItemRepository {
        return ItemRepository.getInstance(
            ToDoDatabase.getDatabase(context.applicationContext).itemDao(), context.applicationContext)
    }

}

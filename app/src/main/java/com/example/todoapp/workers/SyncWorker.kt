package com.example.todoapp.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.todoapp.data.ToDoDatabase
import com.example.todoapp.data.repositories.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    private val currentContext = context
    private val noteRepository = getNoteRepository(context)

    override suspend fun doWork(): Result {
        Log.d("Test", "Test003")
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
        noteRepository.updateRoom(currentContext)
//        noteRepository.updateSupabase(currentContext) // TODO solve problem using Toast
    }

    private fun getNoteRepository(context: Context): NoteRepository {
        return NoteRepository.getInstance(
            ToDoDatabase.getDatabase(context.applicationContext).noteDao(), context.applicationContext)
    }

}

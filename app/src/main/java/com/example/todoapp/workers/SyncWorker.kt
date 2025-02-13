package com.example.todoapp.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.todoapp.repositories.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncWorker(
    context: Context,
    params: WorkerParameters,
    private val noteRepository: NoteRepository
) : CoroutineWorker(context, params) {

    private val currentContext = context

    override suspend fun doWork(): Result {
        Log.d("testingWorkManager", "running")
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
        noteRepository.updateSupabase(currentContext)
    }

}

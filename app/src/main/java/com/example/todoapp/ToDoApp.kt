package com.example.todoapp

import android.app.Application
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.todoapp.workers.SyncWorker
import java.util.concurrent.TimeUnit

class ToDoApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize WorkManager
//        setupBackgroundSync()
    }

    private fun setupBackgroundSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        // Create the Periodic Work Request
        val syncWorkRequest = PeriodicWorkRequest.Builder(SyncWorker::class.java, 1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setInitialDelay(10, TimeUnit.MINUTES)  // Optional: Set an initial delay before first run
            .build()

        // Enqueue the work request
        WorkManager.getInstance(applicationContext).enqueue(syncWorkRequest)
    }
}

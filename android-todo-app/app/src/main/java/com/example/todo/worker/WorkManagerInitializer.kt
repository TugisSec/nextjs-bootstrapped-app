package com.example.todo.worker

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object WorkManagerInitializer {
    
    fun initialize(context: Context) {
        // Create constraints for the work
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .setRequiresCharging(false)
            .setRequiresDeviceIdle(false)
            .setRequiresStorageNotLow(false)
            .build()
        
        // Create the periodic work request for hourly reminders
        val reminderWorkRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            1, TimeUnit.HOURS,
            15, TimeUnit.MINUTES // Flex interval
        )
            .setConstraints(constraints)
            .addTag("reminder_work")
            .build()
        
        // Enqueue the work with replace policy to avoid duplicates
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "hourly_reminders",
            ExistingPeriodicWorkPolicy.REPLACE,
            reminderWorkRequest
        )
    }
    
    fun cancelReminders(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork("hourly_reminders")
    }
}

package com.example.todo.worker

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.*
import com.example.todo.AlarmActivity
import com.example.todo.data.TaskDatabase
import com.example.todo.utils.RepeatUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        const val WORK_NAME = "reminder_work"
        const val TAG = "ReminderWorker"
        
        fun scheduleHourlyReminders(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(false)
                .build()
            
            val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
                1, TimeUnit.HOURS,
                15, TimeUnit.MINUTES // Flex interval
            )
                .setConstraints(constraints)
                .addTag(TAG)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                reminderRequest
            )
            
            Log.d(TAG, "Hourly reminders scheduled")
        }
        
        fun cancelReminders(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "Reminders cancelled")
        }
    }
    
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Reminder worker started")
                
                val database = TaskDatabase.getDatabase(applicationContext)
                val taskDao = database.taskDao()
                
                // Get overdue tasks
                val overdueTasks = taskDao.getOverdueTasks()
                
                if (overdueTasks.isNotEmpty()) {
                    Log.d(TAG, "Found ${overdueTasks.size} overdue tasks")
                    
                    // Launch alarm activity for reminders
                    launchAlarmActivity()
                    
                    // Handle repeating tasks that might need new occurrences
                    handleRepeatingTasks(taskDao)
                }
                
                // Schedule next reminder
                scheduleNextReminder()
                
                Result.success()
            } catch (e: Exception) {
                Log.e(TAG, "Error in reminder worker", e)
                Result.retry()
            }
        }
    }
    
    private fun launchAlarmActivity() {
        try {
            val intent = Intent(applicationContext, AlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                       Intent.FLAG_ACTIVITY_CLEAR_TOP or
                       Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("trigger_source", "hourly_reminder")
            }
            
            applicationContext.startActivity(intent)
            Log.d(TAG, "Alarm activity launched")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch alarm activity", e)
        }
    }
    
    private suspend fun handleRepeatingTasks(taskDao: com.example.todo.data.TaskDao) {
        try {
            val repeatingTasks = taskDao.getRepeatingTasks()
            
            for (task in repeatingTasks) {
                // Check if task is overdue and should create next occurrence
                if (task.dueDate < System.currentTimeMillis() && !task.isComplete) {
                    val nextOccurrence = RepeatUtils.createNextOccurrence(task)
                    if (nextOccurrence != null) {
                        // Check if next occurrence doesn't already exist
                        val existingTasks = taskDao.getTasksDueInRange(
                            nextOccurrence.dueDate - 60000, // 1 minute before
                            nextOccurrence.dueDate + 60000  // 1 minute after
                        )
                        
                        if (existingTasks.isEmpty()) {
                            taskDao.insertTask(nextOccurrence)
                            Log.d(TAG, "Created next occurrence for repeating task: ${task.title}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling repeating tasks", e)
        }
    }
    
    private fun scheduleNextReminder() {
        val nextReminderTime = RepeatUtils.getNextReminderTime()
        val delay = nextReminderTime - System.currentTimeMillis()
        
        if (delay > 0) {
            val oneTimeRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag(TAG)
                .build()
            
            WorkManager.getInstance(applicationContext).enqueue(oneTimeRequest)
            Log.d(TAG, "Next reminder scheduled in ${delay / 1000 / 60} minutes")
        }
    }
}

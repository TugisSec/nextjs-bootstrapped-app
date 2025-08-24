package com.example.todo.utils

import com.example.todo.data.RepeatType
import com.example.todo.data.RepeatEndType
import com.example.todo.data.TaskEntity
import java.util.*

object RepeatUtils {
    
    fun calculateNextDueDate(task: TaskEntity): Long? {
        if (task.repeatType == RepeatType.NONE) return null
        
        val calendar = Calendar.getInstance().apply {
            timeInMillis = task.dueDate
        }
        
        when (task.repeatType) {
            RepeatType.DAILY -> {
                calendar.add(Calendar.DAY_OF_YEAR, task.repeatInterval)
            }
            RepeatType.WEEKLY -> {
                calendar.add(Calendar.WEEK_OF_YEAR, task.repeatInterval)
            }
            RepeatType.MONTHLY -> {
                calendar.add(Calendar.MONTH, task.repeatInterval)
            }
            RepeatType.CUSTOM -> {
                // Custom interval is treated as days by default
                calendar.add(Calendar.DAY_OF_YEAR, task.repeatInterval)
            }
            RepeatType.NONE -> return null
        }
        
        return calendar.timeInMillis
    }
    
    fun shouldCreateNextOccurrence(task: TaskEntity, currentOccurrenceCount: Int): Boolean {
        when (task.repeatEndType) {
            RepeatEndType.NEVER -> return true
            RepeatEndType.AFTER_COUNT -> {
                return currentOccurrenceCount < task.repeatEndCount
            }
            RepeatEndType.ON_DATE -> {
                val nextDueDate = calculateNextDueDate(task) ?: return false
                val endDate = task.repeatEndDate ?: return false
                return nextDueDate <= endDate
            }
        }
    }
    
    fun createNextOccurrence(task: TaskEntity): TaskEntity? {
        val nextDueDate = calculateNextDueDate(task) ?: return null
        
        return task.copy(
            id = 0, // New task will get auto-generated ID
            dueDate = nextDueDate,
            isComplete = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
    
    fun getRepeatDescription(task: TaskEntity): String {
        return when (task.repeatType) {
            RepeatType.NONE -> "No repeat"
            RepeatType.DAILY -> {
                if (task.repeatInterval == 1) "Daily"
                else "Every ${task.repeatInterval} days"
            }
            RepeatType.WEEKLY -> {
                if (task.repeatInterval == 1) "Weekly"
                else "Every ${task.repeatInterval} weeks"
            }
            RepeatType.MONTHLY -> {
                if (task.repeatInterval == 1) "Monthly"
                else "Every ${task.repeatInterval} months"
            }
            RepeatType.CUSTOM -> {
                "Every ${task.repeatInterval} days"
            }
        }
    }
    
    fun getRepeatEndDescription(task: TaskEntity): String {
        return when (task.repeatEndType) {
            RepeatEndType.NEVER -> "Never ends"
            RepeatEndType.AFTER_COUNT -> "After ${task.repeatEndCount} occurrences"
            RepeatEndType.ON_DATE -> {
                task.repeatEndDate?.let { endDate ->
                    "Until ${DateUtils.formatDate(endDate)}"
                } ?: "Never ends"
            }
        }
    }
    
    fun getAllUpcomingOccurrences(task: TaskEntity, maxOccurrences: Int = 10): List<Long> {
        if (task.repeatType == RepeatType.NONE) return emptyList()
        
        val occurrences = mutableListOf<Long>()
        var currentDate = task.dueDate
        var occurrenceCount = 0
        
        while (occurrences.size < maxOccurrences && occurrenceCount < 100) { // Safety limit
            val nextDate = calculateNextDueDate(task.copy(dueDate = currentDate))
            if (nextDate == null) break
            
            occurrenceCount++
            
            // Check if we should continue based on end conditions
            if (!shouldCreateNextOccurrence(task, occurrenceCount)) break
            
            occurrences.add(nextDate)
            currentDate = nextDate
        }
        
        return occurrences
    }
    
    fun isValidRepeatConfiguration(
        repeatType: RepeatType,
        repeatInterval: Int,
        repeatEndType: RepeatEndType,
        repeatEndCount: Int,
        repeatEndDate: Long?
    ): Boolean {
        // Validate repeat interval
        if (repeatInterval <= 0) return false
        
        // Validate end conditions
        when (repeatEndType) {
            RepeatEndType.AFTER_COUNT -> {
                if (repeatEndCount <= 0) return false
            }
            RepeatEndType.ON_DATE -> {
                if (repeatEndDate == null || repeatEndDate <= System.currentTimeMillis()) {
                    return false
                }
            }
            RepeatEndType.NEVER -> {
                // Always valid
            }
        }
        
        return true
    }
    
    fun getNextReminderTime(currentTime: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentTime
            add(Calendar.HOUR_OF_DAY, 1)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}

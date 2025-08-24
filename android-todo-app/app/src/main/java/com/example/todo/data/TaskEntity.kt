package com.example.todo.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val dueDate: Long, // Timestamp
    val isComplete: Boolean = false,
    val repeatType: RepeatType = RepeatType.NONE,
    val repeatInterval: Int = 1, // For custom intervals (e.g., every 2 days)
    val repeatEndType: RepeatEndType = RepeatEndType.NEVER,
    val repeatEndCount: Int = 0, // Number of occurrences
    val repeatEndDate: Long? = null, // End date timestamp
    val customSoundUri: String? = null, // URI for selected sound
    val category: TaskCategory = TaskCategory.NONE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class RepeatType {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    CUSTOM
}

enum class RepeatEndType {
    NEVER,
    AFTER_COUNT,
    ON_DATE
}

enum class TaskCategory(val displayName: String, val colorRes: String) {
    NONE("No Category", "#9E9E9E"),
    WORK("Work", "#FF5722"),
    PERSONAL("Personal", "#4CAF50"),
    SHOPPING("Shopping", "#FF9800"),
    HEALTH("Health", "#E91E63"),
    OTHER("Other", "#9C27B0")
}

// Extension functions for easier date handling
fun TaskEntity.dueDateAsDate(): Date = Date(dueDate)
fun TaskEntity.createdAtAsDate(): Date = Date(createdAt)
fun TaskEntity.updatedAtAsDate(): Date = Date(updatedAt)
fun TaskEntity.repeatEndDateAsDate(): Date? = repeatEndDate?.let { Date(it) }

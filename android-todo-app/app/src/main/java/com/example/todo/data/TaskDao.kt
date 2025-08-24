package com.example.todo.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE isComplete = 0 ORDER BY dueDate ASC")
    fun getPendingTasks(): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE isComplete = 1 ORDER BY updatedAt DESC")
    fun getCompletedTasks(): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?
    
    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY dueDate ASC")
    fun searchTasks(query: String): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE category = :category ORDER BY dueDate ASC")
    fun getTasksByCategory(category: TaskCategory): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE isComplete = 0 AND dueDate <= :currentTime")
    suspend fun getOverdueTasks(currentTime: Long = System.currentTimeMillis()): List<TaskEntity>
    
    @Query("SELECT * FROM tasks WHERE isComplete = 0 AND dueDate BETWEEN :startTime AND :endTime")
    suspend fun getTasksDueInRange(startTime: Long, endTime: Long): List<TaskEntity>
    
    @Query("SELECT COUNT(*) FROM tasks WHERE isComplete = 1 AND updatedAt BETWEEN :startTime AND :endTime")
    suspend fun getCompletedTasksCount(startTime: Long, endTime: Long): Int
    
    @Query("SELECT COUNT(*) FROM tasks WHERE createdAt BETWEEN :startTime AND :endTime")
    suspend fun getCreatedTasksCount(startTime: Long, endTime: Long): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long
    
    @Update
    suspend fun updateTask(task: TaskEntity)
    
    @Delete
    suspend fun deleteTask(task: TaskEntity)
    
    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)
    
    @Query("UPDATE tasks SET isComplete = :isComplete, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTaskCompletion(id: Long, isComplete: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM tasks WHERE isComplete = 1")
    suspend fun deleteAllCompletedTasks()
    
    @Query("SELECT * FROM tasks WHERE repeatType != 'NONE' AND isComplete = 0")
    suspend fun getRepeatingTasks(): List<TaskEntity>
}

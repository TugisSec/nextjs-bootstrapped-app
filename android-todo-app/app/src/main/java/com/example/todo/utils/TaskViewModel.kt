package com.example.todo.utils

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = TaskDatabase.getDatabase(application)
    private val taskDao = database.taskDao()
    
    // Search and filter state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow<TaskCategory?>(null)
    val selectedCategory: StateFlow<TaskCategory?> = _selectedCategory.asStateFlow()
    
    // Tasks flows
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val pendingTasks: Flow<List<TaskEntity>> = taskDao.getPendingTasks()
    val completedTasks: Flow<List<TaskEntity>> = taskDao.getCompletedTasks()
    
    // Filtered tasks based on search and category
    val filteredTasks: Flow<List<TaskEntity>> = combine(
        allTasks,
        searchQuery,
        selectedCategory
    ) { tasks, query, category ->
        var filtered = tasks
        
        // Filter by search query
        if (query.isNotBlank()) {
            filtered = filtered.filter { task ->
                task.title.contains(query, ignoreCase = true) ||
                task.description.contains(query, ignoreCase = true)
            }
        }
        
        // Filter by category
        if (category != null) {
            filtered = filtered.filter { task ->
                task.category == category
            }
        }
        
        filtered
    }
    
    // Task statistics
    val taskStats: Flow<TaskStats> = combine(
        pendingTasks,
        completedTasks
    ) { pending, completed ->
        TaskStats(
            totalTasks = pending.size + completed.size,
            pendingTasks = pending.size,
            completedTasks = completed.size,
            overdueTasks = pending.count { DateUtils.isOverdue(it.dueDate) }
        )
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun updateSelectedCategory(category: TaskCategory?) {
        _selectedCategory.value = category
    }
    
    fun insertTask(task: TaskEntity) {
        viewModelScope.launch {
            try {
                taskDao.insertTask(task)
                
                // If it's a repeating task, schedule future occurrences
                if (task.repeatType != RepeatType.NONE) {
                    scheduleRepeatingTask(task)
                }
            } catch (e: Exception) {
                // Handle error - could emit to an error state
                e.printStackTrace()
            }
        }
    }
    
    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            try {
                val updatedTask = task.copy(updatedAt = System.currentTimeMillis())
                taskDao.updateTask(updatedTask)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            try {
                taskDao.deleteTask(task)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun toggleTaskCompletion(taskId: Long, isComplete: Boolean) {
        viewModelScope.launch {
            try {
                taskDao.updateTaskCompletion(taskId, isComplete)
                
                // If completing a repeating task, create next occurrence
                if (isComplete) {
                    val task = taskDao.getTaskById(taskId)
                    if (task != null && task.repeatType != RepeatType.NONE) {
                        createNextOccurrence(task)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun getTaskById(id: Long): Flow<TaskEntity?> = flow {
        emit(taskDao.getTaskById(id))
    }
    
    fun deleteAllCompletedTasks() {
        viewModelScope.launch {
            try {
                taskDao.deleteAllCompletedTasks()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun getOverdueTasks() {
        viewModelScope.launch {
            try {
                val overdueTasks = taskDao.getOverdueTasks()
                // Handle overdue tasks - could emit to a state for notifications
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun getTasksForProgressChart(startTime: Long, endTime: Long): Flow<ChartData> = flow {
        try {
            val completedCount = taskDao.getCompletedTasksCount(startTime, endTime)
            val createdCount = taskDao.getCreatedTasksCount(startTime, endTime)
            
            emit(ChartData(
                completedTasks = completedCount,
                createdTasks = createdCount,
                period = "${DateUtils.formatDate(startTime)} - ${DateUtils.formatDate(endTime)}"
            ))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(ChartData(0, 0, "Error"))
        }
    }
    
    private fun scheduleRepeatingTask(task: TaskEntity) {
        viewModelScope.launch {
            try {
                // Get upcoming occurrences
                val upcomingOccurrences = RepeatUtils.getAllUpcomingOccurrences(task, 5)
                
                // Insert next few occurrences
                upcomingOccurrences.forEach { dueDate ->
                    val nextTask = task.copy(
                        id = 0,
                        dueDate = dueDate,
                        isComplete = false,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    taskDao.insertTask(nextTask)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun createNextOccurrence(completedTask: TaskEntity) {
        viewModelScope.launch {
            try {
                val nextTask = RepeatUtils.createNextOccurrence(completedTask)
                if (nextTask != null) {
                    // Check if we should create the next occurrence
                    val existingOccurrences = taskDao.getTasksDueInRange(
                        nextTask.dueDate - 1000, // 1 second before
                        nextTask.dueDate + 1000  // 1 second after
                    )
                    
                    // Only create if no similar task exists
                    if (existingOccurrences.isEmpty()) {
                        taskDao.insertTask(nextTask)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

data class TaskStats(
    val totalTasks: Int,
    val pendingTasks: Int,
    val completedTasks: Int,
    val overdueTasks: Int
)

data class ChartData(
    val completedTasks: Int,
    val createdTasks: Int,
    val period: String
)

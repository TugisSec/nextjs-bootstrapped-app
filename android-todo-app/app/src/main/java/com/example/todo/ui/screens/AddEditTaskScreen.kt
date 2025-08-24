package com.example.todo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todo.data.*
import com.example.todo.utils.DateUtils
import com.example.todo.utils.RepeatUtils
import com.example.todo.utils.TaskViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    viewModel: TaskViewModel,
    taskId: Long?,
    onNavigateBack: () -> Unit
) {
    var task by remember { mutableStateOf<TaskEntity?>(null) }
    var isLoading by remember { mutableStateOf(taskId != null) }
    
    // Form state
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var repeatType by remember { mutableStateOf(RepeatType.NONE) }
    var repeatInterval by remember { mutableStateOf(1) }
    var repeatEndType by remember { mutableStateOf(RepeatEndType.NEVER) }
    var repeatEndCount by remember { mutableStateOf(1) }
    var repeatEndDate by remember { mutableStateOf<Long?>(null) }
    var selectedCategory by remember { mutableStateOf(TaskCategory.NONE) }
    
    // UI state
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showRepeatEndDatePicker by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf<String?>(null) }
    
    // Load existing task if editing
    LaunchedEffect(taskId) {
        if (taskId != null) {
            viewModel.getTaskById(taskId).collect { existingTask ->
                if (existingTask != null) {
                    task = existingTask
                    title = existingTask.title
                    description = existingTask.description
                    dueDate = existingTask.dueDate
                    repeatType = existingTask.repeatType
                    repeatInterval = existingTask.repeatInterval
                    repeatEndType = existingTask.repeatEndType
                    repeatEndCount = existingTask.repeatEndCount
                    repeatEndDate = existingTask.repeatEndDate
                    selectedCategory = existingTask.category
                }
                isLoading = false
            }
        }
    }
    
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            text = if (taskId == null) "Add New Task" else "Edit Task",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Title field
        OutlinedTextField(
            value = title,
            onValueChange = { 
                title = it
                titleError = null
            },
            label = { Text("Title *") },
            modifier = Modifier.fillMaxWidth(),
            isError = titleError != null,
            supportingText = titleError?.let { { Text(it) } }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description field
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Due date and time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = DateUtils.formatDate(dueDate),
                onValueChange = { },
                label = { Text("Due Date") },
                modifier = Modifier.weight(1f),
                readOnly = true,
                trailingIcon = {
                    TextButton(onClick = { showDatePicker = true }) {
                        Text("📅")
                    }
                }
            )
            
            OutlinedTextField(
                value = DateUtils.formatTime(dueDate),
                onValueChange = { },
                label = { Text("Time") },
                modifier = Modifier.weight(1f),
                readOnly = true,
                trailingIcon = {
                    TextButton(onClick = { showTimePicker = true }) {
                        Text("🕐")
                    }
                }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Category selection
        Text(
            text = "Category",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TaskCategory.values().forEach { category ->
                FilterChip(
                    onClick = { selectedCategory = category },
                    label = { 
                        Text(
                            text = category.displayName,
                            fontSize = 12.sp
                        )
                    },
                    selected = selectedCategory == category
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Repeat options
        Text(
            text = "Repeat Options",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Repeat type selection
                RepeatType.values().forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = repeatType == type,
                                onClick = { repeatType = type }
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = repeatType == type,
                            onClick = { repeatType = type }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (type) {
                                RepeatType.NONE -> "No Repeat"
                                RepeatType.DAILY -> "Daily"
                                RepeatType.WEEKLY -> "Weekly"
                                RepeatType.MONTHLY -> "Monthly"
                                RepeatType.CUSTOM -> "Custom"
                            }
                        )
                    }
                }
                
                // Custom interval input
                if (repeatType == RepeatType.CUSTOM) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Every")
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = repeatInterval.toString(),
                            onValueChange = { 
                                it.toIntOrNull()?.let { value ->
                                    if (value > 0) repeatInterval = value
                                }
                            },
                            modifier = Modifier.width(80.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("days")
                    }
                }
                
                // Repeat end options
                if (repeatType != RepeatType.NONE) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "End Repeat",
                        fontWeight = FontWeight.Medium
                    )
                    
                    RepeatEndType.values().forEach { endType ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = repeatEndType == endType,
                                    onClick = { repeatEndType = endType }
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = repeatEndType == endType,
                                onClick = { repeatEndType = endType }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (endType) {
                                    RepeatEndType.NEVER -> "Never"
                                    RepeatEndType.AFTER_COUNT -> "After occurrences"
                                    RepeatEndType.ON_DATE -> "On date"
                                }
                            )
                        }
                    }
                    
                    // Additional inputs based on end type
                    when (repeatEndType) {
                        RepeatEndType.AFTER_COUNT -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 32.dp)
                            ) {
                                Text("After")
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedTextField(
                                    value = repeatEndCount.toString(),
                                    onValueChange = { 
                                        it.toIntOrNull()?.let { value ->
                                            if (value > 0) repeatEndCount = value
                                        }
                                    },
                                    modifier = Modifier.width(80.dp),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("times")
                            }
                        }
                        RepeatEndType.ON_DATE -> {
                            OutlinedTextField(
                                value = repeatEndDate?.let { DateUtils.formatDate(it) } ?: "Select date",
                                onValueChange = { },
                                label = { Text("End Date") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 32.dp),
                                readOnly = true,
                                trailingIcon = {
                                    TextButton(onClick = { showRepeatEndDatePicker = true }) {
                                        Text("📅")
                                    }
                                }
                            )
                        }
                        RepeatEndType.NEVER -> { /* No additional input needed */ }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
            
            Button(
                onClick = {
                    if (validateForm(title) { titleError = it }) {
                        saveTask(
                            viewModel = viewModel,
                            existingTask = task,
                            title = title,
                            description = description,
                            dueDate = dueDate,
                            repeatType = repeatType,
                            repeatInterval = repeatInterval,
                            repeatEndType = repeatEndType,
                            repeatEndCount = repeatEndCount,
                            repeatEndDate = repeatEndDate,
                            category = selectedCategory
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (taskId == null) "Add Task" else "Update Task")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
    
    // Date picker dialogs would go here
    // For simplicity, I'm not implementing the full date/time picker dialogs
    // In a real app, you'd use DatePickerDialog and TimePickerDialog
}

private fun validateForm(
    title: String,
    onTitleError: (String?) -> Unit
): Boolean {
    var isValid = true
    
    if (title.isBlank()) {
        onTitleError("Title is required")
        isValid = false
    } else {
        onTitleError(null)
    }
    
    return isValid
}

private fun saveTask(
    viewModel: TaskViewModel,
    existingTask: TaskEntity?,
    title: String,
    description: String,
    dueDate: Long,
    repeatType: RepeatType,
    repeatInterval: Int,
    repeatEndType: RepeatEndType,
    repeatEndCount: Int,
    repeatEndDate: Long?,
    category: TaskCategory
) {
    val task = if (existingTask != null) {
        existingTask.copy(
            title = title,
            description = description,
            dueDate = dueDate,
            repeatType = repeatType,
            repeatInterval = repeatInterval,
            repeatEndType = repeatEndType,
            repeatEndCount = repeatEndCount,
            repeatEndDate = repeatEndDate,
            category = category,
            updatedAt = System.currentTimeMillis()
        )
    } else {
        TaskEntity(
            title = title,
            description = description,
            dueDate = dueDate,
            repeatType = repeatType,
            repeatInterval = repeatInterval,
            repeatEndType = repeatEndType,
            repeatEndCount = repeatEndCount,
            repeatEndDate = repeatEndDate,
            category = category
        )
    }
    
    if (existingTask != null) {
        viewModel.updateTask(task)
    } else {
        viewModel.insertTask(task)
    }
}

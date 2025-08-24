package com.example.todo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todo.data.TaskCategory
import com.example.todo.data.TaskEntity
import com.example.todo.ui.components.TaskCard
import com.example.todo.utils.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TaskViewModel,
    onNavigateToAddTask: () -> Unit,
    onNavigateToEditTask: (Long) -> Unit
) {
    val filteredTasks by viewModel.filteredTasks.collectAsState(initial = emptyList())
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val taskStats by viewModel.taskStats.collectAsState(
        initial = com.example.todo.utils.TaskStats(0, 0, 0, 0)
    )
    
    var showCompletedTasks by remember { mutableStateOf(false) }
    var showCategoryFilter by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with stats
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "My Tasks",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(
                        label = "Total",
                        value = taskStats.totalTasks.toString(),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    StatItem(
                        label = "Pending",
                        value = taskStats.pendingTasks.toString(),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    StatItem(
                        label = "Completed",
                        value = taskStats.completedTasks.toString(),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (taskStats.overdueTasks > 0) {
                        StatItem(
                            label = "Overdue",
                            value = taskStats.overdueTasks.toString(),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::updateSearchQuery,
            label = { Text("Search tasks...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Filter controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Category filter
            FilterChip(
                onClick = { showCategoryFilter = !showCategoryFilter },
                label = { 
                    Text(
                        text = selectedCategory?.displayName ?: "All Categories",
                        fontSize = 12.sp
                    )
                },
                selected = selectedCategory != null,
                leadingIcon = { Text("🏷️", fontSize = 14.sp) }
            )
            
            // Show completed toggle
            FilterChip(
                onClick = { showCompletedTasks = !showCompletedTasks },
                label = { 
                    Text(
                        text = if (showCompletedTasks) "Hide Completed" else "Show Completed",
                        fontSize = 12.sp
                    )
                },
                selected = showCompletedTasks,
                leadingIcon = { Text("✅", fontSize = 14.sp) }
            )
        }
        
        // Category filter dropdown
        AnimatedVisibility(
            visible = showCategoryFilter,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Filter by Category",
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // All categories option
                        FilterChip(
                            onClick = { 
                                viewModel.updateSelectedCategory(null)
                                showCategoryFilter = false
                            },
                            label = { Text("All", fontSize = 10.sp) },
                            selected = selectedCategory == null
                        )
                        
                        // Individual categories
                        TaskCategory.values().filter { it != TaskCategory.NONE }.forEach { category ->
                            FilterChip(
                                onClick = { 
                                    viewModel.updateSelectedCategory(category)
                                    showCategoryFilter = false
                                },
                                label = { Text(category.displayName, fontSize = 10.sp) },
                                selected = selectedCategory == category
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Task list
        val displayTasks = if (showCompletedTasks) {
            filteredTasks
        } else {
            filteredTasks.filter { !it.isComplete }
        }
        
        if (displayTasks.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📝",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isNotBlank() || selectedCategory != null) {
                            "No tasks match your filters"
                        } else {
                            "No tasks yet"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (searchQuery.isNotBlank() || selectedCategory != null) {
                            "Try adjusting your search or filters"
                        } else {
                            "Tap the + button to add your first task"
                        },
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = displayTasks,
                    key = { task -> task.id }
                ) { task ->
                    TaskCard(
                        task = task,
                        onTaskClick = { onNavigateToEditTask(it.id) },
                        onToggleComplete = viewModel::toggleTaskCompletion,
                        onDeleteTask = viewModel::deleteTask
                    )
                }
                
                // Add some bottom padding for the last item
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = color.copy(alpha = 0.8f)
        )
    }
}

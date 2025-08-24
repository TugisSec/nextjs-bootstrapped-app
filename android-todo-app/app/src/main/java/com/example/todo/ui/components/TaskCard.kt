package com.example.todo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todo.data.TaskEntity
import com.example.todo.data.TaskCategory
import com.example.todo.utils.DateUtils
import com.example.todo.utils.RepeatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: TaskEntity,
    onTaskClick: (TaskEntity) -> Unit,
    onToggleComplete: (Long, Boolean) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // Animation for completion state
    val completionAlpha by animateFloatAsState(
        targetValue = if (task.isComplete) 0.6f else 1.0f,
        animationSpec = tween(300),
        label = "completion_alpha"
    )
    
    val checkboxScale by animateFloatAsState(
        targetValue = if (task.isComplete) 1.2f else 1.0f,
        animationSpec = tween(200),
        label = "checkbox_scale"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(completionAlpha)
            .clickable { onTaskClick(task) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Checkbox for completion
            Box(
                modifier = Modifier
                    .scale(checkboxScale)
                    .clickable {
                        onToggleComplete(task.id, !task.isComplete)
                    }
            ) {
                if (task.isComplete) {
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✓",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = CircleShape,
                        color = Color.Transparent,
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp,
                            MaterialTheme.colorScheme.outline
                        )
                    ) {}
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Task content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.isComplete) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Description (if not empty)
                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textDecoration = if (task.isComplete) TextDecoration.LineThrough else null,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Due date and repeat info
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Due date
                    val dueDateColor = when {
                        task.isComplete -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        DateUtils.isOverdue(task.dueDate) -> MaterialTheme.colorScheme.error
                        DateUtils.isToday(task.dueDate) -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    }
                    
                    Text(
                        text = "${DateUtils.getRelativeDateString(task.dueDate)} ${DateUtils.formatTime(task.dueDate)}",
                        fontSize = 12.sp,
                        color = dueDateColor,
                        fontWeight = if (DateUtils.isOverdue(task.dueDate) && !task.isComplete) FontWeight.SemiBold else FontWeight.Normal
                    )
                    
                    // Repeat indicator
                    if (task.repeatType.name != "NONE") {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "🔄",
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                // Category indicator
                if (task.category != TaskCategory.NONE) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = getCategoryColor(task.category).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = task.category.displayName,
                            fontSize = 10.sp,
                            color = getCategoryColor(task.category),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            // Delete button
            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.size(24.dp)
            ) {
                Text(
                    text = "🗑️",
                    fontSize = 16.sp
                )
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete \"${task.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTask(task)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun getCategoryColor(category: TaskCategory): Color {
    return when (category) {
        TaskCategory.WORK -> Color(0xFFFF5722)
        TaskCategory.PERSONAL -> Color(0xFF4CAF50)
        TaskCategory.SHOPPING -> Color(0xFFFF9800)
        TaskCategory.HEALTH -> Color(0xFFE91E63)
        TaskCategory.OTHER -> Color(0xFF9C27B0)
        TaskCategory.NONE -> Color.Gray
    }
}

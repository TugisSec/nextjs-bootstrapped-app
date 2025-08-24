package com.example.todo.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("todo_settings", Context.MODE_PRIVATE) }
    
    var isDarkMode by remember { 
        mutableStateOf(prefs.getBoolean("dark_mode", false))
    }
    var customSoundUri by remember { 
        mutableStateOf(prefs.getString("custom_alarm_sound", null))
    }
    var reminderEnabled by remember { 
        mutableStateOf(prefs.getBoolean("reminders_enabled", true))
    }
    
    // Sound picker launcher
    val soundPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            // Grant persistent permission
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            
            customSoundUri = it.toString()
            prefs.edit().putString("custom_alarm_sound", it.toString()).apply()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            text = "Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // App Preferences Section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "App Preferences",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Dark Mode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Dark Mode",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Switch between light and dark themes",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { 
                            isDarkMode = it
                            prefs.edit().putBoolean("dark_mode", it).apply()
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Reminder Settings Section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Reminder Settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Enable Reminders Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Enable Reminders",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Receive hourly reminders for pending tasks",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { 
                            reminderEnabled = it
                            prefs.edit().putBoolean("reminders_enabled", it).apply()
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Custom Sound Selection
                Column {
                    Text(
                        text = "Alarm Sound",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Choose a custom sound for reminders",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                soundPickerLauncher.launch(arrayOf("audio/*"))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🎵 Select Sound")
                        }
                        
                        if (customSoundUri != null) {
                            OutlinedButton(
                                onClick = {
                                    customSoundUri = null
                                    prefs.edit().remove("custom_alarm_sound").apply()
                                }
                            ) {
                                Text("🗑️")
                            }
                        }
                    }
                    
                    if (customSoundUri != null) {
                        Text(
                            text = "✅ Custom sound selected",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else {
                        Text(
                            text = "Using default alarm sound",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Data Management Section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Data Management",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Clear Completed Tasks
                OutlinedButton(
                    onClick = {
                        // This would need to be connected to the ViewModel
                        // For now, it's just a placeholder
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🗑️ Clear All Completed Tasks")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "This will permanently delete all completed tasks",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // About Section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "About",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("App Version")
                    Text("1.0.0")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Build")
                    Text("Release")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Back Button
        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("← Back to Tasks")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

package com.example.todo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.todo.ui.screens.AddEditTaskScreen
import com.example.todo.ui.screens.HomeScreen
import com.example.todo.ui.screens.SettingsScreen
import com.example.todo.ui.theme.TodoAppTheme
import com.example.todo.utils.TaskViewModel
import com.example.todo.worker.WorkManagerInitializer

class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, initialize WorkManager
            WorkManagerInitializer.initialize(this)
        } else {
            // Permission denied, show explanation or redirect to settings
            showPermissionDialog()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check and request necessary permissions
        checkPermissions()
        
        setContent {
            TodoAppTheme {
                TodoApp()
            }
        }
    }
    
    private fun checkPermissions() {
        // Check for system alert window permission (for full-screen alarms)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
        }
        
        // Check for notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                    WorkManagerInitializer.initialize(this)
                }
                else -> {
                    // Request permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // For older versions, initialize directly
            WorkManagerInitializer.initialize(this)
        }
    }
    
    private fun showPermissionDialog() {
        // You could show a dialog explaining why permissions are needed
        // For now, just initialize WorkManager anyway
        WorkManagerInitializer.initialize(this)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoApp() {
    val navController = rememberNavController()
    val taskViewModel: TaskViewModel = viewModel()
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Text(text = item.icon, fontSize = 20.sp) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = taskViewModel,
                    onNavigateToAddTask = {
                        navController.navigate("add_task")
                    },
                    onNavigateToEditTask = { taskId ->
                        navController.navigate("edit_task/$taskId")
                    }
                )
            }
            
            composable("add_task") {
                AddEditTaskScreen(
                    viewModel = taskViewModel,
                    taskId = null,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable("edit_task/{taskId}") { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId")?.toLongOrNull()
                AddEditTaskScreen(
                    viewModel = taskViewModel,
                    taskId = taskId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable("settings") {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val icon: String,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem("home", "📋", "Tasks"),
    BottomNavItem("add_task", "➕", "Add Task"),
    BottomNavItem("settings", "⚙️", "Settings")
)

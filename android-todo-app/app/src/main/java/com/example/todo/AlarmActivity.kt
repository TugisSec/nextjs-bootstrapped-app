package com.example.todo

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todo.data.TaskDatabase
import com.example.todo.ui.theme.TodoAppTheme
import com.example.todo.worker.ReminderWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AlarmActivity : ComponentActivity() {
    
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set up window flags for full-screen alarm
        setupWindowFlags()
        
        // Initialize vibrator
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        
        setContent {
            TodoAppTheme {
                AlarmScreen(
                    onSnooze = { handleSnooze() },
                    onDismiss = { handleDismiss() }
                )
            }
        }
        
        // Start alarm sound and vibration
        startAlarmEffects()
    }
    
    private fun setupWindowFlags() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        
        // For Android 10+ (API 29+)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
    }
    
    private fun startAlarmEffects() {
        // Start sound
        startAlarmSound()
        
        // Start vibration
        startVibration()
    }
    
    private fun startAlarmSound() {
        try {
            // Get custom sound URI from preferences or use default
            val soundUri = getCustomSoundUri() ?: getDefaultAlarmSound()
            
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                setDataSource(this@AlarmActivity, soundUri)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to default ringtone
            try {
                val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(this@AlarmActivity, defaultUri)
                    isLooping = true
                    prepare()
                    start()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
    
    private fun startVibration() {
        try {
            val vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
            val vibrationEffect = VibrationEffect.createWaveform(vibrationPattern, 0)
            vibrator?.vibrate(vibrationEffect)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun getCustomSoundUri(): Uri? {
        // Get custom sound from SharedPreferences
        val prefs = getSharedPreferences("todo_settings", Context.MODE_PRIVATE)
        val customSoundUriString = prefs.getString("custom_alarm_sound", null)
        return customSoundUriString?.let { Uri.parse(it) }
    }
    
    private fun getDefaultAlarmSound(): Uri {
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    }
    
    private fun handleSnooze() {
        stopAlarmEffects()
        
        // Schedule snooze (10 minutes)
        val snoozeDelay = 10 * 60 * 1000L // 10 minutes
        ReminderWorker.scheduleHourlyReminders(this)
        
        finish()
    }
    
    private fun handleDismiss() {
        stopAlarmEffects()
        finish()
    }
    
    private fun stopAlarmEffects() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        
        vibrator?.cancel()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopAlarmEffects()
    }
    
    override fun onBackPressed() {
        // Prevent back button from dismissing alarm
        // User must explicitly snooze or dismiss
    }
}

@Composable
fun AlarmScreen(
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Animation for flashing background
    val infiniteTransition = rememberInfiniteTransition(label = "alarm_flash")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_animation"
    )
    
    // Get pending tasks count
    var pendingTasksCount by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(Unit) {
        try {
            val database = TaskDatabase.getDatabase(context)
            val overdueTasks = database.taskDao().getOverdueTasks()
            pendingTasksCount = overdueTasks.size
        } catch (e: Exception) {
            pendingTasksCount = 0
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Red.copy(alpha = alpha))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Alarm icon or animation
            Card(
                modifier = Modifier
                    .size(120.dp)
                    .alpha(alpha),
                shape = RoundedCornerShape(60.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⏰",
                        fontSize = 48.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Main message
            Text(
                text = stringResource(R.string.task_reminder),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Pending tasks message
            Text(
                text = if (pendingTasksCount > 0) {
                    "You have $pendingTasksCount pending task${if (pendingTasksCount > 1) "s" else ""}!"
                } else {
                    stringResource(R.string.you_have_pending_tasks)
                },
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Snooze button
                Button(
                    onClick = onSnooze,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.9f),
                        contentColor = Color.Red
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = stringResource(R.string.snooze),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Dismiss button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Red
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = stringResource(R.string.dismiss),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Instructions
            Text(
                text = "Tap Snooze for 10 minutes or Dismiss to stop reminders",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

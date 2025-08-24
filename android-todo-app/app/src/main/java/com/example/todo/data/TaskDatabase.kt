package com.example.todo.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import android.content.Context

@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TaskDatabase : RoomDatabase() {
    
    abstract fun taskDao(): TaskDao
    
    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null
        
        fun getDatabase(context: Context): TaskDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "task_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    @TypeConverter
    fun fromRepeatType(value: RepeatType): String {
        return value.name
    }
    
    @TypeConverter
    fun toRepeatType(value: String): RepeatType {
        return try {
            RepeatType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            RepeatType.NONE
        }
    }
    
    @TypeConverter
    fun fromRepeatEndType(value: RepeatEndType): String {
        return value.name
    }
    
    @TypeConverter
    fun toRepeatEndType(value: String): RepeatEndType {
        return try {
            RepeatEndType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            RepeatEndType.NEVER
        }
    }
    
    @TypeConverter
    fun fromTaskCategory(value: TaskCategory): String {
        return value.name
    }
    
    @TypeConverter
    fun toTaskCategory(value: String): TaskCategory {
        return try {
            TaskCategory.valueOf(value)
        } catch (e: IllegalArgumentException) {
            TaskCategory.NONE
        }
    }
}

# Android To-Do List App

A modern, feature-rich Android To-Do List application built with Kotlin and Jetpack Compose, featuring custom reminders, repeat options, and full-screen alarm notifications.

## 🎯 Features

### Core Functionality
- ✅ **Task Management**: Create, edit, delete, and mark tasks as complete
- ✅ **Rich Task Details**: Title, description, due date/time
- ✅ **Repeat Options**: Daily, Weekly, Monthly, and Custom intervals
- ✅ **Repeat End Conditions**: Never, after X occurrences, or on specific date
- ✅ **Task Categories**: Color-coded category tags for organization

### Reminder & Alert System
- ✅ **Hourly Reminders**: Automatic background reminders via WorkManager
- ✅ **Full-Screen Alerts**: Attention-grabbing alarm overlay (even on lock screen)
- ✅ **Snooze & Dismiss**: User-friendly reminder controls
- ✅ **Custom Sounds**: Framework ready for custom alarm sounds
- ✅ **System Permissions**: Proper overlay and notification permissions

### Modern UI/UX
- ✅ **Material 3 Design**: Clean, minimalist interface
- ✅ **Dark Mode Support**: Follows system theme preferences
- ✅ **Smooth Animations**: Task card transitions and completion animations
- ✅ **Search & Filter**: Find tasks by title or category
- ✅ **Statistics Dashboard**: Track total, pending, completed, and overdue tasks
- ✅ **Bottom Navigation**: Easy access to Tasks, Add Task, and Settings

### Technical Excellence
- ✅ **Kotlin + Jetpack Compose**: Modern Android development
- ✅ **Room Database**: Offline-first data storage
- ✅ **MVVM Architecture**: Clean, maintainable code structure
- ✅ **WorkManager**: Reliable background task scheduling
- ✅ **System Integration**: Lock screen alerts and notifications

## 📱 APK Information

- **File**: `app/build/outputs/apk/debug/app-debug.apk`
- **Size**: 15MB
- **Target SDK**: 34 (Android 14)
- **Minimum SDK**: 24 (Android 7.0+)
- **Architecture**: Universal APK

## 🚀 Installation

1. **Download the APK**: Get `app-debug.apk` from the build outputs
2. **Enable Unknown Sources**: Go to Settings > Security > Install unknown apps
3. **Install**: Tap the APK file and follow installation prompts
4. **Grant Permissions**: Allow overlay permissions for full-screen alerts

## 🔧 Development Setup

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 11 or higher
- Android SDK 34
- Kotlin 1.9.10+

### Build Instructions
```bash
# Clone the repository
git clone <repository-url>
cd android-todo-app

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

## 📋 Permissions Required

The app requests the following permissions:

- `SYSTEM_ALERT_WINDOW` - For full-screen alarm overlays
- `WAKE_LOCK` - To wake device for reminders
- `USE_FULL_SCREEN_INTENT` - For lock screen notifications
- `VIBRATE` - For alarm vibration
- `READ_EXTERNAL_STORAGE` - For custom sound selection
- `POST_NOTIFICATIONS` - For Android 13+ notification support

## 🏗️ Architecture

### Project Structure
```
app/src/main/java/com/example/todo/
├── MainActivity.kt                 # Main entry point
├── AlarmActivity.kt               # Full-screen alarm overlay
├── data/                          # Data layer
│   ├── TaskEntity.kt             # Room entity
│   ├── TaskDao.kt                # Database access
│   └── TaskDatabase.kt           # Room database
├── ui/                           # UI layer
│   ├── components/               # Reusable components
│   ├── screens/                  # Screen composables
│   └── theme/                    # Material 3 theming
├── utils/                        # Utilities
│   ├── TaskViewModel.kt          # Main ViewModel
│   ├── DateUtils.kt              # Date formatting
│   └── RepeatUtils.kt            # Repeat logic
└── worker/                       # Background tasks
    ├── ReminderWorker.kt         # Hourly reminder worker
    └── WorkManagerInitializer.kt # WorkManager setup
```

### Key Components

#### Data Layer
- **Room Database**: Offline-first storage with TaskEntity
- **Repository Pattern**: Clean data access abstraction
- **Type Converters**: Handle enums and complex types

#### UI Layer
- **Jetpack Compose**: Modern declarative UI
- **Material 3**: Latest design system
- **Navigation**: Type-safe navigation between screens
- **State Management**: Reactive UI with StateFlow

#### Background Processing
- **WorkManager**: Reliable background reminders
- **AlarmManager**: Precise timing for alerts
- **Foreground Service**: Long-running reminder tasks

## 🎨 Design Principles

- **Minimalist**: Clean, distraction-free interface
- **Accessible**: High contrast, readable fonts, proper touch targets
- **Responsive**: Adapts to different screen sizes and orientations
- **Consistent**: Follows Material Design guidelines
- **Performant**: Smooth animations and efficient rendering

## 🔄 Repeat System

The app supports sophisticated repeat patterns:

- **Daily**: Every day
- **Weekly**: Every week on the same day
- **Monthly**: Every month on the same date
- **Custom**: Every N days (user-defined)

### End Conditions
- **Never**: Repeats indefinitely
- **After Count**: Stops after X occurrences
- **On Date**: Stops on specific end date

## 🔔 Reminder System

### Hourly Checks
- Background worker runs every hour
- Checks for overdue tasks
- Triggers full-screen alerts when needed

### Full-Screen Alerts
- Displays over lock screen
- Animated, attention-grabbing overlay
- Snooze (15 minutes) or Dismiss options
- Custom vibration patterns

## 🧪 Testing

### Manual Testing Checklist
- [ ] Create new task
- [ ] Edit existing task
- [ ] Mark task as complete
- [ ] Delete task
- [ ] Search functionality
- [ ] Category filtering
- [ ] Repeat task creation
- [ ] Background reminders
- [ ] Full-screen alerts
- [ ] Dark mode toggle

### Automated Testing
```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## 🐛 Known Issues

- Date/Time pickers use simplified implementation (can be enhanced)
- Custom sound selection framework is ready but not fully implemented
- Some deprecated API warnings (non-critical)

## 🚀 Future Enhancements

- [ ] Cloud sync and backup
- [ ] Task sharing and collaboration
- [ ] Advanced statistics and analytics
- [ ] Widget support
- [ ] Voice input for tasks
- [ ] Location-based reminders
- [ ] Task templates
- [ ] Export/Import functionality

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📞 Support

For issues, questions, or feature requests, please create an issue in the repository.

---

**Built with ❤️ using Kotlin and Jetpack Compose**

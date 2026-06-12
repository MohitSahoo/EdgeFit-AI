# EdgeFit Coach - Android Application

Native Android application implementing on-device Edge AI posture monitoring and stretching detection.

## Overview

This is the complete source code for the **EdgeFit Coach** Android application. The app runs real-time posture analysis and stretching detection completely on-device, saving workout history in a local Room database and using Groq API for AI coaching.

## Features

- **Real-Time On-Device Pose Estimation**: Uses Google MediaPipe Pose Landmarker and Android CameraX to track body joints.
- **Stretching Exercise Classification & Form Analysis**: Automatically detects and counts reps (e.g. squats) and analyzes user form.
- **On-Device Database (Room)**: Stores workout sessions, exercise records, individual rep statistics, user profiles, and active fitness goals.
- **Direct Groq AI Integration**: Calls Groq cloud LLMs (`llama-3.3-70b-versatile` and `llama-3.1-8b-instant`) directly via Retrofit to generate workout plans, offer real-time motivation, and perform deep session reviews.
- **Modern Jetpack Compose UI**: Built with Material Design 3, presenting a rich, responsive interface with interactive dashboards and AI chat.

## Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose + Material Design 3
- **Architecture:** MVVM (Model-View-ViewModel) with Dagger Hilt for Dependency Injection
- **Database:** Room SQLite
- **Networking:** Retrofit 2 + OkHttp 4
- **Async:** Kotlin Coroutines + Flow
- **State Management:** StateFlow
- **AI/ML Core:** Google MediaPipe Tasks Vision

## Setup & Build Instructions

### Prerequisites
- **Android Studio Koala (2024.1.1)** or later.
- **JDK 17** configured in Android Studio.
- **Groq API Key**: To enable the AI Coach chat and analysis, make sure a Groq API Key is configured in `AppModule.kt`.

### Build with Android Studio
1. Open Android Studio and select **Open**.
2. Select the `android/` directory (this folder).
3. Allow Gradle to sync and download dependencies.
4. Click **Run** or press `Shift + F10` with a connected device/emulator.

### Build via Command Line
```bash
./gradlew assembleDebug
```
The debug APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

## Project Structure

```
app/src/main/java/com/edgefit/coach/
├── data/
│   ├── local/        # Room Database, WorkoutDao, WorkoutRepository
│   ├── model/        # Groq request/response models, ChatHistoryItem
│   ├── remote/       # GroqApiService, Retrofit Client configuration
│   └── repository/   # Repository layer (AiRepository, DashboardRepository, etc.)
├── di/               # AppModule for Hilt dependency injection
├── exercise/         # SquatDefinition, AngleCalculator, RepCounter, FormAnalyzer
├── pose/             # CameraManager, PoseLandmarkerManager, PoseOverlayView, LandmarkFilter
├── service/          # WorkoutService for background tracking
├── ui/               # MainActivity, HomeScreen, WorkoutScreen, ChatScreen, DashboardScreen
└── viewmodel/        # WorkoutViewModel, ChatViewModel, DashboardViewModel
```

---

**Built with ❤️ using Kotlin and Jetpack Compose**

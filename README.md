# EdgeFit AI Coach 🏋️‍♂️

**On-Device Edge AI Posture Monitoring & Stretching Coach**

EdgeFit AI Coach is a fully native, on-device Edge AI Android application. It combines real-time computer vision, local machine learning models, and advanced large language models to help users maintain healthy posture and guide them through stretching and exercise routines in real time.

By running the core AI processing directly on the device, it ensures ultra-low latency, maximum privacy, and robust performance without requiring a desktop companion or a separate backend server.

---

## 🌟 Key Features

- **Real-Time On-Device Pose Estimation**: Integrates Google MediaPipe Pose Landmarker with Android CameraX to track 33 key body landmarks in real-time directly on the device.
- **Exercise Classification & Rep Counting**: Automatically detects and classifies exercises (such as Squats) and counts repetitions dynamically based on movement range.
- **Biomechanical Form Analysis**: Analyzes joint angles (knees, hips, shoulders, elbows) in real-time to compute form scores (0-100%) and provides instant coaching feedback.
- **On-Device Database (Room)**: Persists workout sessions, exercise records, individual rep statistics, user profiles, and active fitness goals locally.
- **Direct Groq AI Integration**: Leverages cloud LLMs (`llama-3.3-70b-versatile` and `llama-3.1-8b-instant`) directly from Android via Retrofit to generate personalized workout plans, offer real-time motivation, and perform deep session reviews.
- **Offline Mode Support**: Standard core features (workout tracking, pose overlay, rep counting, form scoring) work completely offline, utilizing local template fallbacks for motivation and coaching cues.
- **Modern Jetpack Compose UI**: Built with Material Design 3, presenting a rich, responsive interface with interactive dashboards, color-coded health indicators, and a dedicated AI chat.

---

## 🏗️ Architecture & Tech Stack

EdgeFit AI Coach follows clean architecture guidelines and the MVVM design pattern:

- **UI Layer**: Jetpack Compose, Material 3, Navigation Compose
- **Dependency Injection**: Dagger Hilt
- **Local Storage**: Room SQLite Database for session logging and profile tracking
- **Networking**: Retrofit 2 + OkHttp 4 for direct Groq API integration
- **AI/ML Core**: Google MediaPipe tasks-vision (Pose Landmarker), Custom Form Analyzer, and Rep Counter
- **Camera Integration**: Android CameraX (Lifecycle-aware camera preview and analysis)
- **State Management**: Kotlin Coroutines & Flow (StateFlow for reactive UI state)

---

## 📂 Project Structure

```
EdgeFit-AI/
└── android/
    ├── app/
    │   ├── src/main/
    │   │   ├── java/com/edgefit/coach/
    │   │   │   ├── data/
    │   │   │   │   ├── local/        # Room Database, WorkoutDao, WorkoutRepository
    │   │   │   │   ├── model/        # Groq request/response models, ChatHistoryItem
    │   │   │   │   ├── remote/       # GroqApiService, Retrofit Client configuration
    │   │   │   │   └── repository/   # Repository layer (AiRepository, DashboardRepository, etc.)
    │   │   │   ├── di/               # AppModule for Hilt dependency injection
    │   │   │   ├── exercise/         # SquatDefinition, AngleCalculator, RepCounter, FormAnalyzer
    │   │   │   ├── pose/             # CameraManager, PoseLandmarkerManager, PoseOverlayView, LandmarkFilter
    │   │   │   ├── service/          # WorkoutService for background tracking
    │   │   │   ├── ui/               # MainActivity, HomeScreen, WorkoutScreen, ChatScreen, DashboardScreen
    │   │   │   └── viewmodel/        # WorkoutViewModel, ChatViewModel, DashboardViewModel
    │   │   ├── res/                  # Resources (values, layouts, themes)
    │   │   └── AndroidManifest.xml   # Camera and Internet permissions
    │   └── build.gradle.kts          # Dependencies (Hilt, Room, MediaPipe, CameraX, Retrofit)
    ├── build.gradle.kts
    └── settings.gradle.kts
```

---

## 🚀 Getting Started & Build Instructions

### Prerequisites
- **Android Studio Koala (2024.1.1)** or later.
- **JDK 17** configured in Android Studio.
- **Groq API Key**: To enable AI coaching and workout analysis, make sure a Groq API Key is configured. (By default, the app compiles with a key inside `AppModule.kt`, but it can also be customized).

### Build Steps

#### Option A: Using Android Studio
1. Open Android Studio and select **Open**.
2. Select the `android/` directory from this repository.
3. Allow Gradle to sync and download dependencies.
4. Connect an Android device (with USB debugging enabled) or launch an Emulator.
5. Click **Run** or press `Shift + F10`.

#### Option B: Command Line (Gradle Wrapper)
To compile the application directly from your terminal:
```bash
cd android
chmod +x gradlew
./gradlew assembleDebug
```
The debug APK will be generated at:
`android/app/build/outputs/apk/debug/app-debug.apk`

---

## 🔍 On-Device Edge AI Detail

### 1. Real-Time Pose Tracking (MediaPipe & CameraX)
The app captures live video frames via **CameraX** and streams them to the **PoseLandmarkerManager**. It tracks 33 critical joints, which are filtered and smoothed using a **LandmarkFilter** and **PoseSmoother** to eliminate jitter and ensure smooth animations on the **PoseOverlayView**.

### 2. Motion Classification & Form Analysis
- **AngleCalculator.kt**: Computes exact anatomical angles (knee flexion, hip hinge, torso lean, arm extension).
- **ExerciseClassifier.kt**: Detects the exercise type (e.g. Squat).
- **RepCounter.kt**: Uses state-machine logic to track reps as the user moves between starting, peak, and finishing thresholds.
- **FormAnalyzer.kt**: Compares joint angles against biomechanical standards to determine reps accuracy and output form feedback.

### 3. AI Coaching (Groq API)
- The app integrates directly with the Groq API (no intermediary backend server needed).
- **Llama 3.3 70B** provides chat coaching and comprehensive session summary analysis.
- **Llama 3.1 8B** provides ultra-fast, context-based motivational remarks immediately after each session.
- Offline backup templates are utilized if network issues arise, ensuring workout motivation is always active.

---

## 📄 License

This project is licensed under the terms specified in the LICENSE file.

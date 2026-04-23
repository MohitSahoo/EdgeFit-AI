# EdgeFit Coach — Android Mobile Application
## Product Requirements Document (PRD)

> **Target Platform:** Android (API Level 26+ / Android 8.0+)
> **Language:** Kotlin (primary) with XML layouts or Jetpack Compose
> **Backend:** EdgeFit-Coach FastAPI server running locally or on network (default: `http://<host>:8000`)
> **WebSocket:** `ws://<host>:8000/ws/motivation`
> **Status:** Draft v1.0

---

## 1. Overview

EdgeFit Coach is an AI-powered posture monitoring and coaching app. This Android client connects to the existing Python/FastAPI backend in the `EdgeFit-Coach` repo. The app surfaces real-time posture metrics, an AI coaching chat, motivational push-style notifications via WebSocket, and a posture analytics dashboard — all in a native Android UI.

---

## 2. Goals & Success Criteria

- [ ] Connect to the EdgeFit backend REST API and WebSocket over local network
- [ ] Display all 6 posture health metrics from `/dashboard/data` in a clean dashboard
- [ ] Enable AI-powered chat through `/chat/message` with conversation history
- [ ] Receive real-time motivation quotes via WebSocket `/ws/motivation` as in-app notifications
- [ ] Trigger and display AI posture analysis from `/analyze/report`
- [ ] Allow the user to configure the backend server IP/port at first launch

---

## 3. Required Backend Files (from the Repo)

These are the files you **must** have running on the host machine for the Android app to work:

| File | Purpose |
|---|---|
| `api_server.py` | Core FastAPI server — **must be running** (port 8000) |
| `websocket_server.py` | WebSocket server for motivation quotes (port 8001; `/ws/motivation` is on port 8000 via api_server) |
| `llm_handler.py` | AI/LLM integration for chat and analysis |
| `posture_db.py` | Converts `posture_summary.json` → `endpoint_values.json` for dashboard metrics |
| `dashboard_viewer.py` | Generates `posture_report.txt` for the analysis endpoint |
| `main_webcam_headless1.py` | Headless posture detection process started by `/video/start` |
| `config.yaml` | LLM configuration (API key, model server URL, workspace slug) |
| `requirements_api.txt` | Minimal dependencies needed to run `api_server.py` |
| `start_api.py` | Convenience launcher for the API server only |
| `motivation_quotes.json` | File watched by WebSocket to broadcast new quotes |
| `posture_summary.json` | Input data file consumed by posture_db.py for metrics |
| `chat_history.json` | Persisted conversation history |

> **Not needed on Android:** `frontend_test.py`, `dashboard_viewer.py` (runs server-side), any `requirements_qualcomm.txt`, ONNX files (run on the host).

---

## 4. API Endpoints to Integrate

### 4.1 Health & Status

| Method | Endpoint | Android Use |
|---|---|---|
| `GET` | `/health` | Connectivity check on app start / settings screen |
| `GET` | `/files/status` | Optional: debug screen to verify backend data files |
| `GET` | `/` | Optional: fetch API version / available routes |

### 4.2 Video Stream Control

| Method | Endpoint | Android Use |
|---|---|---|
| `GET` | `/video/start` | "Start Session" button — launches posture detection on host |
| `GET` | `/video/stop` | "Stop Session" button |
| `GET` | `/video/status` | Poll to show session running/stopped indicator |

### 4.3 WebSocket — Real-time Motivation

| Protocol | Endpoint | Android Use |
|---|---|---|
| `WebSocket` | `ws://<host>:8000/ws/motivation` | Persistent connection; receive motivation quotes; send `"ping"` every 30s for keepalive |

**Incoming message format:**
```json
{
  "type": "motivation",
  "data": {
    "quote": "Keep your back straight!",
    "timestamp": "2024-01-01T12:00:00Z"
  },
  "timestamp": "2024-01-01T12:00:00Z"
}
```
**Message types:** `connection` (welcome), `motivation` (new quote), `pong` (keepalive response)

### 4.4 Dashboard Analytics

| Method | Endpoint | Request | Android Use |
|---|---|---|---|
| `GET` | `/dashboard/data` | — | Load all 6 metrics on Dashboard screen |
| `GET` | `/dashboard/refresh` | — | Pull-to-refresh / manual refresh button |

**Response fields to display:**
```
posture_health_score        → Progress ring / percentage
slouch_to_good_conversions  → Counter card
consistency_index           → Progress bar
session_success_rate        → Progress bar
recent_trend_score          → Trend indicator
total_good_posture_minutes  → Minutes counter card
```

### 4.5 AI Chat

| Method | Endpoint | Request Body | Android Use |
|---|---|---|---|
| `POST` | `/chat/message` | `{ "message": "..." }` | Send user message, display AI response |
| `GET` | `/chat/history?limit=20` | — | Load conversation history on Chat screen open |
| `DELETE` | `/chat/history` | — | "Clear Chat" button |

**Response fields:**
```
response          → AI reply text
timestamp         → Message time
conversation_id   → For grouping
```

### 4.6 Analysis & Reports

| Method | Endpoint | Android Use |
|---|---|---|
| `POST` | `/analyze/report` | "Generate Analysis" button — triggers full AI report |
| `GET` | `/analyze/report-file` | View raw posture report text in a scrollable screen |

---

## 5. Screen Inventory

### Screen 1 — Onboarding / Server Setup
- [ ] Input field for backend IP address (e.g., `192.168.1.10`)
- [ ] Input field for port (default `8000`)
- [ ] "Connect" button → calls `GET /health` to validate
- [ ] Show success/error state
- [ ] Save IP/port to `SharedPreferences`

### Screen 2 — Home / Session Control
- [ ] Session status card (Running / Stopped) from `/video/status`
- [ ] "Start Session" button → `GET /video/start`
- [ ] "Stop Session" button → `GET /video/stop`
- [ ] Live motivation quote banner (from WebSocket `/ws/motivation`)
- [ ] Bottom navigation bar (Dashboard | Chat | Analysis | Settings)

### Screen 3 — Dashboard
- [ ] Pull-to-refresh → `GET /dashboard/refresh`
- [ ] Auto-load on enter → `GET /dashboard/data`
- [ ] 6 metric cards:
  - [ ] Posture Health Score (circular progress, color-coded)
  - [ ] Slouch-to-Good Conversions (count chip)
  - [ ] Consistency Index (horizontal progress bar)
  - [ ] Session Success Rate (horizontal progress bar)
  - [ ] Recent Trend Score (trend arrow + value)
  - [ ] Total Good Posture Minutes (large counter)
- [ ] Last updated timestamp

### Screen 4 — AI Chat
- [ ] Load history on open → `GET /chat/history`
- [ ] Message bubble list (user right, AI left)
- [ ] Text input + Send button → `POST /chat/message`
- [ ] Loading indicator while waiting for AI response
- [ ] "Clear History" action in toolbar → `DELETE /chat/history`
- [ ] Error snackbar on network failure

### Screen 5 — Analysis
- [ ] "Run Analysis" button → `POST /analyze/report`
- [ ] Analysis result card with AI-generated text
- [ ] "View Raw Report" button → `GET /analyze/report-file` → scrollable full-screen text view
- [ ] Loading state (analysis takes 30–60s per README)
- [ ] "Continue in Chat" button navigates to Chat screen

### Screen 6 — Settings
- [ ] Edit backend IP/port (re-runs health check on save)
- [ ] WebSocket connection status indicator
- [ ] App version info
- [ ] "Test Connection" button → `GET /health`

---

## 6. Technical Architecture

```
Android App
├── ui/
│   ├── onboarding/        OnboardingActivity (server setup)
│   ├── home/              HomeFragment (session control + WS quote banner)
│   ├── dashboard/         DashboardFragment (6 metrics)
│   ├── chat/              ChatFragment (AI chat)
│   ├── analysis/          AnalysisFragment (report + AI analysis)
│   └── settings/          SettingsFragment
├── data/
│   ├── network/
│   │   ├── ApiService.kt         (Retrofit interface for REST)
│   │   ├── ApiClient.kt          (Retrofit + OkHttp setup)
│   │   └── WebSocketManager.kt   (OkHttp WebSocket)
│   ├── repository/
│   │   ├── DashboardRepository.kt
│   │   ├── ChatRepository.kt
│   │   ├── VideoRepository.kt
│   │   └── AnalysisRepository.kt
│   └── model/
│       ├── DashboardData.kt
│       ├── ChatMessage.kt
│       └── MotivationQuote.kt
├── viewmodel/
│   ├── DashboardViewModel.kt
│   ├── ChatViewModel.kt
│   ├── HomeViewModel.kt
│   └── AnalysisViewModel.kt
└── util/
    ├── NetworkUtils.kt
    └── PreferencesManager.kt     (SharedPreferences for IP/port)
```

---

## 7. Dependencies (Gradle)

```kotlin
// Networking
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// Lifecycle / ViewModel
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")

// UI
implementation("com.google.android.material:material:1.11.0")
implementation("androidx.constraintlayout:constraintlayout:2.1.4")

// Charts (for dashboard metrics)
implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

// JSON
implementation("com.google.code.gson:gson:2.10.1")
```

---

## 8. Claude Code Task Checklist

Use the following as your prompt sequence for Claude Code in Android Studio (Kotlin):

### Phase 1 — Project Setup
- [ ] Create a new Android project in Android Studio with **Kotlin** and **Empty Activity**
- [ ] Set `minSdk = 26`, `targetSdk = 34`
- [ ] Add all dependencies listed in Section 7 to `build.gradle.kts`
- [ ] Add `INTERNET` permission to `AndroidManifest.xml`
- [ ] Add `android:usesCleartextTraffic="true"` in manifest for local HTTP (non-HTTPS backend)

### Phase 2 — Network Layer
- [ ] Create `PreferencesManager.kt` to store/retrieve `serverIp` and `serverPort` from SharedPreferences
- [ ] Create `ApiService.kt` with Retrofit interface for all REST endpoints (Sections 4.1–4.6)
- [ ] Create `ApiClient.kt` that reads IP/port from PreferencesManager and builds Retrofit dynamically
- [ ] Create `WebSocketManager.kt` using OkHttp WebSocket to connect to `ws://{ip}:{port}/ws/motivation`, parse messages, and expose a `Flow<MotivationQuote>`

### Phase 3 — Data Models
- [ ] Create `DashboardData.kt` data class matching the `/dashboard/data` JSON response
- [ ] Create `ChatMessage.kt` for chat request/response
- [ ] Create `MotivationQuote.kt` for WebSocket messages
- [ ] Create `VideoStatus.kt`, `AnalysisResponse.kt`, `ReportFile.kt`

### Phase 4 — Repositories
- [ ] `DashboardRepository.kt` — wraps `getDashboardData()` and `refreshDashboard()` calls
- [ ] `ChatRepository.kt` — wraps `sendMessage()`, `getHistory()`, `clearHistory()`
- [ ] `VideoRepository.kt` — wraps `startVideo()`, `stopVideo()`, `getVideoStatus()`
- [ ] `AnalysisRepository.kt` — wraps `generateReport()`, `getReportFile()`

### Phase 5 — ViewModels
- [ ] `HomeViewModel.kt` — exposes session status StateFlow, calls start/stop, holds WS quote LiveData
- [ ] `DashboardViewModel.kt` — exposes dashboard metrics StateFlow, handles refresh
- [ ] `ChatViewModel.kt` — manages message list, loading state, sends messages
- [ ] `AnalysisViewModel.kt` — manages analysis result, loading state (handle 30–60s timeout)

### Phase 6 — UI Screens
- [ ] `OnboardingActivity.kt` + layout — server IP/port form, connect button, health check
- [ ] `MainActivity.kt` — hosts BottomNavigationView with 4 tabs
- [ ] `HomeFragment.kt` + layout — session card, start/stop buttons, motivation quote banner
- [ ] `DashboardFragment.kt` + layout — 6 metric cards with MPAndroidChart progress rings
- [ ] `ChatFragment.kt` + layout — RecyclerView with message bubbles, input bar
- [ ] `AnalysisFragment.kt` + layout — run analysis button, result card, view report button
- [ ] `SettingsFragment.kt` + layout — editable IP/port, test connection, WS status

### Phase 7 — WebSocket Integration
- [ ] Wire `WebSocketManager` into `HomeViewModel` to observe motivation quotes
- [ ] Display quotes in a `MaterialCardView` banner on HomeFragment with fade-in animation
- [ ] Implement 30-second ping keepalive in `WebSocketManager`
- [ ] Handle reconnection on disconnect (exponential backoff, max 5 retries)

### Phase 8 — Polish & Error Handling
- [ ] Add `Snackbar` error messages for all network failures
- [ ] Add `SwipeRefreshLayout` on Dashboard screen
- [ ] Add loading `ProgressBar` / skeleton screens while fetching data
- [ ] Add color-coded threshold logic for metric cards:
  - Green: score ≥ 75, Yellow: 50–74, Red: < 50
- [ ] Handle 30–60s long wait on `/analyze/report` with a progress dialog
- [ ] Add network availability check before any API call

### Phase 9 — Testing
- [ ] Write a simple `ConnectionTest` activity / debug screen to manually ping each endpoint
- [ ] Verify WebSocket ping/pong cycle works without dropping
- [ ] Test on both physical device and emulator (use `10.0.2.2` as host IP in emulator)

---

## 9. Claude Code Skills to Use

| Skill | When to Apply |
|---|---|
| **Kotlin Coroutines + Flow** | All async API calls and WebSocket stream |
| **Retrofit + OkHttp** | REST client and WebSocket client setup |
| **ViewModel + LiveData/StateFlow** | MVVM state management across all screens |
| **RecyclerView with DiffUtil** | Chat message list, efficient updates |
| **MPAndroidChart** | Dashboard metric visualizations |
| **SharedPreferences** | Persisting server IP/port |
| **Material Design 3** | All UI components (cards, buttons, bottom nav) |
| **Jetpack Navigation Component** | Fragment navigation, back stack management |

---

## 10. Network Notes for Android

- The backend runs on the **same local Wi-Fi network** as the Android device. Use the host machine's LAN IP (e.g., `192.168.x.x`).
- For **Android Emulator**, use `10.0.2.2` to reach `localhost` on the host machine.
- Because the server uses plain **HTTP** (not HTTPS), you must add `android:usesCleartextTraffic="true"` to `<application>` in `AndroidManifest.xml`, or add a `network_security_config.xml` that permits cleartext for the host IP.
- WebSocket URL: `ws://<ip>:8000/ws/motivation` (not `wss://`).
- The `/analyze/report` endpoint can take **30–60 seconds** — set your OkHttp `readTimeout` to at least 90 seconds for this call specifically.

---

## 11. Out of Scope (v1.0)

- Camera / pose detection running on the Android device (runs on host only)
- Offline mode / local data caching beyond in-memory state
- User authentication / multi-user support
- Cloud deployment of the backend
- iOS version

---

*Built to complement the EdgeFit-Coach backend — a Qualcomm Edge AI Developer Hackathon project.*

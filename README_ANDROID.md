# EdgeFit Coach - Complete Project

## Project Overview

EdgeFit Coach is an AI-powered posture monitoring system with:
- **Backend:** Python FastAPI server with real-time WebSocket support
- **Android App:** Native Kotlin app with Jetpack Compose UI (NEW!)

---

## 🆕 Android Application

A complete native Android client has been implemented in the `android/` directory.

### Features Implemented

✅ **Onboarding Screen** - Server IP/port configuration with connection testing
✅ **Home Screen** - Start/stop posture monitoring sessions with real-time WebSocket quotes
✅ **Dashboard** - 6 posture metrics with color-coded indicators and pull-to-refresh
✅ **Chat Screen** - AI-powered coaching with conversation history
✅ **Analysis Screen** - Generate comprehensive AI posture reports (30-60s)
✅ **Settings Screen** - Edit server config, test connection, view app info

### Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material Design 3
- **Architecture:** MVVM (ViewModel + StateFlow)
- **Networking:** Retrofit 2.9.0 + OkHttp 4.12.0
- **Async:** Kotlin Coroutines + Flow
- **WebSocket:** OkHttp WebSocket with 30s keepalive
- **Storage:** DataStore Preferences

### Quick Start

#### 1. Start Backend Server

```bash
cd /Users/mohitsahoo/EdgeFit-Coach
python api_server.py
```

Server runs on `http://0.0.0.0:8000`

#### 2. Build Android App

**Option A: Android Studio**
```bash
cd android
# Open in Android Studio, then Run
```

**Option B: Command Line**
```bash
cd android
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

#### 3. Configure Connection

**For Android Emulator:**
- IP: `10.0.2.2`
- Port: `8000`

**For Physical Device:**
- Find your computer's IP: `ifconfig | grep "inet "`
- Use that IP (e.g., `192.168.1.10`)
- Port: `8000`

---

## Backend API Endpoints

All endpoints are integrated in the Android app:

| Endpoint | Method | Android Integration |
|----------|--------|---------------------|
| `/health` | GET | Onboarding connection test |
| `/video/start` | GET | Home screen - Start button |
| `/video/stop` | GET | Home screen - Stop button |
| `/video/status` | GET | Home screen - Status display |
| `/dashboard/data` | GET | Dashboard screen - Load metrics |
| `/dashboard/refresh` | GET | Dashboard screen - Refresh button |
| `/chat/message` | POST | Chat screen - Send message |
| `/chat/history` | GET | Chat screen - Load history |
| `/chat/history` | DELETE | Chat screen - Clear history |
| `/analyze/report` | POST | Analysis screen - Generate report |
| `/analyze/report-file` | GET | Analysis screen - View raw report |
| `/ws/motivation` | WebSocket | Home screen - Live quotes |

---

## Project Structure

```
EdgeFit-Coach/
├── android/                    # 🆕 Android Application
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/edgefit/coach/
│   │   │   │   ├── data/
│   │   │   │   │   ├── model/          # 6 data model files
│   │   │   │   │   ├── remote/         # API + WebSocket
│   │   │   │   │   └── repository/     # 5 repositories
│   │   │   │   ├── ui/
│   │   │   │   │   ├── onboarding/     # Server setup
│   │   │   │   │   ├── home/           # Session control
│   │   │   │   │   ├── dashboard/      # 6 metrics
│   │   │   │   │   ├── chat/           # AI chat
│   │   │   │   │   ├── analysis/       # Reports
│   │   │   │   │   ├── settings/       # Config
│   │   │   │   │   └── theme/          # Material3 theme
│   │   │   │   ├── viewmodel/          # 4 ViewModels
│   │   │   │   └── util/               # PreferencesManager
│   │   │   ├── res/                    # Resources
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle.kts
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── README.md                       # Android-specific docs
├── api_server.py                       # FastAPI backend
├── websocket_server.py                 # WebSocket server
├── llm_handler.py                      # AI integration
├── posture_db.py                       # Metrics processor
├── dashboard_viewer.py                 # Report generator
├── main_webcam_headless1.py            # Posture detection
├── config.yaml                         # LLM config
├── motivation_quotes.json              # AI quotes
├── posture_summary.json                # Posture data
├── chat_history.json                   # Chat history
└── README.md                           # This file
```

---

## Files Created (Android App)

### Configuration (7 files)
- `android/settings.gradle.kts`
- `android/build.gradle.kts`
- `android/app/build.gradle.kts`
- `android/app/proguard-rules.pro`
- `android/gradle/wrapper/gradle-wrapper.properties`
- `android/gradlew`
- `android/.gitignore`

### Data Layer (15 files)
- `data/model/DashboardData.kt`
- `data/model/ChatMessage.kt`
- `data/model/MotivationQuote.kt`
- `data/model/VideoStatus.kt`
- `data/model/AnalysisResponse.kt`
- `data/model/HealthCheck.kt`
- `data/remote/ApiService.kt`
- `data/remote/ApiClient.kt`
- `data/remote/WebSocketManager.kt`
- `data/repository/DashboardRepository.kt`
- `data/repository/ChatRepository.kt`
- `data/repository/VideoRepository.kt`
- `data/repository/AnalysisRepository.kt`
- `data/repository/WebSocketRepository.kt`
- `util/PreferencesManager.kt`

### ViewModel Layer (4 files)
- `viewmodel/HomeViewModel.kt`
- `viewmodel/DashboardViewModel.kt`
- `viewmodel/ChatViewModel.kt`
- `viewmodel/AnalysisViewModel.kt`

### UI Layer (13 files)
- `ui/MainActivity.kt`
- `ui/onboarding/OnboardingScreen.kt`
- `ui/home/HomeScreen.kt`
- `ui/dashboard/DashboardScreen.kt`
- `ui/dashboard/MetricCard.kt`
- `ui/chat/ChatScreen.kt`
- `ui/chat/MessageBubble.kt`
- `ui/analysis/AnalysisScreen.kt`
- `ui/settings/SettingsScreen.kt`
- `ui/theme/Color.kt`
- `ui/theme/Theme.kt`
- `ui/theme/Type.kt`
- `AndroidManifest.xml`

### Resources (3 files)
- `res/values/strings.xml`
- `res/values/themes.xml`
- `README.md`

**Total: 42 files created**

---

## Testing the Android App

### 1. Backend Health Check
```bash
curl http://localhost:8000/health
```

### 2. Test WebSocket
```bash
# In Python
import asyncio
import websockets

async def test():
    async with websockets.connect('ws://localhost:8000/ws/motivation') as ws:
        msg = await ws.recv()
        print(msg)

asyncio.run(test())
```

### 3. Android App Testing Checklist

- [ ] Onboarding connects successfully
- [ ] Home screen shows session status
- [ ] Start/Stop session buttons work
- [ ] WebSocket receives motivation quotes
- [ ] Dashboard displays all 6 metrics
- [ ] Dashboard refresh updates data
- [ ] Chat sends and receives messages
- [ ] Chat history loads correctly
- [ ] Analysis generates report (wait 60s)
- [ ] Settings saves server config
- [ ] App survives rotation/config changes

---

## Troubleshooting

### Android App Cannot Connect

1. **Check backend is running:**
   ```bash
   curl http://<your-ip>:8000/health
   ```

2. **Verify network:**
   - Device and server on same Wi-Fi
   - Firewall allows port 8000

3. **For emulator:**
   - Always use `10.0.2.2`, not `localhost`

4. **Check cleartext traffic:**
   - `AndroidManifest.xml` has `android:usesCleartextTraffic="true"`

### WebSocket Issues

- Ensure `websocket_server.py` is running
- Check `/ws/motivation` endpoint is accessible
- Verify no proxy/VPN blocking WebSocket

### Build Errors

```bash
cd android
./gradlew clean
./gradlew assembleDebug
```

---

## Next Steps

### For Development
1. Open `android/` in Android Studio
2. Sync Gradle dependencies
3. Run on emulator or device
4. Test all features with backend running

### For Production
1. Update `applicationId` in `app/build.gradle.kts`
2. Configure signing keys
3. Build release APK: `./gradlew assembleRelease`
4. Test on multiple devices/Android versions

### Future Enhancements
- [ ] Add offline mode with local caching
- [ ] Implement push notifications
- [ ] Add user authentication
- [ ] Support multiple backend servers
- [ ] Add data export functionality
- [ ] Implement dark/light theme toggle
- [ ] Add accessibility improvements
- [ ] Create widget for quick session control

---

## Architecture Highlights

### MVVM Pattern
```
View (Compose) → ViewModel (StateFlow) → Repository → API/WebSocket
```

### State Management
- **StateFlow** for UI state (reactive, lifecycle-aware)
- **Flow** for WebSocket message stream
- **DataStore** for persistent settings

### Network Layer
- **Retrofit** for REST API calls
- **OkHttp** for WebSocket connection
- **Gson** for JSON serialization
- 90-second timeout for long-running analysis

### Coroutines Usage
- `viewModelScope` for ViewModel operations
- `Dispatchers.IO` for network calls
- Structured concurrency with proper cancellation

---

## License

Part of EdgeFit Coach - Qualcomm Edge AI Developer Hackathon project.

---

**Built with Kotlin, Jetpack Compose, and Material Design 3** 🚀

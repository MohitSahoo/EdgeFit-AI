# EdgeFit Coach - Android Application

Native Android client for the EdgeFit Coach AI-powered posture monitoring system.

## Overview

This Android app connects to the EdgeFit Coach backend server to provide:
- Real-time posture monitoring session control
- 6-metric posture analytics dashboard
- AI-powered coaching chat interface
- Comprehensive posture analysis reports
- Live motivation quotes via WebSocket

## Requirements

- **Android Device/Emulator:** Android 8.0 (API 26) or higher
- **Backend Server:** EdgeFit Coach FastAPI server running on local network
- **Network:** Device must be on same Wi-Fi network as backend server

## Architecture

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose with Material Design 3
- **Architecture:** MVVM (Model-View-ViewModel)
- **Networking:** Retrofit 2.9.0 + OkHttp 4.12.0
- **Async:** Kotlin Coroutines + Flow
- **State Management:** StateFlow
- **Data Persistence:** DataStore Preferences

## Project Structure

```
app/src/main/java/com/edgefit/coach/
├── data/
│   ├── model/          # Data classes for API responses
│   ├── remote/         # API service, WebSocket manager
│   └── repository/     # Repository layer
├── ui/
│   ├── onboarding/     # Server setup screen
│   ├── home/           # Session control + motivation quotes
│   ├── dashboard/      # 6 metrics display
│   ├── chat/           # AI chat interface
│   ├── analysis/       # Report generation
│   ├── settings/       # Server configuration
│   └── theme/          # Material3 theme
├── viewmodel/          # ViewModels for each screen
└── util/               # Utilities (PreferencesManager)
```

## Setup Instructions

### 1. Backend Server Setup

Ensure the EdgeFit Coach backend is running:

```bash
cd /path/to/EdgeFit-Coach
python api_server.py
```

The server should be accessible at `http://<your-ip>:8000`

### 2. Build the Android App

#### Using Android Studio:
1. Open the `android/` directory in Android Studio
2. Wait for Gradle sync to complete
3. Click "Run" or press Shift+F10

#### Using Command Line:
```bash
cd android
./gradlew assembleDebug
```

The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

### 3. Configure Server Connection

#### For Android Emulator:
- Use IP: `10.0.2.2`
- Port: `8000`

#### For Physical Device:
1. Find your computer's LAN IP address:
   - **macOS/Linux:** `ifconfig | grep "inet "`
   - **Windows:** `ipconfig`
2. Use that IP (e.g., `192.168.1.10`)
3. Port: `8000`

### 4. First Launch

1. Launch the app
2. Enter server IP and port on onboarding screen
3. Tap "Connect" to test connection
4. Once connected, you'll see the main app interface

## Features

### Home Screen
- Start/Stop posture monitoring sessions
- View session status (Running/Stopped)
- Receive real-time motivation quotes from AI coach
- WebSocket connection for live updates

### Dashboard
- **Posture Health Score:** Overall posture quality (0-100%)
- **Slouch-to-Good Conversions:** Times you corrected posture
- **Consistency Index:** How consistent your posture is
- **Session Success Rate:** Percentage of successful sessions
- **Recent Trend Score:** Recent performance trend
- **Total Good Posture Time:** Cumulative time with good posture
- Pull-to-refresh for latest data
- Color-coded metrics (Green ≥75%, Yellow 50-74%, Red <50%)

### Chat Screen
- AI-powered posture coaching
- Conversation history
- Real-time responses
- Clear history option
- Message bubbles (user right, AI left)

### Analysis Screen
- Generate comprehensive AI analysis (30-60s)
- View detailed posture report
- Continue conversation in chat
- Export/share analysis results

### Settings Screen
- Edit server IP and port
- Test connection
- View app information
- Connection status indicator

## API Endpoints Used

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/health` | GET | Connection health check |
| `/video/start` | GET | Start posture detection session |
| `/video/stop` | GET | Stop posture detection session |
| `/video/status` | GET | Get current session status |
| `/dashboard/data` | GET | Fetch 6 posture metrics |
| `/dashboard/refresh` | GET | Refresh dashboard data |
| `/chat/message` | POST | Send chat message to AI |
| `/chat/history` | GET | Retrieve conversation history |
| `/chat/history` | DELETE | Clear chat history |
| `/analyze/report` | POST | Generate AI analysis report |
| `/analyze/report-file` | GET | Get raw report file |
| `/ws/motivation` | WebSocket | Real-time motivation quotes |

## WebSocket Protocol

The app maintains a persistent WebSocket connection for real-time updates:

- **Connection:** `ws://<ip>:<port>/ws/motivation`
- **Keepalive:** Sends "ping" every 30 seconds
- **Message Types:**
  - `connection`: Welcome message
  - `motivation`: New motivation quote
  - `pong`: Keepalive response

## Troubleshooting

### Cannot Connect to Server

1. **Check backend is running:**
   ```bash
   curl http://<your-ip>:8000/health
   ```

2. **Verify network connectivity:**
   - Device and server on same Wi-Fi network
   - Firewall not blocking port 8000

3. **For emulator:**
   - Always use `10.0.2.2` instead of `localhost`

4. **Check AndroidManifest.xml:**
   - `android:usesCleartextTraffic="true"` is set (required for HTTP)

### WebSocket Not Connecting

1. Ensure backend WebSocket server is running
2. Check that `/ws/motivation` endpoint is accessible
3. Verify no proxy/VPN interfering with WebSocket connections

### Analysis Takes Too Long

- Analysis generation can take 30-60 seconds
- Ensure stable network connection
- Check backend logs for errors

## Development

### Adding New Features

1. **Data Model:** Add to `data/model/`
2. **API Endpoint:** Update `ApiService.kt`
3. **Repository:** Create/update in `data/repository/`
4. **ViewModel:** Create in `viewmodel/`
5. **UI Screen:** Create in `ui/`

### Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## Dependencies

Key dependencies (see `app/build.gradle.kts` for full list):

- Jetpack Compose BOM 2024.02.00
- Retrofit 2.9.0
- OkHttp 4.12.0
- Kotlin Coroutines 1.7.3
- Material3
- Navigation Compose 2.7.7
- DataStore Preferences 1.0.0

## License

Part of the EdgeFit Coach project - Qualcomm Edge AI Developer Hackathon submission.

## Support

For issues or questions:
1. Check backend server logs
2. Review Android logcat output
3. Verify network configuration
4. Ensure all backend dependencies are installed

---

**Built with ❤️ using Kotlin and Jetpack Compose**

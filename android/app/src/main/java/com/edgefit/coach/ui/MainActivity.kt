package com.edgefit.coach.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.edgefit.coach.data.remote.ApiClient
import com.edgefit.coach.data.remote.WebSocketManager
import com.edgefit.coach.data.repository.*
import com.edgefit.coach.ui.analysis.AnalysisScreen
import com.edgefit.coach.ui.chat.ChatScreen
import com.edgefit.coach.ui.dashboard.DashboardScreen
import com.edgefit.coach.ui.home.HomeScreen
import com.edgefit.coach.ui.onboarding.OnboardingScreen
import com.edgefit.coach.ui.settings.SettingsScreen
import com.edgefit.coach.ui.theme.EdgeFitCoachTheme
import com.edgefit.coach.util.PreferencesManager
import com.edgefit.coach.viewmodel.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ApiClient
        ApiClient.initialize(this)

        setContent {
            EdgeFitCoachTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val preferencesManager = remember { PreferencesManager(this) }
                    val scope = rememberCoroutineScope()
                    var isOnboarded by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        scope.launch {
                            val ip = preferencesManager.serverIp.first()
                            val port = preferencesManager.serverPort.first()
                            isOnboarded = ip != PreferencesManager.DEFAULT_IP ||
                                         port != PreferencesManager.DEFAULT_PORT
                        }
                    }

                    if (isOnboarded) {
                        MainScreen(preferencesManager)
                    } else {
                        OnboardingScreen(
                            preferencesManager = preferencesManager,
                            onOnboardingComplete = { isOnboarded = true }
                        )
                    }
                }
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Chat : Screen("chat", "Chat", Icons.Default.Chat)
    object Analysis : Screen("analysis", "Analysis", Icons.Default.Assessment)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun MainScreen(preferencesManager: PreferencesManager) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    // Initialize repositories and ViewModels
    val apiService = ApiClient.getApiService()

    val serverIp = preferencesManager.serverIp.collectAsState(initial = PreferencesManager.DEFAULT_IP)
    val serverPort = preferencesManager.serverPort.collectAsState(initial = PreferencesManager.DEFAULT_PORT)

    val webSocketManager = remember(serverIp.value, serverPort.value) {
        WebSocketManager(serverIp.value, serverPort.value)
    }

    val videoRepository = remember { VideoRepository(apiService) }
    val webSocketRepository = remember(webSocketManager) { WebSocketRepository(webSocketManager) }
    val dashboardRepository = remember { DashboardRepository(apiService) }
    val chatRepository = remember { ChatRepository(apiService) }
    val analysisRepository = remember { AnalysisRepository(apiService) }

    val homeViewModel = remember { HomeViewModel(videoRepository, webSocketRepository) }
    val dashboardViewModel = remember { DashboardViewModel(dashboardRepository) }
    val chatViewModel = remember { ChatViewModel(chatRepository) }
    val analysisViewModel = remember { AnalysisViewModel(analysisRepository) }

    val items = listOf(
        Screen.Home,
        Screen.Dashboard,
        Screen.Chat,
        Screen.Analysis,
        Screen.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(viewModel = homeViewModel)
            }
            composable(Screen.Dashboard.route) {
                DashboardScreen(viewModel = dashboardViewModel)
            }
            composable(Screen.Chat.route) {
                ChatScreen(
                    viewModel = chatViewModel,
                    onNavigateToAnalysis = {
                        navController.navigate(Screen.Analysis.route)
                    }
                )
            }
            composable(Screen.Analysis.route) {
                AnalysisScreen(
                    viewModel = analysisViewModel,
                    onNavigateToChat = {
                        navController.navigate(Screen.Chat.route)
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    preferencesManager = preferencesManager,
                    onServerConfigChanged = {
                        ApiClient.updateBaseUrl()
                    }
                )
            }
        }
    }
}

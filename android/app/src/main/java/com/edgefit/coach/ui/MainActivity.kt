package com.edgefit.coach.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.edgefit.coach.ui.chat.ChatScreen
import com.edgefit.coach.ui.dashboard.DashboardScreen
import com.edgefit.coach.ui.home.HomeScreen
import com.edgefit.coach.ui.settings.SettingsScreen
import com.edgefit.coach.ui.theme.EdgeFitCoachTheme
import com.edgefit.coach.ui.workout.WorkoutScreen
import com.edgefit.coach.ui.workout.WorkoutSummaryScreen
import com.edgefit.coach.viewmodel.ChatViewModel
import com.edgefit.coach.viewmodel.DashboardViewModel
import com.edgefit.coach.viewmodel.WorkoutViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EdgeFitCoachTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Workout : Screen("workout", "Workout", Icons.Default.FitnessCenter)
    object WorkoutSummary : Screen("workout_summary", "Summary", Icons.Default.Assessment)
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Chat : Screen("chat", "Chat", Icons.Default.Chat)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    // Bottom nav items (excluding Workout and Summary which are fullscreen flows)
    val bottomNavItems = listOf(
        Screen.Home,
        Screen.Dashboard,
        Screen.Chat,
        Screen.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom bar on fullscreen workout screens
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = navBackStackEntry?.destination?.hierarchy?.any { it.route == screen.route } == true,
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
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onStartWorkout = { 
                        navController.navigate(Screen.Workout.route) 
                    }
                )
            }
            composable(Screen.Workout.route) {
                // Use activity-scoped ViewModel so the session survives orientation changes
                // and can be shared with the summary screen
                val workoutViewModel: WorkoutViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
                
                WorkoutScreen(
                    viewModel = workoutViewModel,
                    onWorkoutComplete = {
                        navController.navigate(Screen.WorkoutSummary.route) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }
            composable(Screen.WorkoutSummary.route) {
                val workoutViewModel: WorkoutViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
                
                WorkoutSummaryScreen(
                    viewModel = workoutViewModel,
                    onNavigateBack = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onGetAiAnalysis = {
                        // In a full implementation, we'd pass the session data to the Chat screen
                        // and navigate there to show the AI analysis
                        navController.navigate(Screen.Chat.route) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }
            composable(Screen.Dashboard.route) {
                val dashboardViewModel: DashboardViewModel = hiltViewModel()
                DashboardScreen(viewModel = dashboardViewModel)
            }
            composable(Screen.Chat.route) {
                val chatViewModel: ChatViewModel = hiltViewModel()
                ChatScreen(
                    viewModel = chatViewModel,
                    onNavigateToAnalysis = {}
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}

// Temporary placeholders for incomplete screens
@Composable
fun SettingsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Settings Screen")
    }
}

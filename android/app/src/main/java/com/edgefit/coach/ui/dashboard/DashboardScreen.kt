package com.edgefit.coach.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edgefit.coach.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val dashboardData by viewModel.dashboardData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val error by viewModel.error.collectAsState()
    val lastUpdated by viewModel.lastUpdated.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    lastUpdated?.let {
                        Text(
                            text = "Updated: $it",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (isLoading && dashboardData == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                dashboardData?.let { data ->
                    // Video Stream View
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Live Camera Feed",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                                
                                val context = androidx.compose.ui.platform.LocalContext.current
                                val preferencesManager = remember { com.edgefit.coach.util.PreferencesManager(context) }
                                val ip by preferencesManager.serverIp.collectAsState(initial = "10.0.2.2")
                                val port by preferencesManager.serverPort.collectAsState(initial = "8000")
                                val streamUrl = "http://$ip:$port/video/stream"
                                
                                androidx.compose.ui.viewinterop.AndroidView(
                                    factory = { ctx ->
                                        android.webkit.WebView(ctx).apply {
                                            settings.javaScriptEnabled = true
                                            settings.loadWithOverviewMode = true
                                            settings.useWideViewPort = true
                                            setBackgroundColor(android.graphics.Color.BLACK)
                                            loadUrl(streamUrl)
                                        }
                                    },
                                    update = { webView ->
                                        // Only reload if the URL changes to avoid flicker
                                        if (webView.url != streamUrl) {
                                            webView.loadUrl(streamUrl)
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    // Posture Health Score
                    item {
                        CircularMetricCard(
                            title = "Posture Health Score",
                            value = data.postureHealthScore,
                            color = getColorForScore(data.postureHealthScore)
                        )
                    }

                    // Slouch to Good Conversions
                    item {
                        MetricCard(
                            title = "Slouch-to-Good Conversions",
                            value = data.slouchToGoodConversions.toString(),
                            subtitle = "Times you corrected your posture",
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    // Consistency Index
                    item {
                        MetricCard(
                            title = "Consistency Index",
                            value = "${data.consistencyIndex.toInt()}%",
                            subtitle = "How consistent your posture is",
                            progress = (data.consistencyIndex / 100).toFloat(),
                            color = getColorForScore(data.consistencyIndex)
                        )
                    }

                    // Session Success Rate
                    item {
                        MetricCard(
                            title = "Session Success Rate",
                            value = "${data.sessionSuccessRate.toInt()}%",
                            subtitle = "Percentage of successful sessions",
                            progress = (data.sessionSuccessRate / 100).toFloat(),
                            color = getColorForScore(data.sessionSuccessRate)
                        )
                    }

                    // Recent Trend Score
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Recent Trend Score",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "${data.recentTrendScore.toInt()}%",
                                        style = MaterialTheme.typography.displaySmall,
                                        color = getColorForScore(data.recentTrendScore)
                                    )
                                }
                                Icon(
                                    imageVector = if (data.recentTrendScore >= 50)
                                        Icons.Default.TrendingUp
                                    else
                                        Icons.Default.TrendingDown,
                                    contentDescription = "Trend",
                                    modifier = Modifier.size(48.dp),
                                    tint = getColorForScore(data.recentTrendScore)
                                )
                            }
                        }
                    }

                    // Total Good Posture Minutes
                    item {
                        MetricCard(
                            title = "Total Good Posture Time",
                            value = "${data.totalGoodPostureMinutes.toInt()} min",
                            subtitle = "Total time with good posture",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Refresh button
                    item {
                        Button(
                            onClick = { viewModel.refreshDashboard() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isRefreshing
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Refresh Data")
                            }
                        }
                    }
                } ?: item {
                    Text(
                        text = "No dashboard data available",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

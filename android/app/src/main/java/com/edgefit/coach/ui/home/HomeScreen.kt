package com.edgefit.coach.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edgefit.coach.viewmodel.HomeViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val sessionStatus by viewModel.sessionStatus.collectAsState()
    val motivationQuote by viewModel.motivationQuote.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Posture Monitoring",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Session Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Session Status",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val status = sessionStatus?.status ?: "unknown"
                    val isRunning = status == "running"

                    Text(
                        text = if (isRunning) "RUNNING" else "STOPPED",
                        style = MaterialTheme.typography.displaySmall,
                        color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { viewModel.startSession() },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading && !isRunning
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start")
                        }

                        OutlinedButton(
                            onClick = { viewModel.stopSession() },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading && isRunning
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Stop")
                        }
                    }

                    if (isLoading) {
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Live Video Feed (Only show when running)
            if (isRunning) {
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
                                if (webView.url != streamUrl) {
                                    webView.loadUrl(streamUrl)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Motivation Quote Banner
            AnimatedVisibility(
                visible = motivationQuote != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                motivationQuote?.let { quote ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "💬 AI Coach",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = quote.quote,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "Good: ${quote.goodPostureCount}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Slouching: ${quote.slouchingCount}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "${quote.goodPercentage.toInt()}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            IconButton(onClick = { viewModel.dismissQuote() }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Start a session to begin posture monitoring on the host machine",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

package com.edgefit.coach.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.edgefit.coach.data.remote.ApiClient
import com.edgefit.coach.util.PreferencesManager
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    preferencesManager: PreferencesManager,
    onOnboardingComplete: () -> Unit
) {
    var serverIp by remember { mutableStateOf("192.168.1.1") }
    var serverPort by remember { mutableStateOf("8000") }
    var isLoading by remember { mutableStateOf(false) }
    var connectionStatus by remember { mutableStateOf<ConnectionStatus>(ConnectionStatus.Idle) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "EdgeFit Coach",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "AI-Powered Posture Monitoring",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Connect to Backend Server",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = serverIp,
            onValueChange = { serverIp = it },
            label = { Text("Server IP Address") },
            placeholder = { Text("192.168.1.1") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = serverPort,
            onValueChange = { serverPort = it },
            label = { Text("Port") },
            placeholder = { Text("8000") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    connectionStatus = ConnectionStatus.Connecting

                    try {
                        // Save configuration
                        preferencesManager.saveServerConfig(serverIp, serverPort)
                        ApiClient.updateBaseUrl()

                        // Test connection
                        val apiService = ApiClient.getApiService()
                        val response = apiService.healthCheck()

                        if (response.isSuccessful) {
                            connectionStatus = ConnectionStatus.Success
                            kotlinx.coroutines.delay(1000)
                            onOnboardingComplete()
                        } else {
                            connectionStatus = ConnectionStatus.Error("Connection failed: ${response.message()}")
                        }
                    } catch (e: Exception) {
                        connectionStatus = ConnectionStatus.Error(e.message ?: "Unknown error")
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading && serverIp.isNotBlank() && serverPort.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Connect")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (connectionStatus) {
            is ConnectionStatus.Idle -> {}
            is ConnectionStatus.Connecting -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Connecting to server...")
                }
            }
            is ConnectionStatus.Success -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Connected successfully!",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            is ConnectionStatus.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            (connectionStatus as ConnectionStatus.Error).message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "For Android Emulator, use 10.0.2.2 to reach localhost on host machine",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

sealed class ConnectionStatus {
    object Idle : ConnectionStatus()
    object Connecting : ConnectionStatus()
    object Success : ConnectionStatus()
    data class Error(val message: String) : ConnectionStatus()
}

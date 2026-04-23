package com.edgefit.coach.ui.settings

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
import androidx.compose.ui.unit.dp
import com.edgefit.coach.BuildConfig
import com.edgefit.coach.data.remote.ApiClient
import com.edgefit.coach.util.PreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferencesManager: PreferencesManager,
    onServerConfigChanged: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var serverIp by remember { mutableStateOf("") }
    var serverPort by remember { mutableStateOf("") }
    var isTestingConnection by remember { mutableStateOf(false) }
    var connectionTestResult by remember { mutableStateOf<ConnectionTestResult?>(null) }

    LaunchedEffect(Unit) {
        serverIp = preferencesManager.serverIp.first()
        serverPort = preferencesManager.serverPort.first()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Server Configuration",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = serverIp,
                onValueChange = { serverIp = it },
                label = { Text("Server IP Address") },
                placeholder = { Text("192.168.1.1") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = serverPort,
                onValueChange = { serverPort = it },
                label = { Text("Port") },
                placeholder = { Text("8000") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            isTestingConnection = true
                            connectionTestResult = null

                            try {
                                preferencesManager.saveServerConfig(serverIp, serverPort)
                                ApiClient.updateBaseUrl()

                                val apiService = ApiClient.getApiService()
                                val response = apiService.healthCheck()

                                connectionTestResult = if (response.isSuccessful) {
                                    ConnectionTestResult.Success
                                } else {
                                    ConnectionTestResult.Error("Connection failed: ${response.message()}")
                                }
                            } catch (e: Exception) {
                                connectionTestResult = ConnectionTestResult.Error(
                                    e.message ?: "Unknown error"
                                )
                            } finally {
                                isTestingConnection = false
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isTestingConnection && serverIp.isNotBlank() && serverPort.isNotBlank()
                ) {
                    if (isTestingConnection) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text("Test Connection")
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            preferencesManager.saveServerConfig(serverIp, serverPort)
                            onServerConfigChanged()
                            snackbarHostState.showSnackbar("Settings saved")
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = serverIp.isNotBlank() && serverPort.isNotBlank()
                ) {
                    Text("Save")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (connectionTestResult) {
                is ConnectionTestResult.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Connection successful!",
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                is ConnectionTestResult.Error -> {
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
                                (connectionTestResult as ConnectionTestResult.Error).message,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                null -> {}
            }

            Spacer(modifier = Modifier.height(32.dp))

            Divider()

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "App Information",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            InfoRow("Version", "1.0.0")
            InfoRow("Package", "com.edgefit.coach")
            InfoRow("Build Type", if (BuildConfig.DEBUG) "Debug" else "Release")

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💡 Tips",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• For Android Emulator, use 10.0.2.2 to reach localhost\n" +
                               "• For physical device, use your computer's LAN IP\n" +
                               "• Make sure the backend server is running on port 8000",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

sealed class ConnectionTestResult {
    object Success : ConnectionTestResult()
    data class Error(val message: String) : ConnectionTestResult()
}

package com.edgefit.coach.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edgefit.coach.data.model.DashboardData
import kotlin.math.roundToInt

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String? = null,
    progress: Float? = null,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                color = color
            )

            subtitle?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            progress?.let {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = it,
                    modifier = Modifier.fillMaxWidth(),
                    color = color,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
fun CircularMetricCard(
    title: String,
    value: Double,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                CircularProgressIndicator(
                    progress = (value / 100).toFloat(),
                    modifier = Modifier.fillMaxSize(),
                    color = color,
                    strokeWidth = 8.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Text(
                    text = "${value.roundToInt()}%",
                    style = MaterialTheme.typography.headlineMedium,
                    color = color
                )
            }
        }
    }
}

fun getColorForScore(score: Double): androidx.compose.ui.graphics.Color {
    return when {
        score >= 75.0 -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
        score >= 50.0 -> androidx.compose.ui.graphics.Color(0xFFFFC107) // Yellow
        else -> androidx.compose.ui.graphics.Color(0xFFF44336) // Red
    }
}

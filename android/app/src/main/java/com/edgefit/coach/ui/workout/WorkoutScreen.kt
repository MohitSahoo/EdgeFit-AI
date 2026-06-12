package com.edgefit.coach.ui.workout

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.edgefit.coach.exercise.FeedbackSeverity
import com.edgefit.coach.pose.CameraManager
import com.edgefit.coach.pose.PoseOverlayView
import com.edgefit.coach.viewmodel.WorkoutViewModel

@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel,
    onWorkoutComplete: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val workoutState by viewModel.workoutState.collectAsState()
    val poseResult by viewModel.poseResult.collectAsState()
    val imageSize by viewModel.imageSize.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Initialize pose detection
    LaunchedEffect(hasCameraPermission) {
        if (hasCameraPermission) {
            viewModel.initializePoseDetection(context)
        }
    }

    if (!hasCameraPermission) {
        CameraPermissionRequest(onRequestPermission = {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        })
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        var cameraManager by remember { mutableStateOf<CameraManager?>(null) }

        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { previewView ->
                    val manager = CameraManager(ctx, lifecycleOwner, previewView) { bitmap, timestamp, isFrontCamera ->
                        viewModel.processFrame(bitmap, timestamp, isFrontCamera)
                    }
                    manager.startCamera()
                    cameraManager = manager
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Pose overlay
        PoseOverlayView(
            result = poseResult,
            inputImageWidth = imageSize.first,
            inputImageHeight = imageSize.second,
            modifier = Modifier.fillMaxSize(),
            isFrontCamera = cameraManager?.isFrontCamera ?: true
        )

        // Top bar with exercise info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exercise name
                Text(
                    text = workoutState.exerciseName,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                // Duration timer
                Text(
                    text = formatDuration(workoutState.durationSeconds),
                    color = Color.White,
                    fontSize = 18.sp
                )
            }

            if (!workoutState.isExerciseLocked && workoutState.isWorkoutActive) {
                Text(
                    text = "Position yourself for exercise detection",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Rep counter (large, center)
        if (workoutState.isWorkoutActive && workoutState.repCount > 0) {
            Text(
                text = "${workoutState.repCount}",
                color = Color.White,
                fontSize = 96.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 32.dp, vertical = 8.dp)
            )
        }

        // Form feedback banner
        AnimatedVisibility(
            visible = workoutState.formFeedback.isNotEmpty() && workoutState.isWorkoutActive,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp)
        ) {
            val feedbackColor = when (workoutState.formSeverity) {
                FeedbackSeverity.GOOD -> Color(0xFF00C853)
                FeedbackSeverity.WARNING -> Color(0xFFFFAB00)
                FeedbackSeverity.ERROR -> Color(0xFFFF1744)
            }

            Text(
                text = workoutState.formFeedback,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .background(feedbackColor.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            )
        }

        // Bottom controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Form score
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Form", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Text(
                    text = "${workoutState.formScore.toInt()}%",
                    color = formScoreColor(workoutState.formScore),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Start/Stop button
            if (!workoutState.isWorkoutActive) {
                Button(
                    onClick = { viewModel.startWorkout() },
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E676)
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start", tint = Color.Black)
                }
            } else {
                Button(
                    onClick = {
                        viewModel.endWorkout()
                        onWorkoutComplete()
                    },
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF1744)
                    )
                ) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White)
                }
            }

            // Calories
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Cal", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Text(
                    text = "${workoutState.caloriesBurned.toInt()}",
                    color = Color(0xFFFFAB00),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Camera switch button
        IconButton(
            onClick = { cameraManager?.switchCamera() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 64.dp, end = 8.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Default.Cameraswitch, contentDescription = "Switch camera", tint = Color.White)
        }
    }
}

@Composable
private fun CameraPermissionRequest(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "EdgeFit needs camera access to track your exercises using pose detection.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestPermission) {
            Text("Grant Permission")
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(mins, secs)
}

private fun formScoreColor(score: Float): Color {
    return when {
        score >= 80f -> Color(0xFF00E676)
        score >= 60f -> Color(0xFFFFAB00)
        else -> Color(0xFFFF1744)
    }
}

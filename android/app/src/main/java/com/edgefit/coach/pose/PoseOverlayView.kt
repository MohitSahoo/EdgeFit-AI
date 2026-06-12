package com.edgefit.coach.pose

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

/**
 * Composable that draws pose landmarks and skeleton connections
 * as an overlay on top of the camera preview.
 */
@Composable
fun PoseOverlayView(
    result: PoseLandmarkerResult?,
    inputImageWidth: Int,
    inputImageHeight: Int,
    modifier: Modifier = Modifier,
    isFrontCamera: Boolean = true
) {
    if (result == null || result.landmarks().isEmpty()) return

    val landmarks = result.landmarks()[0] // First (only) detected pose

    Canvas(modifier = modifier) {
        val scaleX = size.width / inputImageWidth.toFloat()
        val scaleY = size.height / inputImageHeight.toFloat()

        fun landmarkToOffset(index: Int): Offset {
            val lm = landmarks[index]
            val x = if (isFrontCamera) {
                size.width - (lm.x() * size.width)
            } else {
                lm.x() * size.width
            }
            val y = lm.y() * size.height
            return Offset(x, y)
        }

        // Skeleton connections (MediaPipe Pose 33-landmark topology)
        val connections = listOf(
            // Torso
            11 to 12, 11 to 23, 12 to 24, 23 to 24,
            // Left arm
            11 to 13, 13 to 15,
            // Right arm
            12 to 14, 14 to 16,
            // Left leg
            23 to 25, 25 to 27,
            // Right leg
            24 to 26, 26 to 28,
            // Left hand
            15 to 17, 15 to 19, 17 to 19,
            // Right hand
            16 to 18, 16 to 20, 18 to 20,
            // Left foot
            27 to 29, 27 to 31, 29 to 31,
            // Right foot
            28 to 30, 28 to 32, 30 to 32
        )

        // Draw connections
        for ((start, end) in connections) {
            if (start < landmarks.size && end < landmarks.size) {
                val startLm = landmarks[start]
                val endLm = landmarks[end]

                // Only draw if both landmarks are visible enough
                if (startLm.visibility().orElse(0f) > 0.5f &&
                    endLm.visibility().orElse(0f) > 0.5f
                ) {
                    drawLine(
                        color = SkeletonColor,
                        start = landmarkToOffset(start),
                        end = landmarkToOffset(end),
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // Draw landmark points
        for (i in landmarks.indices) {
            val lm = landmarks[i]
            if (lm.visibility().orElse(0f) > 0.5f) {
                val offset = landmarkToOffset(i)
                // Outer ring
                drawCircle(
                    color = LandmarkOuterColor,
                    radius = 8f,
                    center = offset
                )
                // Inner dot
                drawCircle(
                    color = LandmarkInnerColor,
                    radius = 5f,
                    center = offset
                )
            }
        }
    }
}

// Colors for the pose overlay
private val SkeletonColor = Color(0xFF00E676) // Bright green
private val LandmarkOuterColor = Color(0xFFFF5722) // Deep orange
private val LandmarkInnerColor = Color(0xFFFFFFFF) // White

package com.edgefit.coach.pose

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import java.util.Optional

/**
 * Smooths raw MediaPipe landmarks using an exponential moving average (EMA).
 *
 * MediaPipe landmark positions are noisy frame-to-frame. Without smoothing,
 * downstream angle calculations and rep counting produce unstable results.
 *
 * Pipeline: Raw Landmarks → [PoseSmoother] → [LandmarkFilter] → [MovementTracker]
 */
class PoseSmoother(
    /** EMA alpha factor. Higher = more responsive but noisier. Range: 0..1 */
    private val alpha: Float = 0.4f
) {
    private var previousLandmarks: List<SmoothedLandmark>? = null

    /**
     * Apply EMA smoothing to a frame of landmarks.
     *
     * @param rawLandmarks The 33 raw MediaPipe pose landmarks.
     * @return Smoothed landmarks with the same structure.
     */
    fun smooth(rawLandmarks: List<NormalizedLandmark>): List<SmoothedLandmark> {
        val prev = previousLandmarks

        val smoothed = if (prev == null || prev.size != rawLandmarks.size) {
            // First frame — no smoothing possible
            rawLandmarks.map { lm ->
                SmoothedLandmark(
                    x = lm.x(),
                    y = lm.y(),
                    z = lm.z(),
                    visibility = lm.visibility().orElse(0f)
                )
            }
        } else {
            rawLandmarks.mapIndexed { i, lm ->
                val p = prev[i]
                val vis = lm.visibility().orElse(0f)

                // Only smooth if landmark is visible enough
                if (vis > 0.3f && p.visibility > 0.3f) {
                    SmoothedLandmark(
                        x = ema(p.x, lm.x(), alpha),
                        y = ema(p.y, lm.y(), alpha),
                        z = ema(p.z, lm.z(), alpha),
                        visibility = ema(p.visibility, vis, alpha)
                    )
                } else {
                    SmoothedLandmark(
                        x = lm.x(),
                        y = lm.y(),
                        z = lm.z(),
                        visibility = vis
                    )
                }
            }
        }

        previousLandmarks = smoothed
        return smoothed
    }

    /**
     * Reset smoother state (e.g., when switching exercises or after a pause).
     */
    fun reset() {
        previousLandmarks = null
    }

    private fun ema(prev: Float, current: Float, alpha: Float): Float {
        return alpha * current + (1f - alpha) * prev
    }
}

/**
 * A landmark with smoothed coordinates.
 * Decoupled from MediaPipe's NormalizedLandmark to allow mutation and filtering.
 */
data class SmoothedLandmark(
    val x: Float,
    val y: Float,
    val z: Float,
    val visibility: Float
)

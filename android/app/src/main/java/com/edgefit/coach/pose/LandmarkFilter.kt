package com.edgefit.coach.pose

/**
 * Filters out low-confidence and jittery landmarks.
 *
 * Applied after [PoseSmoother] to remove landmarks that aren't reliable
 * enough for exercise detection. Also applies velocity-based jitter rejection.
 */
class LandmarkFilter(
    /** Minimum visibility threshold. Landmarks below this are marked unreliable. */
    private val minVisibility: Float = 0.5f,
    /** Maximum allowed per-frame displacement (normalized coords). Rejects teleporting landmarks. */
    private val maxDisplacement: Float = 0.15f
) {
    private var previousLandmarks: List<SmoothedLandmark>? = null

    /**
     * Filter landmarks by visibility and jitter.
     *
     * @return A [FilteredPose] containing the filtered landmarks and a reliability flag.
     */
    fun filter(landmarks: List<SmoothedLandmark>): FilteredPose {
        val prev = previousLandmarks

        val filtered = landmarks.mapIndexed { i, lm ->
            var reliable = lm.visibility >= minVisibility

            // Jitter rejection: if a landmark teleported too far, mark unreliable
            if (reliable && prev != null && i < prev.size) {
                val dx = lm.x - prev[i].x
                val dy = lm.y - prev[i].y
                val displacement = kotlin.math.sqrt(dx * dx + dy * dy)
                if (displacement > maxDisplacement) {
                    reliable = false
                }
            }

            FilteredLandmark(
                x = lm.x,
                y = lm.y,
                z = lm.z,
                visibility = lm.visibility,
                reliable = reliable
            )
        }

        previousLandmarks = landmarks

        // A pose is considered valid if key landmarks (hips, shoulders, knees) are reliable
        val keyLandmarkIndices = listOf(11, 12, 23, 24, 25, 26) // shoulders + hips + knees
        val poseReliable = keyLandmarkIndices.all { idx ->
            idx < filtered.size && filtered[idx].reliable
        }

        return FilteredPose(
            landmarks = filtered,
            isPoseReliable = poseReliable
        )
    }

    fun reset() {
        previousLandmarks = null
    }
}

/**
 * A landmark with reliability information added by [LandmarkFilter].
 */
data class FilteredLandmark(
    val x: Float,
    val y: Float,
    val z: Float,
    val visibility: Float,
    /** Whether this landmark passed visibility + jitter checks. */
    val reliable: Boolean
)

/**
 * Complete filtered pose with per-landmark reliability and overall pose validity.
 */
data class FilteredPose(
    val landmarks: List<FilteredLandmark>,
    /** True if all key landmarks (shoulders, hips, knees) are reliable. */
    val isPoseReliable: Boolean
)

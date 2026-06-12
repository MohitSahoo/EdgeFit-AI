package com.edgefit.coach.exercise

/**
 * Counts repetitions by detecting phase transitions.
 *
 * A rep is counted when a full cycle completes:
 *   START → DESCENDING → BOTTOM → ASCENDING → START
 *
 * Includes debounce logic to prevent double-counting from noisy transitions.
 */
class RepCounter {

    companion object {
        /** Minimum time between reps in milliseconds. Prevents double-counting. */
        private const val MIN_REP_INTERVAL_MS = 500L
    }

    private var repCount: Int = 0
    private var lastPhase: ExercisePhase = ExercisePhase.START
    private var reachedBottom: Boolean = false
    private var lastRepTimestamp: Long = 0L

    /** Timestamps and form scores for each completed rep. */
    private val repHistory = mutableListOf<RepInfo>()

    /**
     * Update the counter with a new phase detection.
     *
     * @param phase Current exercise phase.
     * @param formScore Form quality score for this frame (0–100).
     * @return True if a new rep was just completed.
     */
    fun update(phase: ExercisePhase, formScore: Float = 100f): Boolean {
        val currentTime = System.currentTimeMillis()

        // Track whether we've been through the bottom of the movement
        if (phase == ExercisePhase.BOTTOM) {
            reachedBottom = true
        }

        // A rep completes when we return to START after having been at BOTTOM
        val repCompleted = reachedBottom &&
                phase == ExercisePhase.START &&
                lastPhase != ExercisePhase.START &&
                (currentTime - lastRepTimestamp) > MIN_REP_INTERVAL_MS

        if (repCompleted) {
            repCount++
            reachedBottom = false
            lastRepTimestamp = currentTime

            repHistory.add(
                RepInfo(
                    repNumber = repCount,
                    timestamp = currentTime,
                    formScore = formScore,
                    durationMs = if (repHistory.isNotEmpty()) {
                        currentTime - repHistory.last().timestamp
                    } else {
                        0L
                    }
                )
            )
        }

        lastPhase = phase
        return repCompleted
    }

    fun getRepCount(): Int = repCount

    fun getRepHistory(): List<RepInfo> = repHistory.toList()

    fun getAverageFormScore(): Float {
        if (repHistory.isEmpty()) return 0f
        return repHistory.map { it.formScore }.average().toFloat()
    }

    fun getLastRepDuration(): Long {
        return repHistory.lastOrNull()?.durationMs ?: 0L
    }

    fun reset() {
        repCount = 0
        lastPhase = ExercisePhase.START
        reachedBottom = false
        lastRepTimestamp = 0L
        repHistory.clear()
    }
}

/**
 * Information about a single completed rep.
 */
data class RepInfo(
    val repNumber: Int,
    val timestamp: Long,
    val formScore: Float,
    val durationMs: Long
)

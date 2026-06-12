package com.edgefit.coach.exercise

/**
 * Tracks a live exercise session's state.
 */
data class ExerciseSession(
    val startTimeMs: Long = System.currentTimeMillis(),
    var endTimeMs: Long? = null,
    val exerciseType: ExerciseType? = null,
    var repCount: Int = 0,
    var totalFormScore: Float = 0f,
    var formCheckCount: Int = 0,
    var caloriesBurned: Double = 0.0,
    val repInfoList: MutableList<RepInfo> = mutableListOf()
) {
    val isActive: Boolean get() = endTimeMs == null

    val durationSeconds: Int
        get() {
            val end = endTimeMs ?: System.currentTimeMillis()
            return ((end - startTimeMs) / 1000).toInt()
        }

    val averageFormScore: Float
        get() = if (formCheckCount > 0) totalFormScore / formCheckCount else 0f

    fun addRep(repInfo: RepInfo) {
        repCount++
        repInfoList.add(repInfo)
    }

    fun updateFormScore(score: Float) {
        totalFormScore += score
        formCheckCount++
    }

    fun end() {
        endTimeMs = System.currentTimeMillis()
        caloriesBurned = estimateCalories()
    }

    /**
     * Rough calorie estimation based on exercise type, reps, and duration.
     * These are approximations — not medical-grade calculations.
     */
    private fun estimateCalories(): Double {
        val minutes = durationSeconds / 60.0
        val caloriesPerMinute = when (exerciseType) {
            ExerciseType.SQUAT -> 8.0
            ExerciseType.PUSHUP -> 7.0
            ExerciseType.LUNGE -> 6.5
            ExerciseType.PLANK -> 4.0
            ExerciseType.JUMPING_JACK -> 10.0
            null -> 5.0
        }
        return caloriesPerMinute * minutes
    }
}

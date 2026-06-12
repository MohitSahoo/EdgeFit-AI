package com.edgefit.coach.pose

/**
 * Tracks body movement velocity and direction for exercise state detection.
 *
 * Sits between [LandmarkFilter] and the exercise classifier.
 * Provides movement state (MOVING_UP, MOVING_DOWN, STATIONARY) which
 * the [ExerciseClassifier] uses for rep phase detection.
 */
class MovementTracker {

    companion object {
        /** Minimum velocity magnitude (normalized/frame) to register as movement. */
        private const val VELOCITY_THRESHOLD = 0.005f
        /** Number of frames to average velocity over. */
        private const val VELOCITY_WINDOW = 5
    }

    private val hipYHistory = ArrayDeque<Float>(VELOCITY_WINDOW + 1)
    private val shoulderYHistory = ArrayDeque<Float>(VELOCITY_WINDOW + 1)

    /**
     * Update tracker with a new frame of filtered landmarks.
     *
     * @return Current [MovementState] based on hip and shoulder vertical movement.
     */
    fun update(pose: FilteredPose): MovementState {
        if (!pose.isPoseReliable) {
            return MovementState.UNKNOWN
        }

        val landmarks = pose.landmarks

        // Track hip center Y (average of left and right hip)
        val leftHip = landmarks.getOrNull(23)
        val rightHip = landmarks.getOrNull(24)
        val leftShoulder = landmarks.getOrNull(11)
        val rightShoulder = landmarks.getOrNull(12)

        if (leftHip == null || rightHip == null || leftShoulder == null || rightShoulder == null) {
            return MovementState.UNKNOWN
        }

        val hipCenterY = (leftHip.y + rightHip.y) / 2f
        val shoulderCenterY = (leftShoulder.y + rightShoulder.y) / 2f

        hipYHistory.addLast(hipCenterY)
        shoulderYHistory.addLast(shoulderCenterY)

        if (hipYHistory.size > VELOCITY_WINDOW + 1) hipYHistory.removeFirst()
        if (shoulderYHistory.size > VELOCITY_WINDOW + 1) shoulderYHistory.removeFirst()

        if (hipYHistory.size < 2) {
            return MovementState.STATIONARY
        }

        // Calculate vertical velocity (positive Y = downward in screen coords)
        val hipVelocity = hipYHistory.last() - hipYHistory.first()
        val shoulderVelocity = shoulderYHistory.last() - shoulderYHistory.first()

        // Use whichever has more movement (hips for squats, shoulders for pushups)
        val dominantVelocity = if (kotlin.math.abs(hipVelocity) > kotlin.math.abs(shoulderVelocity)) {
            hipVelocity
        } else {
            shoulderVelocity
        }

        return when {
            dominantVelocity > VELOCITY_THRESHOLD -> MovementState.MOVING_DOWN
            dominantVelocity < -VELOCITY_THRESHOLD -> MovementState.MOVING_UP
            else -> MovementState.STATIONARY
        }
    }

    /**
     * Get the current hip center Y position (normalized 0..1).
     */
    fun getHipCenterY(): Float = hipYHistory.lastOrNull() ?: 0.5f

    /**
     * Get the current shoulder center Y position (normalized 0..1).
     */
    fun getShoulderCenterY(): Float = shoulderYHistory.lastOrNull() ?: 0.3f

    fun reset() {
        hipYHistory.clear()
        shoulderYHistory.clear()
    }
}

/**
 * Vertical movement direction of the body.
 */
enum class MovementState {
    MOVING_UP,
    MOVING_DOWN,
    STATIONARY,
    UNKNOWN
}

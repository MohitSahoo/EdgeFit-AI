package com.edgefit.coach.exercise

import com.edgefit.coach.pose.FilteredPose
import com.edgefit.coach.pose.MovementState

/**
 * Squat exercise definition with state machine phase detection.
 *
 * Squat phases:
 *   START (standing) → DESCENDING → BOTTOM → ASCENDING → START (= 1 rep)
 *
 * Detection is based on knee angle:
 *   - Standing:   knee angle > 160°
 *   - Descending: knee angle decreasing, 90° < angle < 160°
 *   - Bottom:     knee angle < 100°
 *   - Ascending:  knee angle increasing from bottom
 */
class SquatDefinition : ExerciseDefinition {

    companion object {
        /** Knee angle when standing upright. */
        private const val STANDING_ANGLE = 160f
        /** Knee angle at the bottom of the squat. */
        private const val BOTTOM_ANGLE = 100f
        /** Minimum knee angle for a "full" squat (parallel or below). */
        private const val DEEP_SQUAT_ANGLE = 90f
    }

    override val exerciseType = ExerciseType.SQUAT

    override fun detectPhase(pose: FilteredPose, movementState: MovementState): ExercisePhase? {
        val leftKnee = AngleCalculator.leftKneeAngle(pose) ?: return null
        val rightKnee = AngleCalculator.rightKneeAngle(pose) ?: return null

        // Use average knee angle for robustness
        val kneeAngle = (leftKnee + rightKnee) / 2f

        return when {
            kneeAngle > STANDING_ANGLE -> ExercisePhase.START
            kneeAngle < BOTTOM_ANGLE -> ExercisePhase.BOTTOM
            movementState == MovementState.MOVING_DOWN -> ExercisePhase.DESCENDING
            movementState == MovementState.MOVING_UP -> ExercisePhase.ASCENDING
            else -> ExercisePhase.DESCENDING // Default to descending if in between
        }
    }

    override fun matchConfidence(pose: FilteredPose): Float {
        // Check if person is in a standing position (upright, legs visible)
        val leftKnee = AngleCalculator.leftKneeAngle(pose)
        val rightKnee = AngleCalculator.rightKneeAngle(pose)
        val leftHip = AngleCalculator.leftHipAngle(pose)
        val rightHip = AngleCalculator.rightHipAngle(pose)

        if (leftKnee == null || rightKnee == null) return 0f
        if (leftHip == null || rightHip == null) return 0f

        val avgKnee = (leftKnee + rightKnee) / 2f
        val avgHip = (leftHip + rightHip) / 2f

        // Person should be roughly upright with legs visible
        var confidence = 0f
        if (avgKnee > 140f) confidence += 0.3f // Legs roughly straight
        if (avgHip > 150f) confidence += 0.3f  // Torso roughly upright

        // Check if body is oriented vertically (hips below shoulders)
        val lm = pose.landmarks
        if (lm.size >= 25) {
            val shoulderY = (lm[11].y + lm[12].y) / 2f
            val hipY = (lm[23].y + lm[24].y) / 2f
            if (hipY > shoulderY) confidence += 0.4f // Upright orientation
        }

        return confidence.coerceIn(0f, 1f)
    }
}

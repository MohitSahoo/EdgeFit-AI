package com.edgefit.coach.exercise

import com.edgefit.coach.pose.FilteredPose
import com.edgefit.coach.pose.MovementState

/**
 * Interface for exercise-specific definitions.
 * Each supported exercise implements this to provide classification
 * and form analysis rules.
 */
interface ExerciseDefinition {
    /** Unique identifier for this exercise type. */
    val exerciseType: ExerciseType

    /**
     * Determine the current phase of the exercise from a pose frame.
     *
     * @return The detected [ExercisePhase], or null if the pose doesn't match this exercise.
     */
    fun detectPhase(pose: FilteredPose, movementState: MovementState): ExercisePhase?

    /**
     * Check if the person's pose matches the starting position of this exercise.
     *
     * @return Confidence score 0..1. Higher means more likely to be this exercise.
     */
    fun matchConfidence(pose: FilteredPose): Float
}

/**
 * Supported exercise types.
 */
enum class ExerciseType(val displayName: String) {
    SQUAT("Squats"),
    PUSHUP("Push-ups"),
    LUNGE("Lunges"),
    PLANK("Plank"),
    JUMPING_JACK("Jumping Jacks")
}

/**
 * Phase within a single rep of an exercise.
 */
enum class ExercisePhase {
    /** Starting/standing position. */
    START,
    /** Moving into the exercise (e.g., descending in squat). */
    DESCENDING,
    /** At the bottom/peak of the movement. */
    BOTTOM,
    /** Returning to start position (e.g., ascending in squat). */
    ASCENDING,
    /** Exercise hold position (e.g., plank, bottom of pushup hold). */
    HOLD
}

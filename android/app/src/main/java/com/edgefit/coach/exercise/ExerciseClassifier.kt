package com.edgefit.coach.exercise

import com.edgefit.coach.pose.FilteredPose
import com.edgefit.coach.pose.MovementState

/**
 * Classifies the current exercise and tracks its phase.
 *
 * Pipeline:
 *   FilteredPose + MovementState → [ExerciseClassifier] → (ExerciseType, ExercisePhase)
 *
 * Uses a voting system: the exercise definition with highest [matchConfidence]
 * for N consecutive frames becomes the active exercise.
 */
class ExerciseClassifier(
    /** Registered exercise definitions. Start with squat only; add more in Phase 5. */
    private val definitions: List<ExerciseDefinition> = listOf(SquatDefinition())
) {
    companion object {
        /** Minimum confidence to consider an exercise match. */
        private const val MIN_CONFIDENCE = 0.6f
        /** Number of consecutive frames needed to lock an exercise classification. */
        private const val LOCK_FRAMES = 10
        /** Number of frames without a match before clearing the classification. */
        private const val UNLOCK_FRAMES = 20
    }

    private var activeExercise: ExerciseDefinition? = null
    private var activePhase: ExercisePhase = ExercisePhase.START

    private var candidateExercise: ExerciseDefinition? = null
    private var candidateFrames: Int = 0
    private var noMatchFrames: Int = 0

    /**
     * Classify the current exercise and phase from a pose frame.
     *
     * @return [ClassificationResult] with the detected exercise, phase, and confidence.
     */
    fun classify(pose: FilteredPose, movementState: MovementState): ClassificationResult {
        if (!pose.isPoseReliable) {
            return ClassificationResult(
                exerciseType = activeExercise?.exerciseType,
                phase = activePhase,
                confidence = 0f,
                isLocked = activeExercise != null
            )
        }

        // If we have an active exercise, track its phase
        if (activeExercise != null) {
            val phase = activeExercise!!.detectPhase(pose, movementState)
            if (phase != null) {
                activePhase = phase
                noMatchFrames = 0
                return ClassificationResult(
                    exerciseType = activeExercise!!.exerciseType,
                    phase = activePhase,
                    confidence = 1f,
                    isLocked = true
                )
            } else {
                noMatchFrames++
                if (noMatchFrames > UNLOCK_FRAMES) {
                    activeExercise = null
                    activePhase = ExercisePhase.START
                    candidateExercise = null
                    candidateFrames = 0
                }
            }
        }

        // No active exercise — try to classify
        var bestDefinition: ExerciseDefinition? = null
        var bestConfidence = 0f

        for (definition in definitions) {
            val confidence = definition.matchConfidence(pose)
            if (confidence > bestConfidence && confidence >= MIN_CONFIDENCE) {
                bestConfidence = confidence
                bestDefinition = definition
            }
        }

        if (bestDefinition != null) {
            if (bestDefinition == candidateExercise) {
                candidateFrames++
                if (candidateFrames >= LOCK_FRAMES) {
                    // Lock this exercise
                    activeExercise = bestDefinition
                    activePhase = bestDefinition.detectPhase(pose, movementState) ?: ExercisePhase.START
                    noMatchFrames = 0
                    candidateExercise = null
                    candidateFrames = 0
                }
            } else {
                candidateExercise = bestDefinition
                candidateFrames = 1
            }
        } else {
            candidateFrames = 0
            candidateExercise = null
        }

        return ClassificationResult(
            exerciseType = activeExercise?.exerciseType ?: candidateExercise?.exerciseType,
            phase = activePhase,
            confidence = if (activeExercise != null) 1f else bestConfidence,
            isLocked = activeExercise != null
        )
    }

    /**
     * Force-set the exercise type (e.g., user selects from a menu).
     */
    fun setExercise(type: ExerciseType) {
        activeExercise = definitions.find { it.exerciseType == type }
        activePhase = ExercisePhase.START
        noMatchFrames = 0
    }

    fun reset() {
        activeExercise = null
        activePhase = ExercisePhase.START
        candidateExercise = null
        candidateFrames = 0
        noMatchFrames = 0
    }
}

/**
 * Result of exercise classification for a single frame.
 */
data class ClassificationResult(
    val exerciseType: ExerciseType?,
    val phase: ExercisePhase,
    val confidence: Float,
    /** True when the exercise has been identified with high certainty. */
    val isLocked: Boolean
)

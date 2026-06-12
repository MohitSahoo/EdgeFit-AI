package com.edgefit.coach.exercise

import com.edgefit.coach.pose.FilteredPose
import kotlin.math.abs

/**
 * Interface for per-exercise form analysis rules.
 * Each exercise defines its own set of form checks.
 */
interface FormRules {
    val exerciseType: ExerciseType

    /**
     * Analyze form quality for the current frame.
     *
     * @param pose The filtered pose landmarks.
     * @param phase The current exercise phase.
     * @return List of form feedback items (issues and praise).
     */
    fun analyze(pose: FilteredPose, phase: ExercisePhase): FormResult
}

/**
 * Result of form analysis for a single frame.
 */
data class FormResult(
    /** Overall form score for this frame (0–100). */
    val score: Float,
    /** List of feedback items (issues found or positive reinforcement). */
    val feedback: List<FormFeedback>
)

/**
 * A single piece of form feedback.
 */
data class FormFeedback(
    val message: String,
    val severity: FeedbackSeverity,
    /** Which body part this feedback applies to (for UI highlighting). */
    val bodyPart: BodyPart? = null
)

enum class FeedbackSeverity {
    GOOD,       // Positive reinforcement
    WARNING,    // Minor issue
    ERROR       // Major issue that should be corrected immediately
}

enum class BodyPart {
    KNEES, BACK, HIPS, SHOULDERS, HEAD, ANKLES, ARMS
}

/**
 * Squat form rules implementing comprehensive form checks.
 *
 * Checks:
 *  1. Minimum depth (knee angle at bottom)
 *  2. Knee tracking (knees shouldn't cave inward)
 *  3. Back alignment (torso lean)
 *  4. Symmetry (left vs right knee angles)
 *  5. Range of motion consistency
 */
class SquatFormRules : FormRules {

    override val exerciseType = ExerciseType.SQUAT

    override fun analyze(pose: FilteredPose, phase: ExercisePhase): FormResult {
        val feedback = mutableListOf<FormFeedback>()
        var totalScore = 100f
        var checkCount = 0

        // ── Check 1: Depth ──
        if (phase == ExercisePhase.BOTTOM) {
            val leftKnee = AngleCalculator.leftKneeAngle(pose)
            val rightKnee = AngleCalculator.rightKneeAngle(pose)

            if (leftKnee != null && rightKnee != null) {
                val avgKnee = (leftKnee + rightKnee) / 2f
                checkCount++

                when {
                    avgKnee > 120f -> {
                        feedback.add(
                            FormFeedback("Go deeper — aim for thighs parallel to ground", FeedbackSeverity.ERROR, BodyPart.KNEES)
                        )
                        totalScore -= 30f
                    }
                    avgKnee > 100f -> {
                        feedback.add(
                            FormFeedback("Almost there — try to go a little deeper", FeedbackSeverity.WARNING, BodyPart.KNEES)
                        )
                        totalScore -= 10f
                    }
                    else -> {
                        feedback.add(
                            FormFeedback("Great depth!", FeedbackSeverity.GOOD, BodyPart.KNEES)
                        )
                    }
                }
            }
        }

        // ── Check 2: Knee symmetry ──
        val leftKnee = AngleCalculator.leftKneeAngle(pose)
        val rightKnee = AngleCalculator.rightKneeAngle(pose)

        if (leftKnee != null && rightKnee != null) {
            checkCount++
            val asymmetry = abs(leftKnee - rightKnee)

            when {
                asymmetry > 20f -> {
                    feedback.add(
                        FormFeedback("Uneven knees — distribute weight evenly", FeedbackSeverity.ERROR, BodyPart.KNEES)
                    )
                    totalScore -= 25f
                }
                asymmetry > 10f -> {
                    feedback.add(
                        FormFeedback("Slight knee imbalance detected", FeedbackSeverity.WARNING, BodyPart.KNEES)
                    )
                    totalScore -= 10f
                }
            }
        }

        // ── Check 3: Back alignment (torso lean) ──
        val leftHip = AngleCalculator.leftHipAngle(pose)
        val rightHip = AngleCalculator.rightHipAngle(pose)

        if (leftHip != null && rightHip != null && phase != ExercisePhase.START) {
            checkCount++
            val avgHip = (leftHip + rightHip) / 2f

            when {
                avgHip < 45f -> {
                    feedback.add(
                        FormFeedback("Too much forward lean — keep chest up", FeedbackSeverity.ERROR, BodyPart.BACK)
                    )
                    totalScore -= 25f
                }
                avgHip < 70f -> {
                    feedback.add(
                        FormFeedback("Try to keep your back more upright", FeedbackSeverity.WARNING, BodyPart.BACK)
                    )
                    totalScore -= 10f
                }
            }
        }

        // ── Check 4: Shoulder symmetry ──
        val shoulderSymmetry = AngleCalculator.shoulderSymmetry(pose)
        if (shoulderSymmetry != null) {
            checkCount++
            when {
                shoulderSymmetry > 0.08f -> {
                    feedback.add(
                        FormFeedback("Keep shoulders level", FeedbackSeverity.WARNING, BodyPart.SHOULDERS)
                    )
                    totalScore -= 10f
                }
            }
        }

        // ── Check 5: Knee tracking over toes ──
        if (phase == ExercisePhase.BOTTOM || phase == ExercisePhase.DESCENDING) {
            val lm = pose.landmarks
            if (lm.size >= 29) {
                val leftKneeLm = lm[25]
                val leftAnkleLm = lm[27]
                val rightKneeLm = lm[26]
                val rightAnkleLm = lm[28]

                if (leftKneeLm.reliable && leftAnkleLm.reliable &&
                    rightKneeLm.reliable && rightAnkleLm.reliable
                ) {
                    checkCount++
                    // In normalized coords, if knee X extends way past ankle X, knees are caving
                    val leftKneeOverAnkle = abs(leftKneeLm.x - leftAnkleLm.x)
                    val rightKneeOverAnkle = abs(rightKneeLm.x - rightAnkleLm.x)

                    if (leftKneeOverAnkle > 0.1f || rightKneeOverAnkle > 0.1f) {
                        feedback.add(
                            FormFeedback("Keep knees in line with toes", FeedbackSeverity.WARNING, BodyPart.KNEES)
                        )
                        totalScore -= 15f
                    }
                }
            }
        }

        // If no form issues detected and we checked things, give positive feedback
        if (feedback.none { it.severity == FeedbackSeverity.ERROR || it.severity == FeedbackSeverity.WARNING } &&
            checkCount > 0
        ) {
            feedback.add(FormFeedback("Perfect form! 💪", FeedbackSeverity.GOOD))
        }

        return FormResult(
            score = totalScore.coerceIn(0f, 100f),
            feedback = feedback
        )
    }
}

/**
 * Main form analyzer that delegates to exercise-specific rules.
 */
class FormAnalyzer {

    private val rulesByExercise: Map<ExerciseType, FormRules> = mapOf(
        ExerciseType.SQUAT to SquatFormRules()
        // Add PushupFormRules, LungeFormRules, etc. in Phase 5
    )

    /**
     * Analyze form for the given exercise type.
     *
     * @return [FormResult] with score and feedback, or a default result if no rules exist.
     */
    fun analyze(
        exerciseType: ExerciseType?,
        pose: FilteredPose,
        phase: ExercisePhase
    ): FormResult {
        if (exerciseType == null) {
            return FormResult(score = 0f, feedback = emptyList())
        }

        val rules = rulesByExercise[exerciseType]
            ?: return FormResult(score = 100f, feedback = listOf(
                FormFeedback("Form analysis not available for ${exerciseType.displayName}", FeedbackSeverity.WARNING)
            ))

        return rules.analyze(pose, phase)
    }
}

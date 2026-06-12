package com.edgefit.coach.exercise

import com.edgefit.coach.pose.FilteredLandmark
import com.edgefit.coach.pose.FilteredPose
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Calculates joint angles from filtered pose landmarks.
 *
 * All angles are in degrees (0–180).
 * Uses the standard 3-point angle calculation via atan2.
 *
 * MediaPipe Pose landmark indices:
 *   0: nose, 11: left shoulder, 12: right shoulder,
 *   13: left elbow, 14: right elbow, 15: left wrist, 16: right wrist,
 *   23: left hip, 24: right hip, 25: left knee, 26: right knee,
 *   27: left ankle, 28: right ankle
 */
object AngleCalculator {

    /**
     * Calculate the angle at point B formed by segments BA and BC.
     *
     * @return Angle in degrees (0–180).
     */
    fun calculateAngle(
        a: FilteredLandmark,
        b: FilteredLandmark,
        c: FilteredLandmark
    ): Float {
        val radians = atan2(
            (c.y - b.y).toDouble(),
            (c.x - b.x).toDouble()
        ) - atan2(
            (a.y - b.y).toDouble(),
            (a.x - b.x).toDouble()
        )

        var angle = abs(radians * 180.0 / PI).toFloat()
        if (angle > 180f) angle = 360f - angle
        return angle
    }

    // ─── Joint Angle Helpers ───

    /**
     * Left knee angle: hip–knee–ankle.
     */
    fun leftKneeAngle(pose: FilteredPose): Float? {
        val lm = pose.landmarks
        if (lm.size < 28) return null
        val hip = lm[23]; val knee = lm[25]; val ankle = lm[27]
        if (!hip.reliable || !knee.reliable || !ankle.reliable) return null
        return calculateAngle(hip, knee, ankle)
    }

    /**
     * Right knee angle: hip–knee–ankle.
     */
    fun rightKneeAngle(pose: FilteredPose): Float? {
        val lm = pose.landmarks
        if (lm.size < 29) return null
        val hip = lm[24]; val knee = lm[26]; val ankle = lm[28]
        if (!hip.reliable || !knee.reliable || !ankle.reliable) return null
        return calculateAngle(hip, knee, ankle)
    }

    /**
     * Left hip angle: shoulder–hip–knee.
     */
    fun leftHipAngle(pose: FilteredPose): Float? {
        val lm = pose.landmarks
        if (lm.size < 26) return null
        val shoulder = lm[11]; val hip = lm[23]; val knee = lm[25]
        if (!shoulder.reliable || !hip.reliable || !knee.reliable) return null
        return calculateAngle(shoulder, hip, knee)
    }

    /**
     * Right hip angle: shoulder–hip–knee.
     */
    fun rightHipAngle(pose: FilteredPose): Float? {
        val lm = pose.landmarks
        if (lm.size < 27) return null
        val shoulder = lm[12]; val hip = lm[24]; val knee = lm[26]
        if (!shoulder.reliable || !hip.reliable || !knee.reliable) return null
        return calculateAngle(shoulder, hip, knee)
    }

    /**
     * Left elbow angle: shoulder–elbow–wrist.
     */
    fun leftElbowAngle(pose: FilteredPose): Float? {
        val lm = pose.landmarks
        if (lm.size < 16) return null
        val shoulder = lm[11]; val elbow = lm[13]; val wrist = lm[15]
        if (!shoulder.reliable || !elbow.reliable || !wrist.reliable) return null
        return calculateAngle(shoulder, elbow, wrist)
    }

    /**
     * Right elbow angle: shoulder–elbow–wrist.
     */
    fun rightElbowAngle(pose: FilteredPose): Float? {
        val lm = pose.landmarks
        if (lm.size < 17) return null
        val shoulder = lm[12]; val elbow = lm[14]; val wrist = lm[16]
        if (!shoulder.reliable || !elbow.reliable || !wrist.reliable) return null
        return calculateAngle(shoulder, elbow, wrist)
    }

    /**
     * Left shoulder angle: elbow–shoulder–hip.
     */
    fun leftShoulderAngle(pose: FilteredPose): Float? {
        val lm = pose.landmarks
        if (lm.size < 24) return null
        val elbow = lm[13]; val shoulder = lm[11]; val hip = lm[23]
        if (!elbow.reliable || !shoulder.reliable || !hip.reliable) return null
        return calculateAngle(elbow, shoulder, hip)
    }

    /**
     * Right shoulder angle: elbow–shoulder–hip.
     */
    fun rightShoulderAngle(pose: FilteredPose): Float? {
        val lm = pose.landmarks
        if (lm.size < 25) return null
        val elbow = lm[14]; val shoulder = lm[12]; val hip = lm[24]
        if (!elbow.reliable || !shoulder.reliable || !hip.reliable) return null
        return calculateAngle(elbow, shoulder, hip)
    }

    // ─── Posture Metrics ───

    /**
     * Posture ratio: vertical distance from nose to shoulder line / shoulder width.
     * Ported from Python main_webcam_headless1.py posture detection logic.
     *
     * Higher ratio = more upright. Typical threshold: 0.45
     */
    fun postureRatio(pose: FilteredPose): Float? {
        val lm = pose.landmarks
        if (lm.size < 13) return null
        val nose = lm[0]; val leftShoulder = lm[11]; val rightShoulder = lm[12]
        if (!nose.reliable || !leftShoulder.reliable || !rightShoulder.reliable) return null

        val shoulderWidth = abs(rightShoulder.x - leftShoulder.x)
        if (shoulderWidth < 0.01f) return null

        val shoulderLineY = (leftShoulder.y + rightShoulder.y) / 2f
        val postureDistance = shoulderLineY - nose.y

        return postureDistance / shoulderWidth
    }

    /**
     * Shoulder symmetry: difference in Y position between left and right shoulders.
     * Lower = more symmetric. Useful for form analysis.
     */
    fun shoulderSymmetry(pose: FilteredPose): Float? {
        val lm = pose.landmarks
        if (lm.size < 13) return null
        val leftShoulder = lm[11]; val rightShoulder = lm[12]
        if (!leftShoulder.reliable || !rightShoulder.reliable) return null
        return abs(leftShoulder.y - rightShoulder.y)
    }

    /**
     * Distance between two landmarks (normalized coordinates).
     */
    fun distance(a: FilteredLandmark, b: FilteredLandmark): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return sqrt(dx * dx + dy * dy)
    }
}

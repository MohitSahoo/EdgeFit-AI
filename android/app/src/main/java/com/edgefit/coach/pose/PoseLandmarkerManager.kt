package com.edgefit.coach.pose

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

/**
 * Manages the MediaPipe Pose Landmarker for on-device pose estimation.
 *
 * Lifecycle:
 *   initialize() → detectAsync(bitmap, timestamp) → close()
 *
 * Results are delivered via [PoseLandmarkerListener].
 */
class PoseLandmarkerManager(
    private val context: Context,
    private val listener: PoseLandmarkerListener
) {
    companion object {
        private const val TAG = "PoseLandmarkerManager"
        private const val MODEL_ASSET = "pose_landmarker_lite.task"
        private const val DEFAULT_MIN_DETECTION_CONFIDENCE = 0.5f
        private const val DEFAULT_MIN_TRACKING_CONFIDENCE = 0.5f
        private const val DEFAULT_MIN_PRESENCE_CONFIDENCE = 0.5f
        private const val NUM_POSES = 1
    }

    private var poseLandmarker: PoseLandmarker? = null

    var minDetectionConfidence = DEFAULT_MIN_DETECTION_CONFIDENCE
        private set
    var minTrackingConfidence = DEFAULT_MIN_TRACKING_CONFIDENCE
        private set
    var minPresenceConfidence = DEFAULT_MIN_PRESENCE_CONFIDENCE
        private set

    /**
     * Initialize the PoseLandmarker with GPU delegate (falls back to CPU).
     */
    fun initialize() {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(MODEL_ASSET)
                .setDelegate(Delegate.GPU)
                .build()

            val options = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setNumPoses(NUM_POSES)
                .setMinPoseDetectionConfidence(minDetectionConfidence)
                .setMinTrackingConfidence(minTrackingConfidence)
                .setMinPosePresenceConfidence(minPresenceConfidence)
                .setResultListener(this::onResult)
                .setErrorListener(this::onError)
                .build()

            poseLandmarker = PoseLandmarker.createFromOptions(context, options)
            Log.i(TAG, "PoseLandmarker initialized with GPU delegate")
        } catch (gpuException: Exception) {
            Log.w(TAG, "GPU delegate failed, falling back to CPU: ${gpuException.message}")
            initializeCpu()
        }
    }

    private fun initializeCpu() {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(MODEL_ASSET)
                .setDelegate(Delegate.CPU)
                .build()

            val options = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setNumPoses(NUM_POSES)
                .setMinPoseDetectionConfidence(minDetectionConfidence)
                .setMinTrackingConfidence(minTrackingConfidence)
                .setMinPosePresenceConfidence(minPresenceConfidence)
                .setResultListener(this::onResult)
                .setErrorListener(this::onError)
                .build()

            poseLandmarker = PoseLandmarker.createFromOptions(context, options)
            Log.i(TAG, "PoseLandmarker initialized with CPU delegate")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize PoseLandmarker: ${e.message}")
            listener.onPoseError("Failed to initialize pose detection: ${e.message}")
        }
    }

    /**
     * Process a camera frame for pose detection.
     *
     * @param bitmap The camera frame as a Bitmap (ARGB_8888).
     * @param timestampMs Frame timestamp in milliseconds.
     * @param isFrontCamera Whether the frame is from the front camera (needs horizontal flip).
     */
    fun detectAsync(bitmap: Bitmap, timestampMs: Long, isFrontCamera: Boolean = true) {
        val landmarker = poseLandmarker ?: return

        // Front camera images are mirrored — flip for correct landmark positions
        val processedBitmap = if (isFrontCamera) {
            val matrix = Matrix().apply { preScale(-1f, 1f) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
        } else {
            bitmap
        }

        val mpImage = BitmapImageBuilder(processedBitmap).build()
        landmarker.detectAsync(mpImage, timestampMs)
    }

    /**
     * Update detection thresholds. Requires re-initialization.
     */
    fun updateThresholds(
        detectionConfidence: Float = minDetectionConfidence,
        trackingConfidence: Float = minTrackingConfidence,
        presenceConfidence: Float = minPresenceConfidence
    ) {
        minDetectionConfidence = detectionConfidence
        minTrackingConfidence = trackingConfidence
        minPresenceConfidence = presenceConfidence
        close()
        initialize()
    }

    fun close() {
        poseLandmarker?.close()
        poseLandmarker = null
    }

    fun isInitialized(): Boolean = poseLandmarker != null

    private fun onResult(result: PoseLandmarkerResult, input: com.google.mediapipe.framework.image.MPImage) {
        listener.onPoseResult(result, input.width, input.height)
    }

    private fun onError(error: RuntimeException) {
        Log.e(TAG, "Pose detection error: ${error.message}")
        listener.onPoseError(error.message ?: "Unknown pose detection error")
    }
}

/**
 * Callback interface for pose detection results.
 */
interface PoseLandmarkerListener {
    /**
     * Called when pose landmarks are detected.
     *
     * @param result The pose landmarker result containing landmarks for detected poses.
     * @param inputImageWidth Width of the input image (for coordinate scaling).
     * @param inputImageHeight Height of the input image (for coordinate scaling).
     */
    fun onPoseResult(result: PoseLandmarkerResult, inputImageWidth: Int, inputImageHeight: Int)

    /**
     * Called when a pose detection error occurs.
     */
    fun onPoseError(error: String)
}

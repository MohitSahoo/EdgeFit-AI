package com.edgefit.coach.pose

import android.content.Context
import android.util.Log
import android.util.Size
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Manages CameraX lifecycle and feeds frames to [PoseLandmarkerManager].
 *
 * Provides:
 *  - Preview use case for live viewfinder
 *  - ImageAnalysis use case feeding frames to pose detection
 *  - Camera switching (front/back)
 */
class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView,
    private val onFrameAvailable: (bitmap: android.graphics.Bitmap, timestampMs: Long, isFrontCamera: Boolean) -> Unit
) {
    companion object {
        private const val TAG = "CameraManager"
        private val TARGET_RESOLUTION = Size(640, 480)
    }

    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private var lensFacing = CameraSelector.LENS_FACING_FRONT
    val isFrontCamera: Boolean get() = lensFacing == CameraSelector.LENS_FACING_FRONT

    /**
     * Start the camera with preview and frame analysis.
     */
    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindCameraUseCases() {
        val provider = cameraProvider ?: run {
            Log.e(TAG, "CameraProvider is null")
            return
        }

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        // Preview use case
        preview = Preview.Builder()
            .setTargetResolution(TARGET_RESOLUTION)
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        // ImageAnalysis use case — feeds frames to pose detection
        imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(TARGET_RESOLUTION)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    processFrame(imageProxy)
                }
            }

        // Unbind all use cases before rebinding
        provider.unbindAll()

        try {
            camera = provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
            Log.i(TAG, "Camera bound successfully (${if (isFrontCamera) "front" else "back"})")
        } catch (e: Exception) {
            Log.e(TAG, "Camera binding failed: ${e.message}")
        }
    }

    private fun processFrame(imageProxy: ImageProxy) {
        val bitmap = imageProxy.toBitmap()
        val timestampMs = imageProxy.imageInfo.timestamp / 1_000 // Convert from ns to us... actually MediaPipe uses ms
        val frameTimestampMs = System.currentTimeMillis()

        onFrameAvailable(bitmap, frameTimestampMs, isFrontCamera)

        imageProxy.close()
    }

    /**
     * Switch between front and back cameras.
     */
    fun switchCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }
        bindCameraUseCases()
    }

    /**
     * Toggle the device torch (flashlight).
     */
    fun toggleTorch() {
        camera?.let {
            if (it.cameraInfo.hasFlashUnit()) {
                val currentState = it.cameraInfo.torchState.value == TorchState.ON
                it.cameraControl.enableTorch(!currentState)
            }
        }
    }

    fun stopCamera() {
        cameraProvider?.unbindAll()
    }

    fun release() {
        stopCamera()
        cameraExecutor.shutdown()
    }
}

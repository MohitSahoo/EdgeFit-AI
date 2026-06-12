package com.edgefit.coach.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgefit.coach.data.local.WorkoutRepository
import com.edgefit.coach.exercise.*
import com.edgefit.coach.pose.*
import com.edgefit.coach.service.WorkoutService
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel(), PoseLandmarkerListener {

    // ─── Pose pipeline ───
    private val poseSmoother = PoseSmoother(alpha = 0.4f)
    private val landmarkFilter = LandmarkFilter()
    private val movementTracker = MovementTracker()

    // ─── Exercise engine ───
    private val exerciseClassifier = ExerciseClassifier()
    private val repCounter = RepCounter()
    private val formAnalyzer = FormAnalyzer()

    // ─── PoseLandmarkerManager ───
    private var poseLandmarkerManager: PoseLandmarkerManager? = null

    // ─── State ───
    private val _workoutState = MutableStateFlow(WorkoutUiState())
    val workoutState: StateFlow<WorkoutUiState> = _workoutState.asStateFlow()

    private val _poseResult = MutableStateFlow<PoseLandmarkerResult?>(null)
    val poseResult: StateFlow<PoseLandmarkerResult?> = _poseResult.asStateFlow()

    private val _imageSize = MutableStateFlow(Pair(640, 480))
    val imageSize: StateFlow<Pair<Int, Int>> = _imageSize.asStateFlow()

    private var currentSession: ExerciseSession? = null
    private var sessionId: Long? = null

    // ─── Lifecycle ───

    fun initializePoseDetection(context: Context) {
        if (poseLandmarkerManager == null) {
            poseLandmarkerManager = PoseLandmarkerManager(context, this)
            poseLandmarkerManager?.initialize()
        }
    }

    fun processFrame(bitmap: android.graphics.Bitmap, timestampMs: Long, isFrontCamera: Boolean) {
        poseLandmarkerManager?.detectAsync(bitmap, timestampMs, isFrontCamera)
    }

    // ─── PoseLandmarkerListener ───

    override fun onPoseResult(result: PoseLandmarkerResult, inputImageWidth: Int, inputImageHeight: Int) {
        _poseResult.value = result
        _imageSize.value = Pair(inputImageWidth, inputImageHeight)

        if (result.landmarks().isEmpty()) return

        val rawLandmarks = result.landmarks()[0]
        if (rawLandmarks.isEmpty()) return

        // Pipeline: Raw → Smooth → Filter → Movement → Classify → RepCount → FormAnalyze
        val smoothed = poseSmoother.smooth(rawLandmarks)
        val filtered = landmarkFilter.filter(smoothed)
        val movementState = movementTracker.update(filtered)
        val classification = exerciseClassifier.classify(filtered, movementState)

        // Form analysis
        val formResult = formAnalyzer.analyze(classification.exerciseType, filtered, classification.phase)

        // Rep counting
        val repCompleted = repCounter.update(classification.phase, formResult.score)

        if (repCompleted) {
            currentSession?.let { session ->
                val repHistory = repCounter.getRepHistory()
                if (repHistory.isNotEmpty()) {
                    session.addRep(repHistory.last())
                }
            }
        }

        // Update UI state
        _workoutState.value = _workoutState.value.copy(
            exerciseType = classification.exerciseType,
            exerciseName = classification.exerciseType?.displayName ?: "Detecting...",
            phase = classification.phase,
            isExerciseLocked = classification.isLocked,
            repCount = repCounter.getRepCount(),
            formScore = formResult.score,
            formFeedback = formResult.feedback.firstOrNull { it.severity != FeedbackSeverity.GOOD }?.message
                ?: formResult.feedback.firstOrNull()?.message ?: "",
            formSeverity = formResult.feedback.maxByOrNull { it.severity.ordinal }?.severity
                ?: FeedbackSeverity.GOOD,
            durationSeconds = currentSession?.durationSeconds ?: 0,
            caloriesBurned = currentSession?.caloriesBurned ?: 0.0,
            isPoseDetected = true
        )
    }

    override fun onPoseError(error: String) {
        _workoutState.value = _workoutState.value.copy(
            error = error,
            isPoseDetected = false
        )
    }

    // ─── Workout Controls ───

    fun startWorkout(exerciseType: ExerciseType? = null) {
        viewModelScope.launch {
            // Reset engine
            poseSmoother.reset()
            landmarkFilter.reset()
            movementTracker.reset()
            exerciseClassifier.reset()
            repCounter.reset()

            if (exerciseType != null) {
                exerciseClassifier.setExercise(exerciseType)
            }

            currentSession = ExerciseSession(exerciseType = exerciseType)
            sessionId = workoutRepository.startSession()

            _workoutState.value = WorkoutUiState(
                isWorkoutActive = true,
                exerciseName = exerciseType?.displayName ?: "Auto-detect"
            )

            // Start foreground service
            val intent = Intent(appContext, WorkoutService::class.java).apply {
                action = WorkoutService.ACTION_START
            }
            appContext.startForegroundService(intent)
        }
    }

    fun endWorkout() {
        viewModelScope.launch {
            currentSession?.end()

            val session = currentSession
            val sId = sessionId

            if (session != null && sId != null) {
                workoutRepository.saveExerciseRecord(sId, session)
                workoutRepository.endSession(sId, session)
            }

            _workoutState.value = _workoutState.value.copy(
                isWorkoutActive = false,
                isWorkoutComplete = true
            )

            // Stop foreground service
            val intent = Intent(appContext, WorkoutService::class.java).apply {
                action = WorkoutService.ACTION_STOP
            }
            appContext.startService(intent)
        }
    }

    fun getSessionSummary(): ExerciseSession? = currentSession

    override fun onCleared() {
        super.onCleared()
        poseLandmarkerManager?.close()
    }
}

/**
 * UI state for the workout screen.
 */
data class WorkoutUiState(
    val isWorkoutActive: Boolean = false,
    val isWorkoutComplete: Boolean = false,
    val exerciseType: ExerciseType? = null,
    val exerciseName: String = "",
    val phase: ExercisePhase = ExercisePhase.START,
    val isExerciseLocked: Boolean = false,
    val repCount: Int = 0,
    val formScore: Float = 0f,
    val formFeedback: String = "",
    val formSeverity: FeedbackSeverity = FeedbackSeverity.GOOD,
    val durationSeconds: Int = 0,
    val caloriesBurned: Double = 0.0,
    val isPoseDetected: Boolean = false,
    val error: String? = null
)

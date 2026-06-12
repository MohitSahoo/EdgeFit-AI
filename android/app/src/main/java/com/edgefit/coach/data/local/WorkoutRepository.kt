package com.edgefit.coach.data.local

import com.edgefit.coach.data.local.dao.WorkoutDao
import com.edgefit.coach.data.local.entity.*
import com.edgefit.coach.exercise.ExerciseSession
import com.edgefit.coach.exercise.ExerciseType
import kotlinx.coroutines.flow.Flow

/**
 * Repository wrapping [WorkoutDao] with business logic.
 * Single source of truth for all workout data.
 */
class WorkoutRepository(private val dao: WorkoutDao) {

    // ─── Session Management ───

    suspend fun startSession(): Long {
        val session = WorkoutSessionEntity(
            startTime = System.currentTimeMillis()
        )
        return dao.insertSession(session)
    }

    suspend fun endSession(
        sessionId: Long,
        exerciseSession: ExerciseSession
    ) {
        val existing = dao.getSession(sessionId) ?: return
        val updated = existing.copy(
            endTime = System.currentTimeMillis(),
            totalDurationSeconds = exerciseSession.durationSeconds,
            totalCalories = exerciseSession.caloriesBurned,
            averageFormScore = exerciseSession.averageFormScore.toDouble()
        )
        dao.updateSession(updated)
    }

    suspend fun saveExerciseRecord(
        sessionId: Long,
        exerciseSession: ExerciseSession
    ): Long {
        val record = ExerciseRecordEntity(
            sessionId = sessionId,
            exerciseType = exerciseSession.exerciseType?.name ?: "UNKNOWN",
            reps = exerciseSession.repCount,
            durationSeconds = exerciseSession.durationSeconds,
            averageFormScore = exerciseSession.averageFormScore.toDouble(),
            caloriesBurned = exerciseSession.caloriesBurned,
            timestamp = System.currentTimeMillis()
        )
        val recordId = dao.insertExerciseRecord(record)

        // Save individual rep records
        val repRecords = exerciseSession.repInfoList.map { rep ->
            RepRecordEntity(
                exerciseRecordId = recordId,
                repNumber = rep.repNumber,
                formScore = rep.formScore.toDouble(),
                durationMs = rep.durationMs,
                timestamp = rep.timestamp
            )
        }
        if (repRecords.isNotEmpty()) {
            dao.insertRepRecords(repRecords)
        }

        return recordId
    }

    // ─── Queries ───

    fun getAllSessions(): Flow<List<WorkoutSessionEntity>> = dao.getAllSessions()

    fun getSessionsByDateRange(startTime: Long, endTime: Long) =
        dao.getSessionsByDateRange(startTime, endTime)

    fun getExerciseRecords(sessionId: Long) = dao.getExerciseRecords(sessionId)

    fun getTotalWorkoutCount(): Flow<Int> = dao.getTotalWorkoutCount()

    fun getTotalCalories(): Flow<Double> = dao.getTotalCalories()

    fun getTotalReps(): Flow<Int> = dao.getTotalReps()

    fun getAverageFormScore(): Flow<Double> = dao.getAverageFormScore()

    fun getExerciseDistribution() = dao.getExerciseDistribution()

    fun getDailySummaries(startTime: Long) = dao.getDailySummaries(startTime)

    fun getCurrentStreak(): Flow<Int> = dao.getCurrentStreak()

    // ─── User Profile ───

    suspend fun saveProfile(profile: UserProfileEntity) = dao.insertOrUpdateProfile(profile)

    fun getProfile() = dao.getProfile()

    // ─── Goals ───

    suspend fun createGoal(goal: GoalEntity): Long = dao.insertGoal(goal)

    suspend fun updateGoal(goal: GoalEntity) = dao.updateGoal(goal)

    suspend fun deleteGoal(goal: GoalEntity) = dao.deleteGoal(goal)

    fun getActiveGoals() = dao.getActiveGoals()

    fun getCompletedGoals() = dao.getCompletedGoals()

    // ─── Workout Plans ───

    suspend fun savePlan(plan: WorkoutPlanEntity, exercises: List<WorkoutPlanExerciseEntity>): Long {
        val planId = dao.insertPlan(plan)
        val exercisesWithPlanId = exercises.map { it.copy(planId = planId) }
        dao.insertPlanExercises(exercisesWithPlanId)
        return planId
    }

    fun getAllPlans() = dao.getAllPlans()

    fun getPlanExercises(planId: Long) = dao.getPlanExercises(planId)
}

package com.edgefit.coach.data.local.dao

import androidx.room.*
import com.edgefit.coach.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    // ─── WorkoutSession ───

    @Insert
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    @Query("SELECT * FROM workout_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    suspend fun getSession(sessionId: Long): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE startTime >= :startTime AND startTime <= :endTime ORDER BY startTime DESC")
    fun getSessionsByDateRange(startTime: Long, endTime: Long): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT COUNT(*) FROM workout_sessions")
    fun getTotalWorkoutCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(totalCalories), 0.0) FROM workout_sessions")
    fun getTotalCalories(): Flow<Double>

    @Query("SELECT COALESCE(AVG(averageFormScore), 0.0) FROM workout_sessions")
    fun getAverageFormScore(): Flow<Double>

    // ─── ExerciseRecord ───

    @Insert
    suspend fun insertExerciseRecord(record: ExerciseRecordEntity): Long

    @Query("SELECT * FROM exercise_records WHERE sessionId = :sessionId")
    fun getExerciseRecords(sessionId: Long): Flow<List<ExerciseRecordEntity>>

    @Query("SELECT exerciseType, COUNT(*) as count FROM exercise_records GROUP BY exerciseType ORDER BY count DESC")
    fun getExerciseDistribution(): Flow<List<ExerciseCount>>

    @Query("SELECT COALESCE(SUM(reps), 0) FROM exercise_records")
    fun getTotalReps(): Flow<Int>

    // ─── RepRecord ───

    @Insert
    suspend fun insertRepRecord(record: RepRecordEntity)

    @Insert
    suspend fun insertRepRecords(records: List<RepRecordEntity>)

    @Query("SELECT * FROM rep_records WHERE exerciseRecordId = :exerciseRecordId ORDER BY repNumber")
    fun getRepRecords(exerciseRecordId: Long): Flow<List<RepRecordEntity>>

    // ─── UserProfile ───

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getProfile(): Flow<UserProfileEntity?>

    // ─── Goals ───

    @Insert
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    @Query("SELECT * FROM goals WHERE isCompleted = 0 ORDER BY startDate DESC")
    fun getActiveGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedGoals(): Flow<List<GoalEntity>>

    // ─── WorkoutPlan ───

    @Insert
    suspend fun insertPlan(plan: WorkoutPlanEntity): Long

    @Query("SELECT * FROM workout_plans ORDER BY createdAt DESC")
    fun getAllPlans(): Flow<List<WorkoutPlanEntity>>

    @Delete
    suspend fun deletePlan(plan: WorkoutPlanEntity)

    @Insert
    suspend fun insertPlanExercises(exercises: List<WorkoutPlanExerciseEntity>)

    @Query("SELECT * FROM workout_plan_exercises WHERE planId = :planId ORDER BY orderIndex")
    fun getPlanExercises(planId: Long): Flow<List<WorkoutPlanExerciseEntity>>

    // ─── Aggregation queries (replaces DailySummary entity) ───

    @Query("""
        SELECT date(startTime / 1000, 'unixepoch', 'localtime') as date,
               COUNT(*) as workoutCount,
               COALESCE(SUM(totalCalories), 0.0) as totalCalories,
               COALESCE(SUM(totalDurationSeconds), 0) as totalDuration,
               COALESCE(AVG(averageFormScore), 0.0) as avgFormScore
        FROM workout_sessions
        WHERE startTime >= :startTime
        GROUP BY date(startTime / 1000, 'unixepoch', 'localtime')
        ORDER BY date DESC
    """)
    fun getDailySummaries(startTime: Long): Flow<List<DailySummaryProjection>>

    @Query("""
        SELECT COALESCE(MAX(streakDays), 0) FROM (
            SELECT COUNT(*) as streakDays FROM (
                SELECT date(startTime / 1000, 'unixepoch', 'localtime') as d,
                       julianday('now', 'localtime') - julianday(date(startTime / 1000, 'unixepoch', 'localtime')) as daysAgo
                FROM workout_sessions
                GROUP BY d
                HAVING daysAgo = (
                    SELECT COUNT(DISTINCT date(s2.startTime / 1000, 'unixepoch', 'localtime'))
                    FROM workout_sessions s2
                    WHERE date(s2.startTime / 1000, 'unixepoch', 'localtime') > date(startTime / 1000, 'unixepoch', 'localtime')
                      AND date(s2.startTime / 1000, 'unixepoch', 'localtime') <= date('now', 'localtime')
                )
            )
        )
    """)
    fun getCurrentStreak(): Flow<Int>
}

/**
 * Projection for exercise distribution query.
 */
data class ExerciseCount(
    val exerciseType: String,
    val count: Int
)

/**
 * Projection for daily summary aggregation (NOT stored — computed from raw data).
 */
data class DailySummaryProjection(
    val date: String,
    val workoutCount: Int,
    val totalCalories: Double,
    val totalDuration: Int,
    val avgFormScore: Double
)

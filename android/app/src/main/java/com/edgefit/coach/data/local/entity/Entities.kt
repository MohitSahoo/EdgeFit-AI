package com.edgefit.coach.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,
    val endTime: Long? = null,
    val totalDurationSeconds: Int = 0,
    val totalCalories: Double = 0.0,
    val averageFormScore: Double = 0.0,
    val notes: String? = null
)

@Entity(tableName = "exercise_records")
data class ExerciseRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseType: String,
    val reps: Int,
    val durationSeconds: Int,
    val averageFormScore: Double,
    val caloriesBurned: Double,
    val timestamp: Long
)

@Entity(tableName = "rep_records")
data class RepRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseRecordId: Long,
    val repNumber: Int,
    val formScore: Double,
    val durationMs: Long,
    val feedback: String? = null,
    val timestamp: Long
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val displayName: String = "Athlete",
    val heightCm: Float? = null,
    val weightKg: Float? = null,
    val fitnessLevel: String = "beginner", // beginner, intermediate, advanced
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String? = null,
    val targetValue: Double,
    val currentValue: Double = 0.0,
    val unit: String, // "reps", "minutes", "days", "kg"
    val goalType: String, // "daily", "weekly", "total", "streak"
    val exerciseType: String? = null, // null = applies to all
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null
)

@Entity(tableName = "workout_plans")
data class WorkoutPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String? = null,
    val difficulty: String = "beginner", // beginner, intermediate, advanced
    val estimatedDurationMinutes: Int = 30,
    val isAiGenerated: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "workout_plan_exercises")
data class WorkoutPlanExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long,
    val exerciseType: String,
    val targetReps: Int? = null,
    val targetDurationSeconds: Int? = null,
    val sets: Int = 1,
    val restBetweenSetsSeconds: Int = 60,
    val orderIndex: Int
)

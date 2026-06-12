package com.edgefit.coach.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.edgefit.coach.data.local.dao.WorkoutDao
import com.edgefit.coach.data.local.entity.*

@Database(
    entities = [
        WorkoutSessionEntity::class,
        ExerciseRecordEntity::class,
        RepRecordEntity::class,
        UserProfileEntity::class,
        GoalEntity::class,
        WorkoutPlanEntity::class,
        WorkoutPlanExerciseEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class EdgeFitDatabase : RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: EdgeFitDatabase? = null

        fun getDatabase(context: Context): EdgeFitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EdgeFitDatabase::class.java,
                    "edgefit_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

package com.mostgymapp.app.data.db.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.mostgymapp.app.data.db.entity.ExerciseEntity
import com.mostgymapp.app.data.db.entity.SetEntity
import com.mostgymapp.app.data.db.entity.WorkoutEntity
import com.mostgymapp.app.data.db.entity.WorkoutExerciseEntity

data class WorkoutExerciseWithSets(
    @Embedded val workoutExercise: WorkoutExerciseEntity,
    @Relation(parentColumn = "exerciseId", entityColumn = "id")
    val exercise: ExerciseEntity,
    @Relation(parentColumn = "id", entityColumn = "workoutExerciseId")
    val sets: List<SetEntity>
)

data class WorkoutWithExercisesAndSets(
    @Embedded val workout: WorkoutEntity,
    @Relation(
        entity = WorkoutExerciseEntity::class,
        parentColumn = "id",
        entityColumn = "workoutId"
    )
    val exercises: List<WorkoutExerciseWithSets>
)

data class WorkoutHistoryRow(
    val id: Long,
    val startTime: Long,
    val endTime: Long,
    val exerciseCount: Int,
    val volume: Double
)

data class LastSetValuesRow(
    val weight: Double,
    val reps: Int,
    val rpe: Double?
)

data class SetWithWorkoutDateRow(
    val setId: Long,
    val workoutExerciseId: Long,
    val weight: Double,
    val reps: Int,
    val rpe: Double?,
    val createdAt: Long,
    val workoutStartTime: Long
)

data class ExerciseStatsPointRow(
    val day: String,
    val bestE1rm: Double,
    val tonnage: Double
)

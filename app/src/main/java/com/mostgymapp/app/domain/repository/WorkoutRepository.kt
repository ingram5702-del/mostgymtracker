package com.mostgymapp.app.domain.repository

import com.mostgymapp.app.domain.model.ActiveWorkout
import com.mostgymapp.app.domain.model.AddSetInput
import com.mostgymapp.app.domain.model.LastSetValues
import com.mostgymapp.app.domain.model.WorkoutDetail
import com.mostgymapp.app.domain.model.WorkoutHistoryItem
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun observeActiveWorkout(): Flow<ActiveWorkout?>
    fun observeHistory(): Flow<List<WorkoutHistoryItem>>
    fun observeWorkoutDetail(workoutId: Long): Flow<WorkoutDetail?>

    suspend fun startWorkout(note: String? = null): Long
    suspend fun finishWorkout(workoutId: Long)

    suspend fun addExerciseToWorkout(workoutId: Long, exerciseName: String): Long
    suspend fun addSet(input: AddSetInput): Long
    suspend fun duplicateSet(setId: Long)
    suspend fun deleteSet(setId: Long)
    suspend fun markSetCompleted(setId: Long, completed: Boolean)

    suspend fun getLastSetValuesForWorkoutExercise(workoutExerciseId: Long): LastSetValues?
    suspend fun getBestSetScoreForWorkoutExercise(workoutExerciseId: Long): Double?
}

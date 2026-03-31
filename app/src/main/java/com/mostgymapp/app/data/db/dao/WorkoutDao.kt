package com.mostgymapp.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.mostgymapp.app.data.db.entity.WorkoutEntity
import com.mostgymapp.app.data.db.entity.WorkoutExerciseEntity
import com.mostgymapp.app.data.db.relation.WorkoutHistoryRow
import com.mostgymapp.app.data.db.relation.WorkoutWithExercisesAndSets
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Insert
    suspend fun insertWorkout(entity: WorkoutEntity): Long

    @Insert
    suspend fun insertWorkoutExercise(entity: WorkoutExerciseEntity): Long

    @Query("SELECT * FROM workout WHERE id = :workoutId")
    suspend fun getWorkout(workoutId: Long): WorkoutEntity?

    @Query("SELECT * FROM workout WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveWorkout(): WorkoutEntity?

    @Query("SELECT * FROM workout WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    fun observeActiveWorkout(): Flow<WorkoutEntity?>

    @Query("UPDATE workout SET endTime = :endTs WHERE id = :workoutId")
    suspend fun finishWorkout(workoutId: Long, endTs: Long)

    @Query("SELECT COALESCE(MAX(orderIndex), -1) FROM workout_exercise WHERE workoutId = :workoutId")
    suspend fun getMaxWorkoutExerciseOrder(workoutId: Long): Int

    @Query("SELECT * FROM workout_exercise WHERE workoutId = :workoutId AND exerciseId = :exerciseId LIMIT 1")
    suspend fun getWorkoutExerciseByExercise(workoutId: Long, exerciseId: Long): WorkoutExerciseEntity?

    @Query("SELECT * FROM workout_exercise WHERE id = :id")
    suspend fun getWorkoutExerciseById(id: Long): WorkoutExerciseEntity?

    @Transaction
    @Query("SELECT * FROM workout WHERE id = :workoutId")
    fun observeWorkoutDetails(workoutId: Long): Flow<WorkoutWithExercisesAndSets?>

    @Transaction
    @Query("SELECT * FROM workout WHERE id = :workoutId")
    suspend fun getWorkoutDetailsOnce(workoutId: Long): WorkoutWithExercisesAndSets?

    @Query(
        """
        SELECT w.id, w.startTime, w.endTime,
               COUNT(DISTINCT we.id) AS exerciseCount,
               COALESCE(SUM(CASE WHEN s.isCompleted = 1 THEN s.weight * s.reps ELSE 0 END), 0) AS volume
        FROM workout w
        LEFT JOIN workout_exercise we ON we.workoutId = w.id
        LEFT JOIN set_entry s ON s.workoutExerciseId = we.id
        WHERE w.endTime IS NOT NULL
        GROUP BY w.id
        ORDER BY w.startTime DESC
        """
    )
    fun observeHistory(): Flow<List<WorkoutHistoryRow>>
}

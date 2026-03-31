package com.mostgymapp.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.mostgymapp.app.data.db.entity.SetEntity
import com.mostgymapp.app.data.db.relation.ExerciseStatsPointRow
import com.mostgymapp.app.data.db.relation.LastSetValuesRow
import com.mostgymapp.app.data.db.relation.SetWithWorkoutDateRow
import kotlinx.coroutines.flow.Flow

@Dao
interface SetDao {

    @Insert
    suspend fun insert(entity: SetEntity): Long

    @Delete
    suspend fun delete(entity: SetEntity)

    @Query("SELECT * FROM set_entry WHERE id = :id")
    suspend fun getById(id: Long): SetEntity?

    @Query("SELECT COALESCE(MAX(orderIndex), -1) FROM set_entry WHERE workoutExerciseId = :workoutExerciseId")
    suspend fun getMaxOrder(workoutExerciseId: Long): Int

    @Query("UPDATE set_entry SET isCompleted = :completed WHERE id = :setId")
    suspend fun updateCompleted(setId: Long, completed: Boolean)

    @Query(
        """
        SELECT s.id AS setId,
               s.workoutExerciseId,
               s.weight,
               s.reps,
               s.rpe,
               s.createdAt,
               w.startTime AS workoutStartTime
        FROM set_entry s
        JOIN workout_exercise we ON we.id = s.workoutExerciseId
        JOIN workout w ON w.id = we.workoutId
        WHERE we.exerciseId = :exerciseId
          AND w.endTime IS NOT NULL
          AND s.isCompleted = 1
        ORDER BY w.startTime DESC, s.orderIndex DESC
        """
    )
    fun observeSetsByExercise(exerciseId: Long): Flow<List<SetWithWorkoutDateRow>>

    @Query(
        """
        SELECT s.weight, s.reps, s.rpe
        FROM set_entry s
        JOIN workout_exercise we ON we.id = s.workoutExerciseId
        JOIN workout w ON w.id = we.workoutId
        WHERE we.exerciseId = :exerciseId
          AND w.id != :currentWorkoutId
          AND s.isCompleted = 1
        ORDER BY w.startTime DESC, s.orderIndex DESC
        LIMIT 1
        """
    )
    suspend fun getLastSetValuesForExercise(exerciseId: Long, currentWorkoutId: Long): LastSetValuesRow?

    @Query(
        """
        SELECT DATE(w.startTime / 1000, 'unixepoch') AS day,
               COALESCE(MAX(s.weight * (1 + s.reps / 30.0)), 0.0) AS bestE1rm,
               COALESCE(SUM(s.weight * s.reps), 0.0) AS tonnage
        FROM set_entry s
        JOIN workout_exercise we ON we.id = s.workoutExerciseId
        JOIN workout w ON w.id = we.workoutId
        WHERE we.exerciseId = :exerciseId
          AND w.endTime IS NOT NULL
          AND s.isCompleted = 1
          AND w.startTime >= :fromTs
        GROUP BY day
        ORDER BY day ASC
        """
    )
    fun observeStatsPoints(exerciseId: Long, fromTs: Long): Flow<List<ExerciseStatsPointRow>>

    @Query(
        """
        SELECT MAX(s.weight * s.reps)
        FROM set_entry s
        JOIN workout_exercise we ON we.id = s.workoutExerciseId
        JOIN workout w ON w.id = we.workoutId
        WHERE we.exerciseId = :exerciseId
          AND w.id != :currentWorkoutId
          AND s.isCompleted = 1
        """
    )
    suspend fun getBestSetScoreForExercise(exerciseId: Long, currentWorkoutId: Long): Double?
}

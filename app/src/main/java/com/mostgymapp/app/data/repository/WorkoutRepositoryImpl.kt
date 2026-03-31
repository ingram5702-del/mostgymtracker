package com.mostgymapp.app.data.repository

import androidx.room.withTransaction
import com.mostgymapp.app.data.db.WorkoutLogDatabase
import com.mostgymapp.app.data.db.dao.ExerciseDao
import com.mostgymapp.app.data.db.dao.SetDao
import com.mostgymapp.app.data.db.dao.WorkoutDao
import com.mostgymapp.app.data.db.entity.ExerciseEntity
import com.mostgymapp.app.data.db.entity.SetEntity
import com.mostgymapp.app.data.db.entity.WorkoutEntity
import com.mostgymapp.app.data.db.entity.WorkoutExerciseEntity
import com.mostgymapp.app.data.db.relation.WorkoutExerciseWithSets
import com.mostgymapp.app.data.db.relation.WorkoutWithExercisesAndSets
import com.mostgymapp.app.domain.model.ActiveWorkout
import com.mostgymapp.app.domain.model.AddSetInput
import com.mostgymapp.app.domain.model.LastSetValues
import com.mostgymapp.app.domain.model.SetEntry
import com.mostgymapp.app.domain.model.WorkoutDetail
import com.mostgymapp.app.domain.model.WorkoutExercise
import com.mostgymapp.app.domain.model.WorkoutHistoryItem
import com.mostgymapp.app.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val db: WorkoutLogDatabase,
    private val workoutDao: WorkoutDao,
    private val exerciseDao: ExerciseDao,
    private val setDao: SetDao
) : WorkoutRepository {

    override fun observeActiveWorkout(): Flow<ActiveWorkout?> =
        workoutDao.observeActiveWorkout().flatMapLatest { workout ->
            if (workout == null) {
                flowOf(null)
            } else {
                workoutDao.observeWorkoutDetails(workout.id).map { relation ->
                    relation?.toActiveWorkout()
                }
            }
        }

    override fun observeHistory(): Flow<List<WorkoutHistoryItem>> =
        workoutDao.observeHistory().map { rows ->
            rows.map {
                WorkoutHistoryItem(
                    id = it.id,
                    startTime = it.startTime,
                    endTime = it.endTime,
                    exerciseCount = it.exerciseCount,
                    volume = it.volume
                )
            }
        }

    override fun observeWorkoutDetail(workoutId: Long): Flow<WorkoutDetail?> =
        workoutDao.observeWorkoutDetails(workoutId).map { it?.toWorkoutDetail() }

    override suspend fun startWorkout(note: String?): Long = db.withTransaction {
        val active = workoutDao.getActiveWorkout()
        if (active != null) {
            active.id
        } else {
            workoutDao.insertWorkout(WorkoutEntity(startTime = System.currentTimeMillis(), note = note))
        }
    }

    override suspend fun finishWorkout(workoutId: Long) {
        workoutDao.finishWorkout(workoutId = workoutId, endTs = System.currentTimeMillis())
    }

    override suspend fun addExerciseToWorkout(workoutId: Long, exerciseName: String): Long = db.withTransaction {
        val normalized = exerciseName.trim()
        require(normalized.isNotBlank()) { "Exercise name cannot be blank" }

        val workout = workoutDao.getWorkout(workoutId) ?: error("Workout not found: $workoutId")
        require(workout.endTime == null) { "Cannot add exercise to finished workout" }

        val exercise = exerciseDao.getByName(normalized)
            ?: run {
                val id = exerciseDao.insert(ExerciseEntity(name = normalized))
                if (id > 0) {
                    exerciseDao.getById(id)!!
                } else {
                    exerciseDao.getByName(normalized) ?: error("Failed to create/find exercise")
                }
            }

        val existing = workoutDao.getWorkoutExerciseByExercise(workoutId, exercise.id)
        if (existing != null) {
            existing.id
        } else {
            val nextOrder = workoutDao.getMaxWorkoutExerciseOrder(workoutId) + 1
            workoutDao.insertWorkoutExercise(
                WorkoutExerciseEntity(
                    workoutId = workoutId,
                    exerciseId = exercise.id,
                    orderIndex = nextOrder
                )
            )
        }
    }

    override suspend fun addSet(input: AddSetInput): Long {
        require(input.weight >= 0.0) { "Weight must be >= 0" }
        require(input.reps >= 0) { "Reps must be >= 0" }

        return db.withTransaction {
            val workoutExercise = workoutDao.getWorkoutExerciseById(input.workoutExerciseId)
                ?: error("Workout exercise not found")
            val workout = workoutDao.getWorkout(workoutExercise.workoutId)
                ?: error("Workout not found")
            require(workout.endTime == null) { "Cannot add set to finished workout" }

            val nextOrder = setDao.getMaxOrder(input.workoutExerciseId) + 1
            setDao.insert(
                SetEntity(
                    workoutExerciseId = input.workoutExerciseId,
                    orderIndex = nextOrder,
                    weight = input.weight,
                    reps = input.reps,
                    rpe = input.rpe,
                    note = input.note,
                    isCompleted = input.isCompleted
                )
            )
        }
    }

    override suspend fun duplicateSet(setId: Long) {
        db.withTransaction {
            val source = setDao.getById(setId) ?: return@withTransaction
            val nextOrder = setDao.getMaxOrder(source.workoutExerciseId) + 1
            setDao.insert(
                source.copy(
                    id = 0,
                    orderIndex = nextOrder,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun deleteSet(setId: Long) {
        val set = setDao.getById(setId) ?: return
        setDao.delete(set)
    }

    override suspend fun markSetCompleted(setId: Long, completed: Boolean) {
        setDao.updateCompleted(setId, completed)
    }

    override suspend fun getLastSetValuesForWorkoutExercise(workoutExerciseId: Long): LastSetValues? {
        val workoutExercise = workoutDao.getWorkoutExerciseById(workoutExerciseId) ?: return null
        val row = setDao.getLastSetValuesForExercise(
            exerciseId = workoutExercise.exerciseId,
            currentWorkoutId = workoutExercise.workoutId
        ) ?: return null

        return LastSetValues(weight = row.weight, reps = row.reps, rpe = row.rpe)
    }

    override suspend fun getBestSetScoreForWorkoutExercise(workoutExerciseId: Long): Double? {
        val workoutExercise = workoutDao.getWorkoutExerciseById(workoutExerciseId) ?: return null
        return setDao.getBestSetScoreForExercise(
            exerciseId = workoutExercise.exerciseId,
            currentWorkoutId = workoutExercise.workoutId
        )
    }

    private fun WorkoutWithExercisesAndSets.toActiveWorkout(): ActiveWorkout = ActiveWorkout(
        workoutId = workout.id,
        startTime = workout.startTime,
        note = workout.note,
        exercises = exercises
            .sortedBy { it.workoutExercise.orderIndex }
            .map { it.toDomainWorkoutExercise() }
    )

    private fun WorkoutWithExercisesAndSets.toWorkoutDetail(): WorkoutDetail = WorkoutDetail(
        id = workout.id,
        startTime = workout.startTime,
        endTime = workout.endTime,
        note = workout.note,
        exercises = exercises
            .sortedBy { it.workoutExercise.orderIndex }
            .map { it.toDomainWorkoutExercise() }
    )

    private fun WorkoutExerciseWithSets.toDomainWorkoutExercise(): WorkoutExercise = WorkoutExercise(
        id = workoutExercise.id,
        exerciseId = exercise.id,
        exerciseName = exercise.name,
        orderIndex = workoutExercise.orderIndex,
        sets = sets
            .sortedBy { it.orderIndex }
            .map { set ->
                SetEntry(
                    id = set.id,
                    workoutExerciseId = set.workoutExerciseId,
                    orderIndex = set.orderIndex,
                    weight = set.weight,
                    reps = set.reps,
                    rpe = set.rpe,
                    note = set.note,
                    isCompleted = set.isCompleted
                )
            }
    )
}

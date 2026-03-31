package com.mostgymapp.app.data.repository

import androidx.room.withTransaction
import com.mostgymapp.app.data.db.WorkoutLogDatabase
import com.mostgymapp.app.data.db.dao.TemplateDao
import com.mostgymapp.app.data.db.dao.WorkoutDao
import com.mostgymapp.app.data.db.entity.TemplateEntity
import com.mostgymapp.app.data.db.entity.TemplateExerciseEntity
import com.mostgymapp.app.data.db.entity.WorkoutEntity
import com.mostgymapp.app.data.db.entity.WorkoutExerciseEntity
import com.mostgymapp.app.domain.model.TemplateDetail
import com.mostgymapp.app.domain.model.TemplateExercise
import com.mostgymapp.app.domain.model.TemplateSummary
import com.mostgymapp.app.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemplateRepositoryImpl @Inject constructor(
    private val db: WorkoutLogDatabase,
    private val templateDao: TemplateDao,
    private val workoutDao: WorkoutDao
) : TemplateRepository {

    override fun observeTemplates(): Flow<List<TemplateSummary>> =
        templateDao.observeTemplateSummaries().map { rows ->
            rows.map {
                TemplateSummary(
                    id = it.id,
                    name = it.name,
                    note = it.note,
                    exerciseCount = it.exerciseCount,
                    createdAt = it.createdAt
                )
            }
        }

    override fun observeTemplateDetails(templateId: Long): Flow<TemplateDetail?> =
        templateDao.observeTemplateDetails(templateId).map { relation ->
            relation?.let {
                TemplateDetail(
                    id = it.template.id,
                    name = it.template.name,
                    note = it.template.note,
                    exercises = it.exercises.sortedBy { row -> row.templateExercise.orderIndex }.map { row ->
                        TemplateExercise(
                            id = row.templateExercise.id,
                            exerciseId = row.exercise.id,
                            exerciseName = row.exercise.name,
                            orderIndex = row.templateExercise.orderIndex,
                            defaultRestSec = row.templateExercise.defaultRestSec
                        )
                    }
                )
            }
        }

    override suspend fun createTemplateFromWorkout(workoutId: Long, name: String, note: String?) {
        require(name.isNotBlank()) { "Template name cannot be empty" }

        db.withTransaction {
            val workout = workoutDao.getWorkoutDetailsOnce(workoutId)
                ?: error("Workout not found")
            val templateId = templateDao.insertTemplate(
                TemplateEntity(
                    name = name.trim(),
                    note = note
                )
            )
            workout.exercises.sortedBy { it.workoutExercise.orderIndex }.forEach { relation ->
                templateDao.insertTemplateExercise(
                    TemplateExerciseEntity(
                        templateId = templateId,
                        exerciseId = relation.exercise.id,
                        orderIndex = relation.workoutExercise.orderIndex
                    )
                )
            }
        }
    }

    override suspend fun deleteTemplate(templateId: Long) {
        templateDao.deleteTemplate(templateId)
    }

    override suspend fun startWorkoutFromTemplate(templateId: Long): Long = db.withTransaction {
        val template = templateDao.getTemplateDetails(templateId) ?: error("Template not found")

        workoutDao.getActiveWorkout()?.id?.let { activeId ->
            return@withTransaction activeId
        }

        val workoutId = workoutDao.insertWorkout(
            WorkoutEntity(startTime = System.currentTimeMillis(), note = template.template.note)
        )
        template.exercises.sortedBy { it.templateExercise.orderIndex }.forEach { relation ->
            workoutDao.insertWorkoutExercise(
                WorkoutExerciseEntity(
                    workoutId = workoutId,
                    exerciseId = relation.exercise.id,
                    orderIndex = relation.templateExercise.orderIndex
                )
            )
        }
        workoutId
    }
}

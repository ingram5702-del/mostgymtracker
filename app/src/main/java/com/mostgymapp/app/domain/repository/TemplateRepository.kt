package com.mostgymapp.app.domain.repository

import com.mostgymapp.app.domain.model.TemplateDetail
import com.mostgymapp.app.domain.model.TemplateSummary
import kotlinx.coroutines.flow.Flow

interface TemplateRepository {
    fun observeTemplates(): Flow<List<TemplateSummary>>
    fun observeTemplateDetails(templateId: Long): Flow<TemplateDetail?>

    suspend fun createTemplateFromWorkout(workoutId: Long, name: String, note: String?)
    suspend fun deleteTemplate(templateId: Long)
    suspend fun startWorkoutFromTemplate(templateId: Long): Long
}

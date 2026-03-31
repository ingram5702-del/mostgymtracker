package com.mostgymapp.app.domain.usecase

import com.mostgymapp.app.domain.repository.TemplateRepository
import javax.inject.Inject

class CreateTemplateFromWorkoutUseCase @Inject constructor(
    private val repository: TemplateRepository
) {
    suspend operator fun invoke(workoutId: Long, name: String, note: String?) {
        repository.createTemplateFromWorkout(workoutId, name, note)
    }
}

class StartWorkoutFromTemplateUseCase @Inject constructor(
    private val repository: TemplateRepository
) {
    suspend operator fun invoke(templateId: Long): Long = repository.startWorkoutFromTemplate(templateId)
}

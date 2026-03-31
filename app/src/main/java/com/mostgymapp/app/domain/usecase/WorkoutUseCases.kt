package com.mostgymapp.app.domain.usecase

import com.mostgymapp.app.domain.model.AddSetInput
import com.mostgymapp.app.domain.repository.WorkoutRepository
import javax.inject.Inject

class StartWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(note: String? = null): Long = repository.startWorkout(note)
}

class AddSetUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(input: AddSetInput): Long = repository.addSet(input)
}

class FinishWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(workoutId: Long) = repository.finishWorkout(workoutId)
}

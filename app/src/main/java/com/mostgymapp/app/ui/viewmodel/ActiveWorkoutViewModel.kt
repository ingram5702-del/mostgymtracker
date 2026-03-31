package com.mostgymapp.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mostgymapp.app.domain.model.AddSetInput
import com.mostgymapp.app.domain.model.LastSetValues
import com.mostgymapp.app.domain.repository.ExerciseRepository
import com.mostgymapp.app.domain.repository.WorkoutRepository
import com.mostgymapp.app.domain.usecase.AddSetUseCase
import com.mostgymapp.app.domain.usecase.FinishWorkoutUseCase
import com.mostgymapp.app.domain.usecase.StartWorkoutUseCase
import com.mostgymapp.app.timer.RestTimerManager
import com.mostgymapp.app.ui.state.ActiveWorkoutUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ActiveWorkoutViewModel @Inject constructor(
    private val startWorkoutUseCase: StartWorkoutUseCase,
    private val addSetUseCase: AddSetUseCase,
    private val finishWorkoutUseCase: FinishWorkoutUseCase,
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val restTimerManager: RestTimerManager
) : ViewModel() {

    private val errors = MutableStateFlow<String?>(null)

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private val _inputSuggestions = MutableStateFlow<Map<Long, LastSetValues>>(emptyMap())
    val inputSuggestions: StateFlow<Map<Long, LastSetValues>> = _inputSuggestions

    private val _insights = MutableStateFlow<Map<Long, ExerciseInsight>>(emptyMap())
    val insights: StateFlow<Map<Long, ExerciseInsight>> = _insights

    val uiState: StateFlow<ActiveWorkoutUiState> = combine(
        workoutRepository.observeActiveWorkout(),
        restTimerManager.state,
        exerciseRepository.observeAll(),
        errors
    ) { activeWorkout, timerState, exercises, error ->
        ActiveWorkoutUiState(
            isLoading = false,
            activeWorkout = activeWorkout,
            timerState = timerState,
            availableExerciseNames = exercises.map { it.name },
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ActiveWorkoutUiState(isLoading = true)
    )

    fun startWorkout(note: String? = null) {
        viewModelScope.launch {
            runCatching { startWorkoutUseCase(note) }
                .onFailure { emitError(it.message ?: "Failed to start workout") }
        }
    }

    fun finishWorkout() {
        val workoutId = uiState.value.activeWorkout?.workoutId ?: return
        viewModelScope.launch {
            runCatching {
                finishWorkoutUseCase(workoutId)
                restTimerManager.cancel()
            }.onFailure { emitError(it.message ?: "Failed to finish workout") }
        }
    }

    fun addExercise(name: String) {
        val workoutId = uiState.value.activeWorkout?.workoutId ?: return
        viewModelScope.launch {
            runCatching { workoutRepository.addExerciseToWorkout(workoutId = workoutId, exerciseName = name) }
                .onFailure { emitError(it.message ?: "Failed to add exercise") }
        }
    }

    fun addSet(
        workoutExerciseId: Long,
        weightText: String,
        repsText: String,
        rpeText: String,
        note: String?,
        restSeconds: Int
    ) {
        viewModelScope.launch {
            val weight = weightText.toDoubleOrNull()
            val reps = repsText.toIntOrNull()
            val rpe = rpeText.toDoubleOrNull()

            if (weight == null || reps == null) {
                emitError("Weight and reps are required")
                return@launch
            }
            if (weight < 0 || reps < 0) {
                emitError("Weight and reps must be >= 0")
                return@launch
            }
            if (rpe != null && (rpe < 1.0 || rpe > 10.0)) {
                emitError("RPE must be between 1 and 10")
                return@launch
            }

            runCatching {
                addSetUseCase(
                    AddSetInput(
                        workoutExerciseId = workoutExerciseId,
                        weight = weight,
                        reps = reps,
                        rpe = rpe,
                        note = note?.takeIf { it.isNotBlank() }
                    )
                )
                restTimerManager.start(restSeconds)
                refreshSuggestion(workoutExerciseId)
                refreshInsights(workoutExerciseId)
            }.onFailure { emitError(it.message ?: "Failed to save set") }
        }
    }

    fun duplicateSet(setId: Long, workoutExerciseId: Long) {
        viewModelScope.launch {
            runCatching {
                workoutRepository.duplicateSet(setId)
                refreshSuggestion(workoutExerciseId)
                refreshInsights(workoutExerciseId)
            }.onFailure { emitError(it.message ?: "Failed to duplicate set") }
        }
    }

    fun deleteSet(setId: Long, workoutExerciseId: Long) {
        viewModelScope.launch {
            runCatching {
                workoutRepository.deleteSet(setId)
                refreshSuggestion(workoutExerciseId)
                refreshInsights(workoutExerciseId)
            }.onFailure { emitError(it.message ?: "Failed to delete set") }
        }
    }

    fun toggleSetCompleted(setId: Long, completed: Boolean) {
        viewModelScope.launch {
            runCatching { workoutRepository.markSetCompleted(setId, completed) }
                .onFailure { emitError(it.message ?: "Failed to update set") }
        }
    }

    fun refreshSuggestion(workoutExerciseId: Long) {
        viewModelScope.launch {
            val inWorkoutSuggestion = uiState.value.activeWorkout
                ?.exercises
                ?.firstOrNull { it.id == workoutExerciseId }
                ?.sets
                ?.lastOrNull()
                ?.let { LastSetValues(weight = it.weight, reps = it.reps, rpe = it.rpe) }

            val suggestion = inWorkoutSuggestion
                ?: workoutRepository.getLastSetValuesForWorkoutExercise(workoutExerciseId)

            if (suggestion != null) {
                _inputSuggestions.update { map -> map + (workoutExerciseId to suggestion) }
            }
        }
    }

    fun refreshInsights(workoutExerciseId: Long) {
        viewModelScope.launch {
            val last = workoutRepository.getLastSetValuesForWorkoutExercise(workoutExerciseId)
            val best = workoutRepository.getBestSetScoreForWorkoutExercise(workoutExerciseId)
            _insights.update { it + (workoutExerciseId to ExerciseInsight(lastSet = last, bestScore = best)) }
        }
    }

    fun cancelTimer() {
        viewModelScope.launch {
            restTimerManager.cancel()
        }
    }

    fun clearError() {
        errors.value = null
    }

    private fun emitError(message: String) {
        errors.value = message
        _events.tryEmit(message)
    }
}

data class ExerciseInsight(
    val lastSet: LastSetValues? = null,
    val bestScore: Double? = null
)

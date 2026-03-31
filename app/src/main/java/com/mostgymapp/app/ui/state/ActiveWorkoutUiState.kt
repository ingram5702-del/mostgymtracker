package com.mostgymapp.app.ui.state

import com.mostgymapp.app.domain.model.ActiveWorkout
import com.mostgymapp.app.timer.RestTimerState

data class ActiveWorkoutUiState(
    val isLoading: Boolean = true,
    val activeWorkout: ActiveWorkout? = null,
    val timerState: RestTimerState = RestTimerState(),
    val availableExerciseNames: List<String> = emptyList(),
    val errorMessage: String? = null
)

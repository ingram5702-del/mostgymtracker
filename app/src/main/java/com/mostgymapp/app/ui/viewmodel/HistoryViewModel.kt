package com.mostgymapp.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mostgymapp.app.domain.model.WorkoutDetail
import com.mostgymapp.app.domain.repository.WorkoutRepository
import com.mostgymapp.app.ui.state.HistoryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = workoutRepository.observeHistory()
        .map { items ->
            HistoryUiState(
                isLoading = false,
                items = items
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState(isLoading = true)
        )

    fun observeWorkoutDetail(workoutId: Long): Flow<WorkoutDetail?> =
        workoutRepository.observeWorkoutDetail(workoutId)
}

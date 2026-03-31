package com.mostgymapp.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mostgymapp.app.domain.model.Exercise
import com.mostgymapp.app.domain.model.StatsMetric
import com.mostgymapp.app.domain.model.StatsRange
import com.mostgymapp.app.domain.repository.ExerciseRepository
import com.mostgymapp.app.domain.repository.StatsRepository
import com.mostgymapp.app.domain.usecase.CalculateStatsSummaryUseCase
import com.mostgymapp.app.ui.state.StatsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val statsRepository: StatsRepository,
    private val calculateStatsSummaryUseCase: CalculateStatsSummaryUseCase
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selectedExerciseId = MutableStateFlow<Long?>(null)
    private val range = MutableStateFlow(StatsRange.FOUR_WEEKS)
    private val metric = MutableStateFlow(StatsMetric.E1RM)

    private val exercisesFlow = query.flatMapLatest { exerciseRepository.observeSearch(it) }

    private val pointsFlow = combine(selectedExerciseId, range) { exerciseId, selectedRange ->
        exerciseId to selectedRange
    }.flatMapLatest { (exerciseId, selectedRange) ->
        if (exerciseId == null) {
            flowOf(emptyList())
        } else {
            statsRepository.observeStatsPoints(exerciseId, selectedRange)
        }
    }

    private data class StatsInputs(
        val exercises: List<Exercise>,
        val selectedId: Long?,
        val searchQuery: String,
        val selectedRange: StatsRange,
        val selectedMetric: StatsMetric
    )

    private val baseInputsFlow = combine(
        exercisesFlow,
        selectedExerciseId,
        query,
        range,
        metric
    ) { exercises, selectedId, searchQuery, selectedRange, selectedMetric ->
        StatsInputs(
            exercises = exercises,
            selectedId = selectedId,
            searchQuery = searchQuery,
            selectedRange = selectedRange,
            selectedMetric = selectedMetric
        )
    }

    val uiState: StateFlow<StatsUiState> = combine(baseInputsFlow, pointsFlow) { inputs, points ->
        val resolvedSelected = inputs.selectedId ?: inputs.exercises.firstOrNull()?.id
        val summary = calculateStatsSummaryUseCase(points, inputs.selectedMetric)
        StatsUiState(
            isLoading = false,
            exercises = inputs.exercises,
            selectedExerciseId = resolvedSelected,
            query = inputs.searchQuery,
            range = inputs.selectedRange,
            metric = inputs.selectedMetric,
            points = points,
            summary = summary
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState(isLoading = true)
    )

    fun setQuery(value: String) {
        query.value = value
    }

    fun setRange(value: StatsRange) {
        range.value = value
    }

    fun setMetric(value: StatsMetric) {
        metric.value = value
    }

    fun selectExercise(exerciseId: Long) {
        selectedExerciseId.value = exerciseId
    }
}

package com.mostgymapp.app.ui.state

import com.mostgymapp.app.domain.model.Exercise
import com.mostgymapp.app.domain.model.StatsMetric
import com.mostgymapp.app.domain.model.StatsPoint
import com.mostgymapp.app.domain.model.StatsRange
import com.mostgymapp.app.domain.model.StatsSummary

data class StatsUiState(
    val isLoading: Boolean = true,
    val exercises: List<Exercise> = emptyList(),
    val selectedExerciseId: Long? = null,
    val query: String = "",
    val range: StatsRange = StatsRange.FOUR_WEEKS,
    val metric: StatsMetric = StatsMetric.E1RM,
    val points: List<StatsPoint> = emptyList(),
    val summary: StatsSummary = StatsSummary(pr = 0.0, last = 0.0, trend = 0.0)
)

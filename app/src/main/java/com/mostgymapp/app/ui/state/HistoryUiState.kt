package com.mostgymapp.app.ui.state

import com.mostgymapp.app.domain.model.WorkoutHistoryItem

data class HistoryUiState(
    val isLoading: Boolean = true,
    val items: List<WorkoutHistoryItem> = emptyList(),
    val activity: ActivityHeatmap = ActivityHeatmap.Empty
)

data class ActivityHeatmap(
    val weeks: List<List<Int?>>,
    val maxCount: Int,
    val currentStreak: Int,
    val activeDays: Int
) {
    companion object {
        val Empty = ActivityHeatmap(
            weeks = emptyList(),
            maxCount = 0,
            currentStreak = 0,
            activeDays = 0
        )
    }
}

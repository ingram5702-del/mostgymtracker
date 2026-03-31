package com.mostgymapp.app.ui.state

import com.mostgymapp.app.domain.model.WorkoutHistoryItem

data class HistoryUiState(
    val isLoading: Boolean = true,
    val items: List<WorkoutHistoryItem> = emptyList()
)

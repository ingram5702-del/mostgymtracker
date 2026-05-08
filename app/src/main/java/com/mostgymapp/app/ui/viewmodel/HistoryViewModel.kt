package com.mostgymapp.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mostgymapp.app.domain.model.WorkoutDetail
import com.mostgymapp.app.domain.model.WorkoutHistoryItem
import com.mostgymapp.app.domain.repository.WorkoutRepository
import com.mostgymapp.app.ui.state.ActivityHeatmap
import com.mostgymapp.app.ui.state.HistoryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private const val HEATMAP_WEEKS = 12

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = workoutRepository.observeHistory()
        .map { items ->
            HistoryUiState(
                isLoading = false,
                items = items,
                activity = buildHeatmap(items)
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState(isLoading = true)
        )

    fun observeWorkoutDetail(workoutId: Long): Flow<WorkoutDetail?> =
        workoutRepository.observeWorkoutDetail(workoutId)

    private fun buildHeatmap(items: List<WorkoutHistoryItem>): ActivityHeatmap {
        if (items.isEmpty()) return ActivityHeatmap.Empty

        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)

        val countsByDate: Map<LocalDate, Int> = items
            .groupingBy { Instant.ofEpochMilli(it.startTime).atZone(zone).toLocalDate() }
            .eachCount()

        val currentWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val gridStart = currentWeekStart.minusWeeks((HEATMAP_WEEKS - 1).toLong())

        val weeks = (0 until HEATMAP_WEEKS).map { w ->
            val weekStart = gridStart.plusWeeks(w.toLong())
            (0 until 7).map { d ->
                val date = weekStart.plusDays(d.toLong())
                if (date.isAfter(today)) null else countsByDate[date] ?: 0
            }
        }

        val maxCount = countsByDate.values.maxOrNull() ?: 0

        var streak = 0
        var cursor = today
        if ((countsByDate[cursor] ?: 0) == 0 && (countsByDate[cursor.minusDays(1)] ?: 0) > 0) {
            cursor = cursor.minusDays(1)
        }
        while ((countsByDate[cursor] ?: 0) > 0) {
            streak++
            cursor = cursor.minusDays(1)
        }

        val activeDaysInWindow = weeks.flatten().count { it != null && it > 0 }

        return ActivityHeatmap(
            weeks = weeks,
            maxCount = maxCount,
            currentStreak = streak,
            activeDays = activeDaysInWindow
        )
    }
}

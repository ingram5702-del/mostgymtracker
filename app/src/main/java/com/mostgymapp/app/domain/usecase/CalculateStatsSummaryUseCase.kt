package com.mostgymapp.app.domain.usecase

import com.mostgymapp.app.domain.model.StatsMetric
import com.mostgymapp.app.domain.model.StatsPoint
import com.mostgymapp.app.domain.model.StatsSummary
import javax.inject.Inject

class CalculateStatsSummaryUseCase @Inject constructor() {

    operator fun invoke(points: List<StatsPoint>, metric: StatsMetric): StatsSummary {
        if (points.isEmpty()) {
            return StatsSummary(pr = 0.0, last = 0.0, trend = 0.0)
        }

        val values = points.map { point ->
            when (metric) {
                StatsMetric.E1RM -> point.bestE1rm
                StatsMetric.TONNAGE -> point.tonnage
            }
        }

        val pr = values.maxOrNull() ?: 0.0
        val last = values.lastOrNull() ?: 0.0

        val currentChunk = values.takeLast(3)
        val previousChunk = values.dropLast(3).takeLast(3)
        val currentAvg = if (currentChunk.isNotEmpty()) currentChunk.average() else 0.0
        val previousAvg = if (previousChunk.isNotEmpty()) previousChunk.average() else currentAvg

        return StatsSummary(
            pr = pr,
            last = last,
            trend = currentAvg - previousAvg
        )
    }
}

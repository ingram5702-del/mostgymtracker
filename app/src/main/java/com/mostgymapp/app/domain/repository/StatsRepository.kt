package com.mostgymapp.app.domain.repository

import com.mostgymapp.app.domain.model.StatsPoint
import com.mostgymapp.app.domain.model.StatsRange
import kotlinx.coroutines.flow.Flow

interface StatsRepository {
    fun observeStatsPoints(exerciseId: Long, range: StatsRange): Flow<List<StatsPoint>>
}

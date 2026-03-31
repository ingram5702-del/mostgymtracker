package com.mostgymapp.app.data.repository

import com.mostgymapp.app.data.db.dao.SetDao
import com.mostgymapp.app.domain.model.StatsPoint
import com.mostgymapp.app.domain.model.StatsRange
import com.mostgymapp.app.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsRepositoryImpl @Inject constructor(
    private val setDao: SetDao
) : StatsRepository {

    override fun observeStatsPoints(exerciseId: Long, range: StatsRange): Flow<List<StatsPoint>> {
        val fromTs = rangeToFromTs(range)
        return setDao.observeStatsPoints(exerciseId = exerciseId, fromTs = fromTs)
            .map { rows ->
                rows.map { row ->
                    val dayEpoch = LocalDate.parse(row.day)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                    StatsPoint(
                        dayEpochMillis = dayEpoch,
                        bestE1rm = row.bestE1rm,
                        tonnage = row.tonnage
                    )
                }
            }
    }

    private fun rangeToFromTs(range: StatsRange): Long {
        val now = Instant.now()
        return when (range) {
            StatsRange.FOUR_WEEKS -> now.minusSeconds(28L * 24L * 3600L).toEpochMilli()
            StatsRange.THREE_MONTHS -> now.minusSeconds(90L * 24L * 3600L).toEpochMilli()
            StatsRange.ALL -> 0L
        }
    }
}

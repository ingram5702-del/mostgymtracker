package com.mostgymapp.app.ui.screen.stats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.mostgymapp.app.domain.model.Exercise
import com.mostgymapp.app.domain.model.StatsMetric
import com.mostgymapp.app.domain.model.StatsPoint
import com.mostgymapp.app.domain.model.StatsRange
import com.mostgymapp.app.ui.theme.ChartBlue
import com.mostgymapp.app.ui.theme.SuccessGreen
import com.mostgymapp.app.ui.viewmodel.StatsViewModel
import com.mostgymapp.app.utils.formatTrend
import com.mostgymapp.app.utils.formatWeight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    paddingValues: PaddingValues,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.selectedExerciseId, uiState.exercises) {
        if (uiState.selectedExerciseId == null && uiState.exercises.isNotEmpty()) {
            viewModel.selectExercise(uiState.exercises.first().id)
        }
    }

    Scaffold(
        modifier = Modifier.padding(paddingValues),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Statistics",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                value = uiState.query,
                onValueChange = viewModel::setQuery,
                label = { Text("Search exercise") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(top = 8.dp)
            ) {
                items(uiState.exercises, key = { it.id }) { exercise ->
                    ExerciseRow(
                        exercise = exercise,
                        selected = uiState.selectedExerciseId == exercise.id,
                        onSelect = { viewModel.selectExercise(exercise.id) }
                    )
                }
            }

            val chipColors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.range == StatsRange.FOUR_WEEKS,
                    onClick = { viewModel.setRange(StatsRange.FOUR_WEEKS) },
                    label = { Text("4 wk") },
                    colors = chipColors
                )
                FilterChip(
                    selected = uiState.range == StatsRange.THREE_MONTHS,
                    onClick = { viewModel.setRange(StatsRange.THREE_MONTHS) },
                    label = { Text("3 mo") },
                    colors = chipColors
                )
                FilterChip(
                    selected = uiState.range == StatsRange.ALL,
                    onClick = { viewModel.setRange(StatsRange.ALL) },
                    label = { Text("All") },
                    colors = chipColors
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.metric == StatsMetric.E1RM,
                    onClick = { viewModel.setMetric(StatsMetric.E1RM) },
                    label = { Text("Max (e1RM)") },
                    colors = chipColors
                )
                FilterChip(
                    selected = uiState.metric == StatsMetric.TONNAGE,
                    onClick = { viewModel.setMetric(StatsMetric.TONNAGE) },
                    label = { Text("Tonnage") },
                    colors = chipColors
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "PR",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            formatWeight(uiState.summary.pr),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column {
                        Text(
                            "Last",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            formatWeight(uiState.summary.last),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column {
                        Text(
                            "Trend",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatTrend(uiState.summary.trend),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (uiState.summary.trend >= 0) SuccessGreen else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            ProgressChart(
                points = uiState.points,
                metric = uiState.metric,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(top = 12.dp)
            )
        }
    }
}

@Composable
private fun ExerciseRow(
    exercise: Exercise,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val bgColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
    val textColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 1.dp else 0.dp)
    ) {
        Text(
            text = exercise.name,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp)
        )
    }
}

@Composable
private fun ProgressChart(
    points: List<StatsPoint>,
    metric: StatsMetric,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) {
        Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
            Text(
                "Not enough data for chart.\nComplete at least 2 workouts with this exercise.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val labels = points.map { toShortDate(it.dayEpochMillis) }
    val values = points.map {
        when (metric) {
            StatsMetric.E1RM -> it.bestE1rm
            StatsMetric.TONNAGE -> it.tonnage
        }
    }

    val chartColorInt = android.graphics.Color.parseColor("#1565C0")
    val chartCircleColorInt = android.graphics.Color.parseColor("#1976D2")
    val chartFillColorInt = android.graphics.Color.parseColor("#E3F2FD")

    AndroidView(
        modifier = modifier,
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                axisRight.isEnabled = false
                legend.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                xAxis.labelRotationAngle = -20f
                xAxis.textColor = android.graphics.Color.parseColor("#73777F")
                axisLeft.textColor = android.graphics.Color.parseColor("#73777F")
                setExtraOffsets(8f, 8f, 8f, 8f)
            }
        },
        update = { chart ->
            val entries = values.mapIndexed { index, value -> Entry(index.toFloat(), value.toFloat()) }
            val dataSet = LineDataSet(entries, metric.name).apply {
                color = chartColorInt
                lineWidth = 2.5f
                setCircleColor(chartCircleColorInt)
                circleRadius = 4f
                setCircleHoleColor(android.graphics.Color.WHITE)
                circleHoleRadius = 2f
                valueTextSize = 0f
                setDrawValues(false)
                setDrawFilled(true)
                fillColor = chartFillColorInt
                fillAlpha = 80
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }

            chart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                    return labels.getOrNull(value.toInt()) ?: ""
                }
            }
            chart.data = LineData(dataSet)
            chart.invalidate()
        }
    )
}

private fun toShortDate(epochMs: Long): String {
    val formatter = SimpleDateFormat("dd.MM", Locale.getDefault())
    return formatter.format(Date(epochMs))
}

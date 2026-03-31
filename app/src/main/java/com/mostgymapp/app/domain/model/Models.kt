package com.mostgymapp.app.domain.model

data class Exercise(
    val id: Long,
    val name: String
)

data class SetEntry(
    val id: Long,
    val workoutExerciseId: Long,
    val orderIndex: Int,
    val weight: Double,
    val reps: Int,
    val rpe: Double?,
    val note: String?,
    val isCompleted: Boolean
)

data class WorkoutExercise(
    val id: Long,
    val exerciseId: Long,
    val exerciseName: String,
    val orderIndex: Int,
    val sets: List<SetEntry>
)

data class WorkoutDetail(
    val id: Long,
    val startTime: Long,
    val endTime: Long?,
    val note: String?,
    val exercises: List<WorkoutExercise>
)

data class ActiveWorkout(
    val workoutId: Long,
    val startTime: Long,
    val note: String?,
    val exercises: List<WorkoutExercise>
)

data class WorkoutHistoryItem(
    val id: Long,
    val startTime: Long,
    val endTime: Long,
    val exerciseCount: Int,
    val volume: Double
)

data class LastSetValues(
    val weight: Double,
    val reps: Int,
    val rpe: Double?
)

data class TemplateSummary(
    val id: Long,
    val name: String,
    val note: String?,
    val exerciseCount: Int,
    val createdAt: Long
)

data class TemplateDetail(
    val id: Long,
    val name: String,
    val note: String?,
    val exercises: List<TemplateExercise>
)

data class TemplateExercise(
    val id: Long,
    val exerciseId: Long,
    val exerciseName: String,
    val orderIndex: Int,
    val defaultRestSec: Int?
)

enum class StatsMetric {
    E1RM,
    TONNAGE
}

enum class StatsRange {
    FOUR_WEEKS,
    THREE_MONTHS,
    ALL
}

data class StatsPoint(
    val dayEpochMillis: Long,
    val bestE1rm: Double,
    val tonnage: Double
)

data class StatsSummary(
    val pr: Double,
    val last: Double,
    val trend: Double
)

package com.mostgymapp.app.domain.model

data class AddSetInput(
    val workoutExerciseId: Long,
    val weight: Double,
    val reps: Int,
    val rpe: Double? = null,
    val note: String? = null,
    val isCompleted: Boolean = true
)

package com.mostgymapp.app.navigation

sealed class Route(val route: String) {
    data object Workout : Route("workout")
    data object History : Route("history")
    data object Templates : Route("templates")
    data object Stats : Route("stats")
    data object Scanner : Route("scanner")
    data object Settings : Route("settings")

    data object WorkoutExercise : Route("workout/exercise/{workoutExerciseId}") {
        fun create(workoutExerciseId: Long): String = "workout/exercise/$workoutExerciseId"
    }

    data object HistoryDetail : Route("history/detail/{workoutId}") {
        fun create(workoutId: Long): String = "history/detail/$workoutId"
    }

    data object TemplateDetail : Route("templates/detail/{templateId}") {
        fun create(templateId: Long): String = "templates/detail/$templateId"
    }
}

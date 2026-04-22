package com.mostgymapp.app.analytics

import com.posthog.PostHog

object Analytics {

    fun screen(name: String, properties: Map<String, Any?> = emptyMap()) {
        PostHog.screen(screenTitle = name, properties = properties.filterValuesNotNull())
    }

    fun capture(event: String, properties: Map<String, Any?> = emptyMap()) {
        PostHog.capture(event = event, properties = properties.filterValuesNotNull())
    }

    object Event {
        const val WORKOUT_STARTED = "workout_started"
        const val WORKOUT_FINISHED = "workout_finished"
        const val WORKOUT_EXERCISE_ADDED = "workout_exercise_added"
        const val SET_LOGGED = "set_logged"
        const val SET_DELETED = "set_deleted"
        const val SET_DUPLICATED = "set_duplicated"
        const val SET_COMPLETED_TOGGLED = "set_completed_toggled"
        const val REST_TIMER_CANCELLED = "rest_timer_cancelled"
        const val TEMPLATE_CREATED = "template_created"
        const val TEMPLATE_DELETED = "template_deleted"
        const val TEMPLATE_STARTED = "template_started"
        const val QR_CODE_SCANNED = "qr_code_scanned"
        const val QR_CODE_OPENED = "qr_code_opened"
        const val FEEDBACK_SENT = "feedback_sent"
        const val PRIVACY_POLICY_OPENED = "privacy_policy_opened"
    }

    object Screen {
        const val WORKOUT = "Workout"
        const val WORKOUT_EXERCISE = "WorkoutExercise"
        const val HISTORY = "History"
        const val HISTORY_DETAIL = "HistoryDetail"
        const val TEMPLATES = "Templates"
        const val TEMPLATE_DETAIL = "TemplateDetail"
        const val SCANNER = "Scanner"
        const val STATS = "Stats"
        const val SETTINGS = "Settings"
    }
}

private fun Map<String, Any?>.filterValuesNotNull(): Map<String, Any> =
    mapNotNull { (k, v) -> v?.let { k to it } }.toMap()

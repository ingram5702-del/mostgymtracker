package com.mostgymapp.app.timer

data class RestTimerState(
    val isRunning: Boolean = false,
    val remainingMs: Long = 0L,
    val endAtMs: Long? = null
)

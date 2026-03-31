package com.mostgymapp.app.timer

import android.os.CountDownTimer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestTimerManager @Inject constructor(
    private val scheduler: RestTimerAlarmScheduler,
    private val store: RestTimerStore
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(RestTimerState())
    val state: StateFlow<RestTimerState> = _state.asStateFlow()

    private var countDownTimer: CountDownTimer? = null

    init {
        scope.launch { restore() }
    }

    suspend fun start(durationSeconds: Int) {
        val endAt = System.currentTimeMillis() + durationSeconds * 1000L
        scheduler.cancel()
        scheduler.schedule(endAt)
        store.saveEndAtMs(endAt)
        runCountDown(endAt)
    }

    suspend fun cancel() {
        countDownTimer?.cancel()
        countDownTimer = null
        scheduler.cancel()
        store.clear()
        _state.value = RestTimerState()
    }

    suspend fun restore() {
        val endAt = store.observeEndAtMs().first()
        if (endAt == null) {
            countDownTimer?.cancel()
            _state.value = RestTimerState()
            return
        }

        val remaining = endAt - System.currentTimeMillis()
        if (remaining <= 0) {
            scheduler.cancel()
            store.clear()
            _state.value = RestTimerState()
        } else {
            runCountDown(endAt)
        }
    }

    private fun runCountDown(endAt: Long) {
        countDownTimer?.cancel()
        val remaining = (endAt - System.currentTimeMillis()).coerceAtLeast(0L)
        _state.value = RestTimerState(isRunning = true, remainingMs = remaining, endAtMs = endAt)

        countDownTimer = object : CountDownTimer(remaining, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                _state.value = RestTimerState(
                    isRunning = true,
                    remainingMs = millisUntilFinished,
                    endAtMs = endAt
                )
            }

            override fun onFinish() {
                scope.launch {
                    scheduler.cancel()
                    store.clear()
                }
                _state.value = RestTimerState()
            }
        }.start()
    }
}

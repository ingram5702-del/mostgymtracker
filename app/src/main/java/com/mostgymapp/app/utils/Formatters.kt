package com.mostgymapp.app.utils

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private val oneDecimal = DecimalFormat("0.0")
private val twoDigits = DecimalFormat("00")

fun formatDateTime(epochMs: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return formatter.format(Date(epochMs))
}

fun formatDuration(startMs: Long, endMs: Long?): String {
    val end = endMs ?: System.currentTimeMillis()
    val totalMinutes = TimeUnit.MILLISECONDS.toMinutes((end - startMs).coerceAtLeast(0L))
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

fun formatWeight(value: Double): String = oneDecimal.format(value)

fun formatVolume(value: Double): String = oneDecimal.format(value)

fun formatTrend(value: Double): String {
    val sign = if (value >= 0) "+" else ""
    return "$sign${oneDecimal.format(value)}"
}

fun formatTimer(ms: Long): String {
    val seconds = (ms / 1000).coerceAtLeast(0)
    val min = seconds / 60
    val sec = seconds % 60
    return "${twoDigits.format(min)}:${twoDigits.format(sec)}"
}

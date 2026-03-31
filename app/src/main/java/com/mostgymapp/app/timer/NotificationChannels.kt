package com.mostgymapp.app.timer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationChannels @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val REST_TIMER_CHANNEL_ID = "rest_timer"
    }

    fun create() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            REST_TIMER_CHANNEL_ID,
            "Rest Timer",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for rest timer completion"
            enableVibration(true)
        }

        NotificationManagerCompat.from(context).createNotificationChannel(channel)
    }
}

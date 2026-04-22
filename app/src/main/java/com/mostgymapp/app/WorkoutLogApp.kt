package com.mostgymapp.app

import android.app.Application
import com.mostgymapp.app.timer.NotificationChannels
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class WorkoutLogApp : Application() {

    companion object {
        const val POSTHOG_API_KEY = "phc_ktCi25BikUosSGBxQn8N9LNCGGv8LKVTJQonQPCaSxPV"
        const val POSTHOG_HOST = "https://eu.i.posthog.com"
    }

    @Inject
    lateinit var notificationChannels: NotificationChannels

    override fun onCreate() {
        super.onCreate()
        notificationChannels.create()

        val config = PostHogAndroidConfig(
            apiKey = POSTHOG_API_KEY,
            host = POSTHOG_HOST
        ).apply {
            captureScreenViews = false
        }
        PostHogAndroid.setup(this, config)
    }
}

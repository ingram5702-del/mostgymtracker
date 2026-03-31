package com.mostgymapp.app

import android.app.Application
import com.mostgymapp.app.timer.NotificationChannels
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class WorkoutLogApp : Application() {

    @Inject
    lateinit var notificationChannels: NotificationChannels

    override fun onCreate() {
        super.onCreate()
        notificationChannels.create()
    }
}

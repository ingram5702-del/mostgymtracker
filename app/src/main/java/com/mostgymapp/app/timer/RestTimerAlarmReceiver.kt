package com.mostgymapp.app.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RestTimerAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notifier: RestTimerNotifier

    @Inject
    lateinit var store: RestTimerStore

    override fun onReceive(context: Context?, intent: Intent?) {
        notifier.notifyFinished()
        CoroutineScope(Dispatchers.IO).launch {
            store.clear()
        }
    }
}

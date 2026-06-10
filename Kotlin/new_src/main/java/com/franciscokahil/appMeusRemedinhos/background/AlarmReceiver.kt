package com.franciscokahil.appMeusRemedinhos.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("EXTRA_TITLE") ?: "Hora do Remédio"
        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: "Não se esqueça da sua medicação!"

        val notificationHelper = NotificationHelper(context)
        notificationHelper.showNotification(title, message)
    }
}

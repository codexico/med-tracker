package com.franciscokahil.appMeusRemedinhos.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.franciscokahil.appMeusRemedinhos.R

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("EXTRA_TITLE") ?: context.getString(R.string.notification_default_title)
        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: context.getString(R.string.notification_default_msg)

        val notificationHelper = NotificationHelper(context)
        notificationHelper.showNotification(title, message)
    }
}

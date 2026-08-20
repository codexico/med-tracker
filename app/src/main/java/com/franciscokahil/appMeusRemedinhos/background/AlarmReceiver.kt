package com.franciscokahil.appMeusRemedinhos.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.franciscokahil.appMeusRemedinhos.R
import com.franciscokahil.appMeusRemedinhos.widget.WidgetUpdateManager

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_MIDNIGHT_REFRESH = "com.franciscokahil.appMeusRemedinhos.ACTION_MIDNIGHT_REFRESH"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_MIDNIGHT_REFRESH) {
            WidgetUpdateManager.updateWidgets(context)
            return
        }

        val title = intent.getStringExtra("EXTRA_TITLE") ?: context.getString(R.string.notification_default_title)
        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: context.getString(R.string.notification_default_msg)

        val notificationHelper = NotificationHelper(context)
        notificationHelper.showNotification(title, message)

        // Ensure widget is in sync (marks item as late)
        WidgetUpdateManager.updateWidgets(context)
    }
}

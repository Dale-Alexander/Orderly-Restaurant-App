package com.example.orderly.utils

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.orderly.R
import com.example.orderly.data.datastore.UserPreferences
import kotlinx.coroutines.flow.first

suspend fun sendNotification(context: Context, title:String, message:String){
    val notificationsEnabled = UserPreferences(context).notificationsEnabled.first()
    if(!notificationsEnabled) return
    val notification = NotificationCompat.Builder(context, "info_channel")
        .setSmallIcon(R.drawable.outline_order_approve_24)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /* if the user denied permission, notificationManager.notify() silently doesnt nothing */
    notificationManager.notify(1, notification)
}
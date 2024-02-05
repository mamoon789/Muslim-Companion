package com.iqra.alquran.receivers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_DEFAULT
import androidx.core.app.NotificationManagerCompat
import com.iqra.alquran.R
import com.iqra.alquran.utils.Constants

class NamazTimeUpdateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("TAG_NAMAZ","alarm ringing")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(context)
        }
        val notificationBuilder = NotificationCompat.Builder(context!!, "channel")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.alert))
            .setContentText(context.getString(R.string.alarm_push, intent?.data.toString()))
            .setDefaults(Notification.DEFAULT_SOUND)
            .setPriority(PRIORITY_DEFAULT)

        NotificationManagerCompat.from(context).notify(200, notificationBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createChannel(context: Context?){
        val channel = NotificationChannel("channel", "test", NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = "test channel"

        val notificationManager = context?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
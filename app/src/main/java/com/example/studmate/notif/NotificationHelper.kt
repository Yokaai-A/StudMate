package com.example.studmate.notif

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.studmate.MainActivity
import com.example.studmate.R

object NotificationHelper {
    const val CHANNEL_REMINDER = "class_reminder"
    const val CHANNEL_UPDATES = "class_updates"

    fun createChannels(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannels(
            listOf(
                NotificationChannel(CHANNEL_REMINDER, "Pengingat Kelas", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Pengingat H-1 dan 15 menit sebelum kelas dimulai"
                },
                NotificationChannel(CHANNEL_UPDATES, "Pengumuman & Transaksi", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Pengumuman tutor, status pembayaran, dan balasan diskusi"
                },
            )
        )
    }

    fun show(context: Context, title: String, body: String, deepLink: String?, channel: String = CHANNEL_UPDATES) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) return
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            deepLink?.let { data = it.toUri() }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pi = PendingIntent.getActivity(
            context, (deepLink ?: title).hashCode(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val n = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        @Suppress("MissingPermission")
        NotificationManagerCompat.from(context).notify((title + body + System.nanoTime()).hashCode(), n)
    }
}

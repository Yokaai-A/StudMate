package com.example.studmate.data.repo

import android.content.Context
import com.example.studmate.data.local.CommunityDao
import com.example.studmate.data.local.NotificationEntity
import com.example.studmate.data.prefs.SessionStore
import com.example.studmate.notif.NotificationHelper
import com.example.studmate.util.newId

/**
 * Mengirim notifikasi ke pengguna: disimpan di inbox (Room) dan ditampilkan sebagai
 * push notification bila penerima adalah akun yang sedang login di perangkat ini.
 * Saat backend tersedia, titik ini yang diganti dengan pengiriman FCM dari server.
 */
class Notifier(
    private val context: Context,
    private val community: CommunityDao,
    private val session: SessionStore,
) {
    suspend fun notify(userIds: Collection<String>, title: String, body: String, classId: String? = null, type: String = "INFO", deepLink: String? = null) {
        if (userIds.isEmpty()) return
        community.upsertNotifications(userIds.distinct().map { NotificationEntity(newId(), it, title, body, classId, type) })
        val current = session.currentUserIdNow()
        if (current != null && current in userIds) {
            NotificationHelper.show(context, title, body, deepLink ?: classId?.let { "studmate://class/$it" })
        }
    }
}

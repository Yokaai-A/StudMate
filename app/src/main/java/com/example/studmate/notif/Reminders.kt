package com.example.studmate.notif

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.studmate.data.local.CommunityDao
import com.example.studmate.data.local.NotificationEntity
import com.example.studmate.data.local.SessionEntity
import com.example.studmate.util.DAY
import com.example.studmate.util.formatTime
import com.example.studmate.util.newId
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

/** Menjadwalkan pengingat otomatis H-1 dan 15 menit sebelum setiap sesi. */
class ReminderScheduler(private val context: Context) {
    private val wm get() = WorkManager.getInstance(context)

    fun schedule(enrollmentId: String, userId: String, classId: String, classTitle: String, sessions: List<SessionEntity>) {
        val now = System.currentTimeMillis()
        sessions.filter { !it.isDone }.forEach { s ->
            listOf(
                Triple("h1", s.startAt - DAY, "Besok: $classTitle"),
                Triple("m15", s.startAt - 15 * 60_000L, "15 menit lagi: $classTitle"),
            ).forEach { (kind, at, title) ->
                if (at > now) {
                    val req = OneTimeWorkRequestBuilder<ReminderWorker>()
                        .setInitialDelay(at - now, TimeUnit.MILLISECONDS)
                        .addTag(tag(enrollmentId))
                        .setInputData(
                            workDataOf(
                                "title" to title,
                                "body" to "${s.title} dimulai pukul ${formatTime(s.startAt)} WIB. Buka Class Hub untuk link/lokasi.",
                                "classId" to classId,
                                "userId" to userId,
                            )
                        ).build()
                    wm.enqueueUniqueWork("rem_${enrollmentId}_${s.id}_$kind", ExistingWorkPolicy.REPLACE, req)
                }
            }
        }
    }

    fun cancel(enrollmentId: String) {
        wm.cancelAllWorkByTag(tag(enrollmentId))
    }

    private fun tag(enrollmentId: String) = "enroll_$enrollmentId"
}

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params), KoinComponent {
    private val community: CommunityDao by inject()

    override suspend fun doWork(): Result {
        val title = inputData.getString("title") ?: return Result.success()
        val body = inputData.getString("body").orEmpty()
        val classId = inputData.getString("classId")
        val userId = inputData.getString("userId")
        if (userId != null) {
            community.upsertNotifications(
                listOf(NotificationEntity(newId(), userId, title, body, classId, type = "REMINDER"))
            )
        }
        NotificationHelper.show(
            applicationContext, title, body,
            classId?.let { "studmate://hub/$it" }, NotificationHelper.CHANNEL_REMINDER,
        )
        return Result.success()
    }
}

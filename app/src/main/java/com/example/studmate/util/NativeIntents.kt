package com.example.studmate.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.provider.CalendarContract
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import com.example.studmate.data.local.ClassEntity
import com.example.studmate.data.local.SessionEntity
import com.example.studmate.domain.AppConfig
import com.example.studmate.domain.ClassFormat
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder

/** Integrasi native Android: Share Sheet, Calendar, Maps, browser, LinkedIn. */
object NativeIntents {

    fun openUrl(context: Context, url: String) {
        val fixed = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
        safeStart(context, Intent(Intent.ACTION_VIEW, fixed.toUri()))
    }

    fun openUri(context: Context, uri: String) {
        val u = uri.toUri()
        if (u.scheme == "content") {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(u, context.contentResolver.getType(u) ?: "*/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            safeStart(context, intent)
        } else openUrl(context, uri)
    }

    fun openMaps(context: Context, query: String) {
        val uri = "geo:0,0?q=${Uri.encode(query)}".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            openUrl(context, "https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}")
        }
    }

    fun addToCalendar(context: Context, cls: ClassEntity, session: SessionEntity) {
        val location = if (cls.format == ClassFormat.ONLINE) cls.meetingLink else "${cls.location}, ${cls.university}"
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, session.startAt)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, session.startAt + session.durationMinutes * 60_000L)
            putExtra(CalendarContract.Events.TITLE, "[StudMate] ${cls.title} — ${session.title}")
            putExtra(CalendarContract.Events.DESCRIPTION, "${session.description}\n\n${AppConfig.classLink(cls.id)}")
            putExtra(CalendarContract.Events.EVENT_LOCATION, location)
        }
        safeStart(context, intent)
    }

    fun shareText(context: Context, text: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        safeStart(context, Intent.createChooser(send, "Bagikan via"))
    }

    fun shareFile(context: Context, file: File, mime: String, text: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        safeStart(context, Intent.createChooser(send, "Bagikan via"))
    }

    fun viewFile(context: Context, file: File, mime: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        safeStart(context, intent)
    }

    fun linkedInCertificateUrl(certName: String, certId: String, issuedAt: Long): String {
        val d = localDate(issuedAt)
        fun enc(s: String) = URLEncoder.encode(s, "UTF-8")
        return "https://www.linkedin.com/profile/add?startTask=CERTIFICATION_NAME" +
            "&name=${enc(certName)}&organizationName=${enc("StudMate")}" +
            "&issueYear=${d.year}&issueMonth=${d.monthValue}" +
            "&certId=${enc(certId)}&certUrl=${enc("https://${AppConfig.DEEP_LINK_HOST}/cert/$certId")}"
    }

    private fun safeStart(context: Context, intent: Intent) {
        try {
            if (context !is android.app.Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "Tidak ada aplikasi yang dapat membuka ini", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareDir(context: Context) = File(context.cacheDir, "share").apply { mkdirs() }

    /* ------------------------- Auto-Promo Card ------------------------- */

    data class PromoInfo(
        val classId: String,
        val title: String,
        val organizer: String,
        val university: String,
        val schedule: String,
        val format: String,
        val price: String,
        val type: String,
        val coverStyle: Int,
    )

    fun promoText(info: PromoInfo): String =
        "📚 ${info.title}\n👤 ${info.organizer} • ${info.university}\n🗓️ ${info.schedule}\n📍 ${info.format} • ${info.price}\n\n" +
            "Daftar langsung di StudMate: ${AppConfig.classLink(info.classId)}"

    fun buildPromoCard(context: Context, info: PromoInfo): File {
        val w = 1080
        val h = 1350
        val bmp = createBitmap(w, h)
        val c = Canvas(bmp)
        val (c1, c2) = gradientFor(info.coverStyle)
        c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), Paint().apply {
            shader = LinearGradient(0f, 0f, w.toFloat(), h.toFloat(), c1, c2, Shader.TileMode.CLAMP)
        })
        val white = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        c.drawCircle(w - 80f, 120f, 260f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(30, 255, 255, 255) })
        c.drawCircle(60f, h - 260f, 200f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(24, 255, 255, 255) })

        val brand = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textSize = 52f; typeface = Typeface.DEFAULT_BOLD }
        c.drawText("StudMate", 80f, 130f, brand)
        val chip = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textSize = 36f }
        drawPill(c, info.type.uppercase(), 80f, 200f, chip)

        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textSize = 86f; typeface = Typeface.DEFAULT_BOLD }
        val titleLayout = StaticLayout.Builder.obtain(info.title, 0, info.title.length, titlePaint, w - 160)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL).setMaxLines(4).setEllipsize(android.text.TextUtils.TruncateAt.END).build()
        c.save(); c.translate(80f, 320f); titleLayout.draw(c); c.restore()

        val body = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(235, 255, 255, 255); textSize = 42f }
        var y = 320f + titleLayout.height + 90f
        listOf("👤  ${info.organizer}", "🏫  ${info.university}", "🗓️  ${info.schedule}", "📍  ${info.format}").forEach {
            c.drawText(it, 80f, y, body); y += 70f
        }

        // panel bawah
        val panel = RectF(60f, h - 400f, w - 60f, h - 60f)
        c.drawRoundRect(panel, 48f, 48f, white)
        val qr = QrUtils.bitmap(AppConfig.classLink(info.classId), 280)
        c.drawBitmap(qr, panel.right - 310f, panel.top + 30f, null)
        val dark = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = c1; textSize = 92f; typeface = Typeface.DEFAULT_BOLD }
        c.drawText(info.price, panel.left + 50f, panel.top + 140f, dark)
        val small = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.DKGRAY; textSize = 36f }
        c.drawText("Scan / klik link untuk daftar", panel.left + 50f, panel.top + 215f, small)
        c.drawText(AppConfig.DEEP_LINK_HOST + "/class", panel.left + 50f, panel.top + 270f, small.apply { typeface = Typeface.DEFAULT_BOLD })

        val file = File(shareDir(context), "promo_${info.classId}.png")
        FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return file
    }

    private fun drawPill(c: Canvas, text: String, x: Float, y: Float, paint: TextPaint) {
        val tw = paint.measureText(text)
        c.drawRoundRect(RectF(x, y - 50f, x + tw + 60f, y + 20f), 40f, 40f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(60, 255, 255, 255) })
        c.drawText(text, x + 30f, y, paint)
    }

    fun gradientFor(style: Int): Pair<Int, Int> = when (style.mod(6)) {
        0 -> Color.rgb(67, 56, 202) to Color.rgb(14, 165, 233)
        1 -> Color.rgb(13, 148, 136) to Color.rgb(34, 197, 94)
        2 -> Color.rgb(219, 39, 119) to Color.rgb(249, 115, 22)
        3 -> Color.rgb(124, 58, 237) to Color.rgb(236, 72, 153)
        4 -> Color.rgb(30, 64, 175) to Color.rgb(99, 102, 241)
        else -> Color.rgb(180, 83, 9) to Color.rgb(234, 179, 8)
    }

    /* ------------------------- E-Certificate PDF ------------------------- */

    data class CertificateInfo(
        val certificateId: String,
        val participant: String,
        val classTitle: String,
        val organizer: String,
        val university: String,
        val totalHours: String,
        val issuedAt: Long,
    )

    fun buildCertificatePdf(context: Context, info: CertificateInfo): File {
        val w = 842
        val h = 595 // A4 landscape (pt)
        val doc = PdfDocument()
        val page = doc.startPage(PdfDocument.PageInfo.Builder(w, h, 1).create())
        val c = page.canvas
        val primary = Color.rgb(67, 56, 202)
        c.drawColor(Color.WHITE)
        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 6f; color = primary }
        c.drawRect(24f, 24f, w - 24f, h - 24f, border)
        c.drawRect(36f, 36f, w - 36f, h - 36f, Paint(border).apply { strokeWidth = 1.5f; color = Color.rgb(14, 165, 233) })

        fun centered(text: String, y: Float, size: Float, color: Int = Color.DKGRAY, bold: Boolean = false) {
            val p = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = size; this.color = color; textAlign = Paint.Align.CENTER
                typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            }
            c.drawText(text, w / 2f, y, p)
        }
        centered("StudMate", 90f, 22f, primary, true)
        centered("SERTIFIKAT PENYELESAIAN", 140f, 30f, Color.BLACK, true)
        centered("Diberikan kepada", 190f, 14f)
        centered(info.participant, 245f, 38f, primary, true)
        centered("atas keberhasilannya menyelesaikan kelas", 290f, 14f)
        centered(info.classTitle, 330f, 22f, Color.BLACK, true)
        centered("diselenggarakan oleh ${info.organizer} • ${info.university}", 365f, 13f)
        centered("Total durasi ${info.totalHours} • Diterbitkan ${formatDate(info.issuedAt)}", 390f, 13f)
        c.drawLine(w / 2f - 120f, 480f, w / 2f + 120f, 480f, Paint().apply { color = Color.GRAY })
        centered(info.organizer, 500f, 13f, Color.BLACK, true)
        centered("ID Sertifikat: ${info.certificateId}", 545f, 11f, Color.GRAY)
        centered("Verifikasi: https://${AppConfig.DEEP_LINK_HOST}/cert/${info.certificateId}", 560f, 10f, Color.GRAY)
        val qr = QrUtils.bitmap("https://${AppConfig.DEEP_LINK_HOST}/cert/${info.certificateId}", 200)
        c.drawBitmap(qr, null, RectF(w - 140f, h - 140f, w - 56f, h - 56f), null)
        doc.finishPage(page)
        val file = File(shareDir(context), "sertifikat_${info.certificateId}.pdf")
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }
}

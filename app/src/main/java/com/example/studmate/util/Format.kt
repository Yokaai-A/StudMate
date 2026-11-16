package com.example.studmate.util

import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import kotlin.math.abs

val LocaleID: Locale = Locale.forLanguageTag("id-ID")
private val zone: ZoneId get() = ZoneId.systemDefault()
private val dateFmt = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", LocaleID)
private val shortDateFmt = DateTimeFormatter.ofPattern("d MMM", LocaleID)
private val timeFmt = DateTimeFormatter.ofPattern("HH.mm", LocaleID)
private val dayTimeFmt = DateTimeFormatter.ofPattern("EEE, d MMM • HH.mm", LocaleID)

fun rupiah(amount: Long): String {
    val nf = NumberFormat.getNumberInstance(LocaleID)
    return "Rp " + nf.format(amount)
}

fun priceLabel(amount: Long): String = if (amount <= 0) "Gratis" else rupiah(amount)

fun formatDate(millis: Long): String = Instant.ofEpochMilli(millis).atZone(zone).format(dateFmt)
fun formatShortDate(millis: Long): String = Instant.ofEpochMilli(millis).atZone(zone).format(shortDateFmt)
fun formatTime(millis: Long): String = Instant.ofEpochMilli(millis).atZone(zone).format(timeFmt)
fun formatDateTime(millis: Long): String = "${formatDate(millis)} • ${formatTime(millis)} WIB"
fun formatDayTime(millis: Long): String = if (millis <= 0) "Jadwal fleksibel" else "${Instant.ofEpochMilli(millis).atZone(zone).format(dayTimeFmt)} WIB"


fun localDate(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

fun durationLabel(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h == 0 -> "$m mnt"
        m == 0 -> "$h jam"
        else -> "$h jam $m mnt"
    }
}

fun relativeTime(millis: Long, now: Long = System.currentTimeMillis()): String {
    val diff = now - millis
    val min = abs(diff) / 60_000
    val future = diff < 0
    val text = when {
        min < 1 -> return "baru saja"
        min < 60 -> "$min menit"
        min < 60 * 24 -> "${min / 60} jam"
        min < 60 * 24 * 30 -> "${min / (60 * 24)} hari"
        else -> return formatDate(millis)
    }
    return if (future) "$text lagi" else "$text lalu"
}

fun newId(): String = UUID.randomUUID().toString()

fun newCode(prefix: String, length: Int = 8): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    return prefix + (1..length).map { chars.random() }.joinToString("")
}

fun initials(name: String): String =
    name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifEmpty { "?" }

const val HOUR = 3_600_000L
const val DAY = 24 * HOUR

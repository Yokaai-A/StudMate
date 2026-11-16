package com.example.studmate.util

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

object QrUtils {
    fun bitmap(content: String, size: Int = 640, fg: Int = Color.BLACK, bg: Int = Color.WHITE): Bitmap {
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, mapOf(EncodeHintType.MARGIN to 1))
        val pixels = IntArray(size * size)
        for (y in 0 until size) {
            val offset = y * size
            for (x in 0 until size) pixels[offset + x] = if (matrix[x, y]) fg else bg
        }
        return createBitmap(size, size).apply { setPixels(pixels, 0, size, 0, 0, size, size) }
    }

    fun ticketPayload(ticketCode: String) = "studmate://ticket/$ticketCode"

    /** Ambil kode tiket dari hasil scan (mendukung payload URI maupun kode polos). */
    fun parseTicket(raw: String): String = raw.trim().substringAfter("studmate://ticket/").trim()
}

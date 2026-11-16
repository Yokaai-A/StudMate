package com.example.studmate

import com.example.studmate.data.local.ClassEntity
import com.example.studmate.data.local.UserEntity
import com.example.studmate.domain.Catalog
import com.example.studmate.domain.Category
import com.example.studmate.domain.ClassFormat
import com.example.studmate.domain.FilterState
import com.example.studmate.domain.MentorStats
import com.example.studmate.domain.PriceFilter
import com.example.studmate.domain.ProgramType
import com.example.studmate.util.DAY
import com.example.studmate.util.QrUtils
import com.example.studmate.util.durationLabel
import com.example.studmate.util.formatDayTime
import com.example.studmate.util.priceLabel
import com.example.studmate.util.rupiah
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StudMateLogicTest {

    @Test
    fun testRupiahAndPriceLabelFormatting() {
        assertEquals("Gratis", priceLabel(0))
        assertEquals("Gratis", priceLabel(-500))
        assertTrue(priceLabel(100_000).contains("100.000"))
        assertTrue(rupiah(50_000).startsWith("Rp"))
    }

    @Test
    fun testDurationLabel() {
        assertEquals("45 mnt", durationLabel(45))
        assertEquals("2 jam", durationLabel(120))
        assertEquals("1 jam 30 mnt", durationLabel(90))
    }

    @Test
    fun testQrTicketParsing() {
        val payload = QrUtils.ticketPayload("SM-KTLN88")
        assertEquals("studmate://ticket/SM-KTLN88", payload)
        assertEquals("SM-KTLN88", QrUtils.parseTicket(payload))
        assertEquals("SM-KTLN88", QrUtils.parseTicket("SM-KTLN88"))
    }

    @Test
    fun testClassEarlyBirdPricing() {
        val now = 1_000_000L
        val clsWithEb = ClassEntity(
            id = "test-1",
            organizerId = "org-1",
            title = "Test Class",
            description = "Desc",
            category = Category.TECH,
            programType = ProgramType.WORKSHOP,
            format = ClassFormat.ONLINE,
            university = "UI",
            price = 100_000,
            earlyBirdPrice = 70_000,
            earlyBirdUntil = now + 1000L,
        )

        assertTrue(clsWithEb.isEarlyBird(now))
        assertEquals(70_000, clsWithEb.priceAt(now))

        assertFalse(clsWithEb.isEarlyBird(now + 2000L))
        assertEquals(100_000, clsWithEb.priceAt(now + 2000L))
    }

    @Test
    fun testFilterStateActiveCount() {
        val defaultFilter = FilterState()
        assertEquals(0, defaultFilter.activeCount)

        val customFilter = FilterState(
            categories = setOf(Category.TECH, Category.BUSINESS),
            price = PriceFilter.FREE,
        )
        assertEquals(3, customFilter.activeCount)
    }

    @Test
    fun testMentorStatsCalculations() {
        val now = System.currentTimeMillis()
        val dummyUser = UserEntity(
            id = "u-1",
            name = "Test Tutor",
            email = "tutor@itb.ac.id",
            joinedAt = now - 10 * DAY,
        )
        val stats = MentorStats(
            user = dummyUser,
            classCount = 1,
            studentCount = 20,
            avgRating = 4.8,
            reviewCount = 5,
            firstClassAt = now - 5 * DAY,
        )

        assertTrue(stats.isNew)
        assertTrue(stats.score > 0)
    }

    @Test
    fun testStrictCourseNamingPolicy() {
        assertTrue(Catalog.validateCourseTitle("Struktur Data").first)
        assertTrue(Catalog.validateCourseTitle("Kalkulus Dasar").first)
        assertTrue(Catalog.validateCourseTitle("Basis Data").first)
        assertTrue(Catalog.validateCourseTitle("Statistika dan Probabilitas").first)

        assertFalse(Catalog.validateCourseTitle("[IF2110] Struktur Data").first)
        assertFalse(Catalog.validateCourseTitle("CS101 Algoritma").first)
        assertFalse(Catalog.validateCourseTitle("Kalkulus MTH-102").first)

        assertFalse(Catalog.validateCourseTitle("Bootcamp Struktur Data Pemula").first)
        assertFalse(Catalog.validateCourseTitle("Kelas Kilat Kalkulus Dasar Auto A").first)
        assertFalse(Catalog.validateCourseTitle("Mastering Basis Data").first)
        assertFalse(Catalog.validateCourseTitle("Jago Kalkulus dalam 3 Hari").first)
    }

    @Test
    fun testFormatDayTime() {
        val millis = 1731920400000L
        val formatted = formatDayTime(millis)
        assertTrue(formatted.contains("WIB"))
    }
}

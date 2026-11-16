package com.example.studmate.domain

enum class Category(val label: String) {
    ACADEMIC("Akademik/Matkul"),
    TECH("Tech & Digital"),
    BUSINESS("Bisnis"),
    LANGUAGE("Bahasa"),
    DESIGN("Desain"),
    CAREER("Karir"),
}

enum class ProgramType(val label: String) {
    TUTORING("1-on-1 Tutoring"),
    STUDY_GROUP("Study Group"),
    WORKSHOP("Workshop"),
    BOOTCAMP("Intensive Bootcamp"),
}

enum class ClassFormat(val label: String) {
    ONLINE("Online Live"),
    OFFLINE("Offline Kampus"),
    HYBRID("Hybrid"),
}

enum class ClassStatus { DRAFT, PUBLISHED, COMPLETED, CANCELLED }

enum class EnrollmentStatus { PENDING_PAYMENT, ACTIVE, COMPLETED, CANCELLED }

enum class PaymentStatus(val label: String) {
    FREE("Gratis"),
    UNPAID("Menunggu Pembayaran"),
    HELD("Ditahan Escrow"),
    RELEASED("Diteruskan ke Organizer"),
    REFUNDED("Dikembalikan"),
}

enum class PaymentMethod(val label: String) {
    QRIS("QRIS"),
    GOPAY("GoPay"),
    DANA("DANA"),
    SHOPEEPAY("ShopeePay"),
    VA_BCA("Virtual Account BCA"),
    VA_BNI("Virtual Account BNI"),
    VA_MANDIRI("Virtual Account Mandiri"),
}

enum class VerificationStatus { NONE, PENDING, VERIFIED }

enum class OrganizerType(val label: String) {
    INDIVIDUAL("Tutor Individu"),
    ORGANIZATION("Organisasi/Komunitas"),
}

enum class MaterialType(val label: String) { SLIDE("Slide"), PDF("PDF"), RECORDING("Rekaman"), LINK("Tautan") }

enum class PriceFilter(val label: String) { ALL("Semua"), FREE("Gratis"), PAID("Berbayar") }

enum class SortOption(val label: String) {
    RELEVANT("Paling Relevan"),
    SOONEST("Segera Dimulai"),
    CHEAPEST("Termurah"),
    RATING("Rating Tertinggi"),
}

enum class ThemeMode(val label: String) { SYSTEM("Ikuti Sistem"), LIGHT("Terang"), DARK("Gelap") }

object AppConfig {

    const val PLATFORM_FEE_PERCENT = 5
    const val DEEP_LINK_HOST = "studmate.app"
    const val MAX_COMPARE = 3
    fun classLink(classId: String) = "https://$DEEP_LINK_HOST/class/$classId"
}

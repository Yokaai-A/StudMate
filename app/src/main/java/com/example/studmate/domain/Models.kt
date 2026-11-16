package com.example.studmate.domain

import com.example.studmate.data.local.ClassEntity
import com.example.studmate.data.local.EnrollmentEntity
import com.example.studmate.data.local.UserEntity

data class ClassSummary(
    val cls: ClassEntity,
    val organizer: UserEntity?,
    val enrolled: Int,
    val rating: Double,
    val reviewCount: Int,
    val sessionCount: Int,
    val totalMinutes: Int,
) {
    val remaining: Int get() = (cls.quota - enrolled).coerceAtLeast(0)
    val isFull: Boolean get() = remaining == 0
}

data class MentorStats(
    val user: UserEntity,
    val classCount: Int,
    val studentCount: Int,
    val avgRating: Double,
    val reviewCount: Int,
    val firstClassAt: Long?,
) {

    val isNew: Boolean
        get() = classCount <= 2 || System.currentTimeMillis() - user.joinedAt < 60L * 24 * 3600 * 1000

    val score: Double get() = studentCount * 2.0 + avgRating * reviewCount * 3.0 + classCount * 5.0
}

data class FilterState(
    val query: String = "",
    val categories: Set<Category> = emptySet(),
    val types: Set<ProgramType> = emptySet(),
    val formats: Set<ClassFormat> = emptySet(),
    val price: PriceFilter = PriceFilter.ALL,
    val maxPrice: Long = MAX_PRICE,
    val dateFrom: Long? = null,
    val dateTo: Long? = null,
    val sort: SortOption = SortOption.RELEVANT,
) {
    val activeCount: Int
        get() = categories.size + types.size + formats.size +
            (if (price != PriceFilter.ALL) 1 else 0) +
            (if (maxPrice < MAX_PRICE) 1 else 0) +
            (if (dateFrom != null || dateTo != null) 1 else 0)

    companion object { const val MAX_PRICE = 1_000_000L }
}

data class LearningItem(
    val enrollment: EnrollmentEntity,
    val summary: ClassSummary,
    val nextSessionAt: Long?,
    val attended: Int,
)

object Catalog {
    val universities = listOf(
        "Universitas Indonesia", "Institut Teknologi Bandung", "Universitas Gadjah Mada",
        "Institut Teknologi Sepuluh Nopember", "IPB University", "Universitas Padjadjaran",
        "Universitas Diponegoro", "Universitas Airlangga", "Universitas Brawijaya",
        "Universitas Bina Nusantara", "Telkom University", "Universitas Negeri Yogyakarta",
        "Universitas Hasanuddin", "Universitas Sumatera Utara", "Universitas Udayana",
        "Universitas Pendidikan Indonesia", "Universitas Sebelas Maret", "Universitas Andalas",
        "Universitas Islam Indonesia", "Universitas Muhammadiyah Malang",
    )

    val faculties = listOf(
        "Fakultas Ilmu Komputer", "Fakultas Teknik", "Fakultas Matematika dan IPA",
        "Fakultas Ekonomi dan Bisnis", "Fakultas Psikologi", "Fakultas Hukum",
        "Fakultas Kedokteran", "Fakultas Ilmu Sosial dan Ilmu Politik",
        "Fakultas Ilmu Pengetahuan Budaya", "Fakultas Farmasi",
    )

    val majors = listOf(
        "Ilmu Komputer", "Sistem Informasi", "Teknik Informatika", "Teknik Elektro", "Teknik Industri",
        "Teknik Sipil", "Matematika", "Statistika", "Fisika", "Kimia", "Biologi", "Akuntansi",
        "Manajemen", "Ilmu Ekonomi", "Ilmu Komunikasi", "Hubungan Internasional", "Psikologi",
        "Hukum", "Kedokteran", "Farmasi", "Desain Komunikasi Visual", "Arsitektur",
        "Sastra Inggris", "Sastra Jepang", "Pendidikan",
    )


    val standardCourses = listOf(
        "Struktur Data",
        "Kalkulus Dasar",
        "Basis Data",
        "Statistika dan Probabilitas",
        "Algoritma dan Pemrograman",
        "Akuntansi Keuangan Menengah",
        "Kimia Organik",
        "Matematika Diskrit",
        "Sistem Operasi",
        "Jaringan Komputer",
        "Rekayasa Perangkat Lunak",
        "Kecerdasan Buatan",
        "Kimia Dasar",
        "Fisika Dasar",
        "Mikroekonomi",
        "Makroekonomi",
        "Manajemen Keuangan",
        "Hukum Perdata",
        "Psikologi Umum",
        "Aljabar Linear Elementer",
        "Pemrograman Berorientasi Objek",
        "Metode Numerik",
        "Arsitektur Komputer",
        "Sistem Informasi Manajemen",
        "Pengantar Ilmu Komunikasi",
    )

    val interests = listOf(
        "Struktur Data", "Kalkulus Dasar", "Basis Data", "Statistika dan Probabilitas",
        "Algoritma dan Pemrograman", "Akuntansi Keuangan Menengah", "Kimia Organik",
        "Matematika Diskrit", "Sistem Operasi", "Jaringan Komputer", "Fisika Dasar",
    )


    fun validateCourseTitle(title: String): Pair<Boolean, String?> {
        val trimmed = title.trim()
        if (trimmed.isBlank()) return false to "Nama mata kuliah tidak boleh kosong"
        val codeRegex = Regex("""\b[A-Za-z]{2,4}[-\s]?[0-9]{2,4}\b""")
        if (codeRegex.containsMatchIn(trimmed)) {
            return false to "Dilarang menyertakan kode mata kuliah (misal: IF-201). Gunakan nama murni seperti 'Struktur Data'."
        }
        val buzzwords = listOf("bootcamp", "kilat", "jago", "bimbel", "mastering", "auto a", "trik", "cepat", "rahasia", "garansi", "tembus")
        val lower = trimmed.lowercase()
        buzzwords.firstOrNull { lower.contains(it) }?.let {
            return false to "Dilarang menyertakan embel-embel promosi '$it'. Gunakan nama mata kuliah murni."
        }
        return true to null
    }


    val interestCategory = mapOf(
        "Struktur Data" to Category.TECH,
        "Kalkulus Dasar" to Category.ACADEMIC,
        "Basis Data" to Category.TECH,
        "Statistika dan Probabilitas" to Category.ACADEMIC,
        "Algoritma dan Pemrograman" to Category.TECH,
        "Akuntansi Keuangan Menengah" to Category.BUSINESS,
        "Kimia Organik" to Category.ACADEMIC,
        "Matematika Diskrit" to Category.ACADEMIC,
        "Sistem Operasi" to Category.TECH,
        "Jaringan Komputer" to Category.TECH,
        "Fisika Dasar" to Category.ACADEMIC,
    )
}

package com.example.studmate.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studmate.domain.ClassSummary
import com.example.studmate.ui.components.FormatChip
import com.example.studmate.ui.components.VerifiedCampusBadge
import com.example.studmate.ui.viewmodel.CompareViewModel
import com.example.studmate.util.formatDayTime
import com.example.studmate.util.priceLabel
import com.example.studmate.util.rupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    viewModel: CompareViewModel,
    onBack: () -> Unit,
    onSelectClass: (String) -> Unit,
) {
    val comparedClasses by viewModel.comparedClasses.collectAsState()

    // Jika mata kuliah yang dibandingkan memiliki nama yang sama, tampilkan nama tersebut di header
    val commonTitle = if (comparedClasses.isNotEmpty() && comparedClasses.all { it.cls.title == comparedClasses.first().cls.title }) {
        "Bandingkan Kelas: ${comparedClasses.first().cls.title}"
    } else {
        "Bandingkan Mata Kuliah (${comparedClasses.size}/3)"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = commonTitle,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
            )
        }
    ) { padding ->
        if (comparedClasses.size < 2) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CompareArrows,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Pilih Minimal 2 Kelas",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tandai 2 hingga 3 penawaran mata kuliah di halaman Beranda untuk membandingkan tutor, nilai matkul, metode, jadwal, dan tarif secara berdampingan.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(onClick = onBack) {
                        Text("Kembali ke Beranda")
                    }
                }
            }
        } else {
            val colWidth = 220.dp
            val labelColWidth = 110.dp
            val now = System.currentTimeMillis()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(12.dp)
                ) {
                    // Kolom Label Indikator Kriteria
                    Column(modifier = Modifier.width(labelColWidth)) {
                        Spacer(modifier = Modifier.height(105.dp)) // tinggi header kartu
                        CompareRowLabel("Tutor")
                        CompareRowLabel("Universitas")
                        CompareRowLabel("Nilai Matkul")
                        CompareRowLabel("Rating")
                        CompareRowLabel("Metode")
                        CompareRowLabel("Jadwal")
                        CompareRowLabel("Biaya")
                        CompareRowLabel("Topik Bahasan")
                        CompareRowLabel("Aksi")
                    }

                    // Kolom Tiap Tutor / Kelas
                    comparedClasses.forEachIndexed { index, summary ->
                        val cls = summary.cls
                        val tutorLetter = ('A' + index).toString()

                        Column(
                            modifier = Modifier
                                .width(colWidth)
                                .padding(horizontal = 6.dp)
                        ) {
                            // Header Card (Tutor A / Tutor B)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            ) {
                                Box(modifier = Modifier.padding(10.dp)) {
                                    IconButton(
                                        onClick = { viewModel.remove(cls.id) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(22.dp),
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Hapus", modifier = Modifier.size(15.dp))
                                    }
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        Text(
                                            text = "Tutor $tutorLetter",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                            ),
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = cls.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        FormatChip(format = cls.format)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(5.dp))

                            // 1. Kriteria: Tutor
                            CompareCell {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = summary.organizer?.displayOrganizerName ?: "Peer Tutor",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    if (summary.organizer?.isVerified == true) {
                                        Spacer(modifier = Modifier.width(3.dp))
                                        VerifiedCampusBadge(compact = true)
                                    }
                                }
                            }

                            // 2. Kriteria: Universitas
                            CompareCell {
                                Text(
                                    text = cls.university,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                            // 3. Kriteria: Nilai Matkul (Academic Credibility Badge)
                            CompareCell {
                                Surface(
                                    color = Color(0xFFEFF6FF),
                                    shape = RoundedCornerShape(6.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = null,
                                            tint = Color(0xFF1E3A8A),
                                            modifier = Modifier.size(13.dp),
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = cls.courseGrade.ifBlank { "A" },
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFF1E3A8A),
                                                fontWeight = FontWeight.Bold,
                                            ),
                                        )
                                    }
                                }
                            }

                            // 4. Kriteria: Rating
                            CompareCell {
                                if (summary.reviewCount > 0) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "★ ${String.format("%.1f", summary.rating)}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        )
                                        Text(
                                            text = " (${summary.reviewCount} ulasan)",
                                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                        )
                                    }
                                } else {
                                    Text("Belum ada ulasan", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline))
                                }
                            }

                            // 5. Kriteria: Metode
                            CompareCell {
                                Text(
                                    text = cls.format.label,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                )
                            }

                            // 6. Kriteria: Jadwal
                            CompareCell {
                                Text(
                                    text = formatDayTime(cls.startAt),
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                            // 7. Kriteria: Biaya
                            CompareCell {
                                val effectivePrice = cls.priceAt(now)
                                Text(
                                    text = if (effectivePrice <= 0) "Gratis" else rupiah(effectivePrice),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (effectivePrice <= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                                    ),
                                )
                            }

                            // 8. Topik Bahasan
                            CompareCell {
                                Text(
                                    text = cls.topicSummary.ifBlank { cls.description },
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                            // 9. Tombol Aksi: [ Pilih Tutor A ] / [ Pilih Tutor B ]
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(55.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Button(
                                    onClick = { onSelectClass(cls.id) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                ) {
                                    Text("Pilih Tutor $tutorLetter", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompareRowLabel(label: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
    }
}

@Composable
private fun CompareCell(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        content()
        Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

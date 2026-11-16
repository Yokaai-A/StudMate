package com.example.studmate.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studmate.domain.ClassSummary
import com.example.studmate.util.formatDayTime
import com.example.studmate.util.priceLabel
import com.example.studmate.util.rupiah

@Composable
fun ClassCard(
    summary: ClassSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isInCompare: Boolean = false,
    onCompareToggle: (() -> Unit)? = null,
) {
    val cls = summary.cls
    val now = System.currentTimeMillis()
    val isEarlyBird = cls.isEarlyBird(now)
    val effectivePrice = cls.priceAt(now)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 1. Header: [Format Chip] ... ★ Rating ... [Bandingkan Toggle]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    FormatChip(format = cls.format)
                    if (isEarlyBird) EarlyBirdBadge()
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Rating
                    if (summary.reviewCount > 0) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = String.format("%.1f", summary.rating),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        )
                        Text(
                            text = " (${summary.reviewCount})",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        )
                    }

                    // Compare Toggle Pill
                    if (onCompareToggle != null) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = onCompareToggle),
                            color = if (isInCompare) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp),
                            border = if (isInCompare) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = if (isInCompare) Icons.Outlined.Check else Icons.Default.CompareArrows,
                                    contentDescription = "Bandingkan",
                                    tint = if (isInCompare) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(13.dp),
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isInCompare) "Ditandai" else "Bandingkan",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = if (isInCompare) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isInCompare) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    ),
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Headline: Nama Mata Kuliah murni (18sp, SemiBold, #0F172A)
            Text(
                text = cls.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 3. Subtitle: Nama Peer Tutor + Lencana Verifikasi + Nama Universitas (13sp, Medium, #475569)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = summary.organizer?.displayOrganizerName ?: "Peer Tutor",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (summary.organizer?.isVerified == true) {
                    Spacer(modifier = Modifier.width(4.dp))
                    VerifiedCampusBadge(compact = true)
                }
                Text(
                    text = " • ${cls.university}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 4. Topik Bahasan
            val topicText = cls.topicSummary.ifBlank { cls.description }
            Text(
                text = "Topik: $topicText",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Garis Pembatas Tipis (1dp)
            Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

            Spacer(modifier = Modifier.height(12.dp))

            // 6. Baris Info: Jadwal & Tarif per sesi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Jadwal
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatDayTime(cls.startAt),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                }

                // Tarif
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = if (effectivePrice <= 0) "Gratis" else "${rupiah(effectivePrice)}/sesi",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (effectivePrice <= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                    if (isEarlyBird && cls.price > effectivePrice) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = rupiah(cls.price),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.outline,
                                textDecoration = TextDecoration.LineThrough,
                            ),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 7. Footer: Sisa Kuota + Tombol [ Lihat Detail ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Sisa Kursi & Kuota Indicator
                val enrolledRatio = if (cls.quota > 0) summary.enrolled.toFloat() / cls.quota else 0f
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (summary.isFull) "Kursi Penuh" else "Sisa ${summary.remaining} kursi",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            color = if (summary.isFull) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (summary.remaining in 1..4) FontWeight.Bold else FontWeight.Normal,
                        ),
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    LinearProgressIndicator(
                        progress = { enrolledRatio.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = if (summary.isFull) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }

                // Tombol Lihat Detail
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp),
                ) {
                    Text(
                        text = "Lihat Detail",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }
        }
    }
}

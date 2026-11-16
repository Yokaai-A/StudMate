package com.example.studmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studmate.domain.Category
import com.example.studmate.domain.ClassFormat
import com.example.studmate.ui.theme.EarlyBirdOrange
import com.example.studmate.ui.theme.EarlyBirdOrangeLight
import com.example.studmate.ui.theme.EscrowGreen
import com.example.studmate.ui.theme.EscrowGreenLight
import com.example.studmate.ui.theme.VerifiedBlue
import com.example.studmate.ui.theme.VerifiedBlueLight

@Composable
fun VerifiedCampusBadge(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Surface(
        modifier = modifier,
        color = VerifiedBlueLight,
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (compact) 6.dp else 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = "Verified Campus",
                tint = VerifiedBlue,
                modifier = Modifier.size(if (compact) 13.dp else 15.dp),
            )
            if (!compact) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Kampus Terverifikasi",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = VerifiedBlue,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                    ),
                )
            }
        }
    }
}

@Composable
fun EscrowBadge(
    modifier: Modifier = Modifier,
    text: String = "Escrow Dilindungi",
) {
    Surface(
        modifier = modifier,
        color = EscrowGreenLight,
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Escrow",
                tint = EscrowGreen,
                modifier = Modifier.size(13.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = EscrowGreen,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                ),
            )
        }
    }
}

@Composable
fun EarlyBirdBadge(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = EarlyBirdOrangeLight,
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.LocalOffer,
                contentDescription = "Early Bird",
                tint = EarlyBirdOrange,
                modifier = Modifier.size(12.dp),
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "Early Bird",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = EarlyBirdOrange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                ),
            )
        }
    }
}

@Composable
fun AcademicGradeBadge(
    grade: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = Color(0xFFEFF6FF),
        shape = RoundedCornerShape(8.dp),
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
                text = "Nilai Matkul: $grade",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF1E3A8A),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                ),
            )
        }
    }
}

@Composable
fun AcademicRoleBadge(
    role: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = Color(0xFFF0FDF4),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF0D9488),
                modifier = Modifier.size(13.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = role,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF0D9488),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                ),
            )
        }
    }
}

@Composable
fun FormatChip(
    format: ClassFormat,
    modifier: Modifier = Modifier,
) {
    val (bgColor, textColor) = when (format) {
        ClassFormat.ONLINE -> Color(0xFFDBEAFE) to Color(0xFF1E3A8A)
        ClassFormat.OFFLINE -> Color(0xFFCCFBF1) to Color(0xFF0D9488)
        ClassFormat.HYBRID -> Color(0xFFFAF5FF) to Color(0xFF7E22CE)
    }

    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
    ) {
        Text(
            text = format.label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                color = textColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
            ),
        )
    }
}

@Composable
fun CategoryChip(
    category: Category,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(6.dp),
    ) {
        Text(
            text = category.label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
            ),
        )
    }
}

package com.example.studmate.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studmate.domain.EnrollmentStatus
import com.example.studmate.domain.PaymentMethod
import com.example.studmate.domain.PaymentStatus
import com.example.studmate.ui.components.AcademicGradeBadge
import com.example.studmate.ui.components.AcademicRoleBadge
import com.example.studmate.ui.components.CategoryChip
import com.example.studmate.ui.components.EarlyBirdBadge
import com.example.studmate.ui.components.EscrowBadge
import com.example.studmate.ui.components.FormatChip
import com.example.studmate.ui.components.VerifiedCampusBadge
import com.example.studmate.ui.viewmodel.ClassDetailViewModel
import com.example.studmate.util.NativeIntents
import com.example.studmate.util.QrUtils
import com.example.studmate.util.durationLabel
import com.example.studmate.util.formatDate
import com.example.studmate.util.formatDateTime
import com.example.studmate.util.formatTime
import com.example.studmate.util.priceLabel
import com.example.studmate.util.rupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassDetailScreen(
    viewModel: ClassDetailViewModel,
    onBack: () -> Unit,
    onOpenHub: (String) -> Unit,
    onOpenMentor: (String) -> Unit,
) {
    val context = LocalContext.current
    val summary by viewModel.summary.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val reviews by viewModel.reviews.collectAsState()
    val enrollment by viewModel.enrollment.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showPaymentSheet by remember { mutableStateOf(false) }

    val cls = summary?.cls
    val now = System.currentTimeMillis()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(cls?.title.orEmpty(), maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sharePromoCard(context) }) {
                        Icon(Icons.Default.Share, contentDescription = "Bagikan Poster Promo")
                    }
                },
            )
        },
        bottomBar = {
            if (cls != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "Biaya Pendaftaran",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline),
                            )
                            val effPrice = cls.priceAt(now)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = priceLabel(effPrice),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (effPrice <= 0) Color(0xFF16A34A) else MaterialTheme.colorScheme.primary,
                                    ),
                                )
                                if (cls.isEarlyBird(now)) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = rupiah(cls.price),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.outline,
                                            textDecoration = TextDecoration.LineThrough,
                                        ),
                                    )
                                }
                            }
                        }

                        if (enrollment != null && enrollment?.status == EnrollmentStatus.ACTIVE) {
                            Button(
                                onClick = { onOpenHub(cls.id) },
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text("Buka Class Hub 🚀")
                            }
                        } else if (enrollment?.status == EnrollmentStatus.PENDING_PAYMENT) {
                            Button(
                                onClick = { showPaymentSheet = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                            ) {
                                Text("Selesaikan Bayar")
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (cls.isFree) {
                                        viewModel.enroll()
                                    } else {
                                        showPaymentSheet = true
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                enabled = summary?.isFull == false && !uiState.isEnrolling,
                            ) {
                                if (uiState.isEnrolling) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                                } else {
                                    Text(if (summary?.isFull == true) "Kuota Penuh" else "Daftar 1-Tap")
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (cls == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val (c1, c2) = NativeIntents.gradientFor(cls.coverStyle)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .background(Brush.horizontalGradient(listOf(Color(c1), Color(c2))))
                            .padding(16.dp),
                    ) {
                        Column(modifier = Modifier.align(Alignment.BottomStart)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                FormatChip(format = cls.format)
                                CategoryChip(category = cls.category)
                                if (cls.isEarlyBird(now)) EarlyBirdBadge()
                                EscrowBadge()
                            }
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = cls.title,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        )

                        if (cls.topicSummary.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Topik: ${cls.topicSummary}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium,
                                ),
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (cls.location.isNotBlank()) "${cls.location} • ${cls.university}" else "${cls.format.label} • ${cls.university}",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { summary?.organizer?.id?.let { onOpenMentor(it) } },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.size(44.dp),
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = (summary?.organizer?.name ?: "Tutor").take(2).uppercase(),
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = summary?.organizer?.displayOrganizerName ?: "Tutor StudMate",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                )
                                                if (summary?.organizer?.isVerified == true) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    VerifiedCampusBadge(compact = true)
                                                }
                                            }
                                            Text(
                                                text = "${summary?.organizer?.university ?: cls.university} • ${summary?.organizer?.major.orEmpty()}",
                                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline),
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    AcademicGradeBadge(grade = cls.courseGrade.ifBlank { "A" })
                                    summary?.organizer?.academicRole?.let { role ->
                                        if (role.isNotBlank()) {
                                            AcademicRoleBadge(role = role)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val s = summary!!
                        val enrolledRatio = if (cls.quota > 0) s.enrolled.toFloat() / cls.quota else 0f
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = if (s.isFull) "Kuota Penuh" else "Sisa ${s.remaining} kursi lagi",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (s.isFull) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                ),
                            )
                            Text(
                                text = "${s.enrolled}/${cls.quota} terdaftar",
                                style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.outline),
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { enrolledRatio.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                        )
                    }
                }

                item {
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Silabus (${sessions.size})") },
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Tentang & Benefit") },
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("Ulasan (${reviews.size})") },
                        )
                    }
                }

                when (selectedTab) {
                    0 -> {
                        items(sessions) { sess ->
                            SessionItemRow(session = sess)
                        }
                    }
                    1 -> {
                        item {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Deskripsi Kelas", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(cls.description, style = MaterialTheme.typography.bodyMedium)

                                Spacer(modifier = Modifier.height(18.dp))

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF059669))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "Jaminan Aman Escrow StudMate",
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF059669),
                                                ),
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            "Dana pendaftaranmu disimpan di Rekening Bersama resmi StudMate dan baru diteruskan ke tutor setelah sesi kelas berjalan sesuai jadwal. Bebas dari penipuan kelas fiktif.",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF065F46)),
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Text("Benefit yang Didapatkan", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.height(8.dp))
                                cls.benefits.forEach { b ->
                                    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(b, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        if (reviews.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(40.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("Belum ada ulasan dari peserta", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.outline))
                                }
                            }
                        } else {
                            items(reviews) { r ->
                                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(r.user?.name ?: "Peserta Terverifikasi", fontWeight = FontWeight.Bold)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            repeat(r.review.rating) {
                                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(r.review.comment, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(formatDate(r.review.createdAt), style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline))
                                    Divider(modifier = Modifier.padding(top = 10.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showPaymentSheet && cls != null) {
            PaymentBottomSheet(
                cls = cls,
                user = currentUser,
                onDismiss = { showPaymentSheet = false },
                onConfirm = { method ->
                    if (enrollment == null) {
                        viewModel.enroll()
                    }
                    viewModel.pay(method)
                    showPaymentSheet = false
                    Toast.makeText(context, "Pembayaran berhasil diamankan di Escrow!", Toast.LENGTH_SHORT).show()
                },
            )
        }
    }
}

@Composable
private fun SessionItemRow(session: com.example.studmate.data.local.SessionEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(32.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = session.orderIndex.toString(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
                if (session.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(session.description, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${formatDateTime(session.startAt)} • ${durationLabel(session.durationMinutes)}",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentBottomSheet(
    cls: com.example.studmate.data.local.ClassEntity,
    user: com.example.studmate.data.local.UserEntity?,
    onDismiss: () -> Unit,
    onConfirm: (PaymentMethod) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedMethod by remember { mutableStateOf(PaymentMethod.QRIS) }
    val now = System.currentTimeMillis()
    val finalPrice = cls.priceAt(now)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text("Pendaftaran & Pembayaran Escrow", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Text("Data pendaftaran otomatis terisi dari profilmu (1-Tap)", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Nama: ${user?.name ?: "Mahasiswa"}", fontWeight = FontWeight.SemiBold)
                    Text("Email: ${user?.email ?: "-"}", style = MaterialTheme.typography.bodySmall)
                    Text("Kampus: ${user?.university ?: cls.university}", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text("Pilih Metode Pembayaran", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))

            PaymentMethod.entries.forEach { method ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedMethod = method }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = selectedMethod == method,
                        onClick = { selectedMethod = method },
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(method.label, style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (selectedMethod == PaymentMethod.QRIS) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Scan QRIS Menggunakan E-Wallet / Mobile Banking", style = MaterialTheme.typography.labelSmall.copy(color = Color.DarkGray))
                        Spacer(modifier = Modifier.height(8.dp))
                        val qrBitmap = remember { QrUtils.bitmap("00020101021226600016ID.LINKAJA.WWW01189360050300000000000215studmate_payment5802ID5303360540${finalPrice}5802ID6304", 200) }
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QRIS Code",
                            modifier = Modifier.size(180.dp),
                        )
                        Text("Total: ${priceLabel(finalPrice)}", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onConfirm(selectedMethod) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Konfirmasi & Bayar ${priceLabel(finalPrice)} (Simulasi)")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

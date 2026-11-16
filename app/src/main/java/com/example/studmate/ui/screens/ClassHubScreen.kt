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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoCameraFront
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studmate.domain.ClassFormat
import com.example.studmate.domain.MaterialType
import com.example.studmate.ui.viewmodel.ClassHubViewModel
import com.example.studmate.util.NativeIntents
import com.example.studmate.util.QrUtils
import com.example.studmate.util.formatDate
import com.example.studmate.util.formatDateTime
import com.example.studmate.util.relativeTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassHubScreen(
    viewModel: ClassHubViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val summary by viewModel.summary.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val enrollment by viewModel.enrollment.collectAsState()
    val attendance by viewModel.attendance.collectAsState()
    val materials by viewModel.materials.collectAsState()
    val announcements by viewModel.announcements.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val certEligibility by viewModel.certEligibility.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showTicketModal by remember { mutableStateOf(false) }

    var newPostContent by remember { mutableStateOf("") }

    var reviewRating by remember { mutableIntStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }

    val cls = summary?.cls
    val now = System.currentTimeMillis()
    val nextSession = sessions.filter { !it.isDone && it.startAt + it.durationMinutes * 60_000L > now }
        .minByOrNull { it.startAt }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(cls?.title ?: "Class Hub", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { showTicketModal = true }) {
                        Icon(Icons.Default.QrCode, contentDescription = "Tiket QR Presensi")
                    }
                },
            )
        }
    ) { padding ->
        if (cls == null || enrollment == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Sesi Mendatang",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                ),
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showTicketModal = true },
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Tiket Presensi", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (nextSession != null) {
                            Text(
                                text = nextSession.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            )
                            Text(
                                text = formatDateTime(nextSession.startAt),
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onPrimaryContainer),
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        if (cls.format == ClassFormat.ONLINE && cls.meetingLink.isNotBlank()) {
                                            NativeIntents.openUrl(context, cls.meetingLink)
                                        } else {
                                            val loc = if (cls.location.isNotBlank()) "${cls.location}, ${cls.university}" else cls.university
                                            NativeIntents.openMaps(context, loc)
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(
                                        imageVector = if (cls.format == ClassFormat.ONLINE) Icons.Default.VideoCameraFront else Icons.Default.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (cls.format == ClassFormat.ONLINE) "Gabung Sesi" else "Buka Maps")
                                }

                                OutlinedButton(
                                    onClick = {
                                        NativeIntents.addToCalendar(context, cls, nextSession)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                ) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Kalender")
                                }
                            }
                        } else {
                            Text(
                                text = "Semua sesi kelas telah selesai! 🎉",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    val attendedCount = attendance.size
                    val totalSess = sessions.size
                    val pct = if (totalSess == 0) 0 else attendedCount * 100 / totalSess
                    Text(
                        text = "Kehadiran: $attendedCount/$totalSess sesi ($pct%)",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    )
                    if (cls.hasCertificate) {
                        Text(
                            text = "Syarat sertifikat: min ${cls.minAttendancePercent}%",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Materi (${materials.size})") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Pengumuman (${announcements.size})") })
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Diskusi (${posts.size})") })
                    Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Sertifikat & Ulasan") })
                }

                when (selectedTab) {
                    0 -> {
                        if (materials.isEmpty()) {
                            EmptyTabPlaceholder("Belum ada materi dibagikan tutor.")
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(materials) { mat ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 6.dp)
                                            .clickable { NativeIntents.openUrl(context, mat.uri) },
                                        shape = RoundedCornerShape(12.dp),
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = when (mat.type) {
                                                        MaterialType.PDF -> Icons.Default.PictureAsPdf
                                                        MaterialType.RECORDING -> Icons.Default.VideoCameraFront
                                                        else -> Icons.Default.Link
                                                    },
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp),
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(mat.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                                    Text(mat.type.label, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline))
                                                }
                                            }
                                            Icon(Icons.Default.Download, contentDescription = "Buka")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        if (announcements.isEmpty()) {
                            EmptyTabPlaceholder("Belum ada pengumuman disiarkan.")
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(announcements) { ann ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(ann.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(ann.message, style = MaterialTheme.typography.bodyMedium)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(relativeTime(ann.createdAt), style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(modifier = Modifier.weight(1f)) {
                                if (posts.isEmpty()) {
                                    item { EmptyTabPlaceholder("Belum ada pertanyaan. Mulai diskusi pertamamu!") }
                                } else {
                                    items(posts) { item ->
                                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    item.user?.name ?: "Mahasiswa",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodySmall,
                                                )
                                                if (item.isOrganizer) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        color = MaterialTheme.colorScheme.primaryContainer,
                                                        shape = RoundedCornerShape(6.dp),
                                                    ) {
                                                        Text("TUTOR", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(relativeTime(item.post.createdAt), style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline))
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(item.post.content, style = MaterialTheme.typography.bodyMedium)
                                            Divider(modifier = Modifier.padding(top = 8.dp))
                                        }
                                    }
                                }
                            }

                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 4.dp,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    OutlinedTextField(
                                        value = newPostContent,
                                        onValueChange = { newPostContent = it },
                                        placeholder = { Text("Tulis pertanyaan atau tanggapan...") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(20.dp),
                                        maxLines = 3,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            if (newPostContent.isNotBlank()) {
                                                viewModel.addPost(newPostContent)
                                                newPostContent = ""
                                            }
                                        },
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = "Kirim", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                    3 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                        ) {
                            Text("E-Sertifikat Kelulusan", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(8.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (certEligibility?.eligible == true) Color(0xFFECFDF5) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                ),
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.WorkspacePremium,
                                            contentDescription = null,
                                            tint = if (certEligibility?.eligible == true) Color(0xFF059669) else MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(28.dp),
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = if (enrollment?.certificateId != null) "Sertifikat Diterbitkan 🎉" else "Status Sertifikat",
                                                fontWeight = FontWeight.Bold,
                                            )
                                            Text(
                                                text = certEligibility?.reason ?: "Memeriksa kelayakan...",
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    if (enrollment?.certificateId != null) {
                                        Text(
                                            text = "ID Sertifikat: ${enrollment?.certificateId}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = { viewModel.claimCertificate(context) },
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f),
                                            ) {
                                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Unduh PDF")
                                            }
                                            OutlinedButton(
                                                onClick = {
                                                    val url = NativeIntents.linkedInCertificateUrl(
                                                        certName = cls.title,
                                                        certId = enrollment?.certificateId ?: "",
                                                        issuedAt = enrollment?.certificateIssuedAt ?: System.currentTimeMillis(),
                                                    )
                                                    NativeIntents.openUrl(context, url)
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                            ) {
                                                Text("Ke LinkedIn")
                                            }
                                        }
                                    } else {
                                        Button(
                                            onClick = { viewModel.claimCertificate(context) },
                                            enabled = certEligibility?.eligible == true && !uiState.isClaimingCert,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            if (uiState.isClaimingCert) {
                                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                                            } else {
                                                Text("Klaim E-Sertifikat")
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text("Beri Ulasan Kelas", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Hanya peserta yang hadir minimal 1 sesi yang dapat memberikan ulasan.", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline))
                            Spacer(modifier = Modifier.height(10.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        (1..5).forEach { star ->
                                            IconButton(onClick = { reviewRating = star }) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = "$star Bintang",
                                                    tint = if (star <= reviewRating) Color(0xFFF59E0B) else Color(0xFFD1D5DB),
                                                    modifier = Modifier.size(32.dp),
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = reviewComment,
                                        onValueChange = { reviewComment = it },
                                        placeholder = { Text("Ceritakan pengalaman belajarmu bersama tutor...") },
                                        modifier = Modifier.fillMaxWidth(),
                                        maxLines = 4,
                                        shape = RoundedCornerShape(12.dp),
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            viewModel.submitReview(reviewRating, reviewComment)
                                            reviewComment = ""
                                        },
                                        enabled = reviewComment.isNotBlank() && attendance.isNotEmpty(),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                    ) {
                                        Text("Kirim Ulasan Terverifikasi ⭐")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showTicketModal && enrollment != null && cls != null) {
            AlertDialog(
                onDismissRequest = { showTicketModal = false },
                title = { Text("Tiket QR Presensi", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(cls.title, fontWeight = FontWeight.SemiBold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Text("Tunjukkan QR ini ke tutor saat absensi masuk", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline))
                        Spacer(modifier = Modifier.height(16.dp))

                        val qrBmp = remember(enrollment?.ticketCode) {
                            QrUtils.bitmap(QrUtils.ticketPayload(enrollment?.ticketCode ?: ""), 260)
                        }
                        Image(
                            bitmap = qrBmp.asImageBitmap(),
                            contentDescription = "Kode QR Tiket",
                            modifier = Modifier.size(200.dp),
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Kode Tiket: ${enrollment?.ticketCode}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { showTicketModal = false }) {
                        Text("Tutup")
                    }
                },
            )
        }
    }
}

@Composable
private fun EmptyTabPlaceholder(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.outline))
    }
}

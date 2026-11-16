package com.example.studmate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studmate.domain.ClassStatus
import com.example.studmate.domain.ClassSummary
import com.example.studmate.domain.MaterialType
import com.example.studmate.ui.components.CategoryChip
import com.example.studmate.ui.components.FormatChip
import com.example.studmate.ui.components.VerifiedCampusBadge
import com.example.studmate.ui.viewmodel.OrganizerViewModel
import com.example.studmate.util.formatDate
import com.example.studmate.util.priceLabel
import com.example.studmate.util.rupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizerStudioScreen(
    viewModel: OrganizerViewModel,
    onCreateClass: () -> Unit,
    onEditClass: (String) -> Unit,
    onScanAttendance: (String) -> Unit,
    onOpenWallet: () -> Unit,
    onSwitchToLearner: () -> Unit,
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val myClasses by viewModel.myClasses.collectAsState()
    val wallet by viewModel.walletSummary.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    var announcementClassId by remember { mutableStateOf<String?>(null) }
    var announcementTitle by remember { mutableStateOf("") }
    var announcementBody by remember { mutableStateOf("") }

    var materialClassId by remember { mutableStateOf<String?>(null) }
    var materialTitle by remember { mutableStateOf("") }
    var materialType by remember { mutableStateOf(MaterialType.SLIDE) }
    var materialUri by remember { mutableStateOf("") }

    val filteredClasses = when (selectedTab) {
        0 -> myClasses.filter { it.cls.status == ClassStatus.PUBLISHED }
        1 -> myClasses.filter { it.cls.status == ClassStatus.DRAFT }
        2 -> myClasses.filter { it.cls.status == ClassStatus.COMPLETED }
        else -> myClasses
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tutor Studio", fontWeight = FontWeight.Bold) },
                actions = {
                    OutlinedButton(
                        onClick = onSwitchToLearner,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        Icon(Icons.Default.SwitchAccount, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mode Belajar", fontSize = 12.sp)
                    }
                },
            )
        },
        floatingActionButton = {
            Button(
                onClick = onCreateClass,
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Buat Kelas Baru")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 90.dp),
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentUser?.displayOrganizerName ?: "Tutor Studio",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            )
                            if (currentUser?.isVerified == true) {
                                Spacer(modifier = Modifier.width(6.dp))
                                VerifiedCampusBadge(compact = true)
                            }
                        }
                        Text(
                            text = "${currentUser?.organizerType?.label ?: "Tutor Individu"} • ${currentUser?.university ?: ""}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onPrimaryContainer),
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            MetricColumn("Total Kelas", myClasses.size.toString())
                            MetricColumn("Total Peserta", myClasses.sumOf { it.enrolled }.toString())
                            MetricColumn("Saldo Dompet", rupiah(wallet?.availableToWithdraw ?: 0))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = onOpenWallet,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buka Dompet Escrow & Payout")
                        }
                    }
                }
            }

            item {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Aktif (${myClasses.count { it.cls.status == ClassStatus.PUBLISHED }})") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Draft (${myClasses.count { it.cls.status == ClassStatus.DRAFT }})") })
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Selesai (${myClasses.count { it.cls.status == ClassStatus.COMPLETED }})") })
                }
            }

            if (filteredClasses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Tidak ada kelas pada status ini",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.outline),
                        )
                    }
                }
            } else {
                items(filteredClasses, key = { it.cls.id }) { summary ->
                    OrganizerClassCard(
                        summary = summary,
                        onScan = { onScanAttendance(summary.cls.id) },
                        onBroadcast = {
                            announcementClassId = summary.cls.id
                            announcementTitle = ""
                            announcementBody = ""
                        },
                        onAddMaterial = {
                            materialClassId = summary.cls.id
                            materialTitle = ""
                            materialUri = ""
                        },
                        onEdit = { onEditClass(summary.cls.id) },
                    )
                }
            }
        }

        if (announcementClassId != null) {
            AlertDialog(
                onDismissRequest = { announcementClassId = null },
                title = { Text("Siarkan Pengumuman 📢", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Pengumuman ini otomatis masuk sebagai Push Notification ke seluruh HP peserta.", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline))
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = announcementTitle,
                            onValueChange = { announcementTitle = it },
                            label = { Text("Judul Pengumuman") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = announcementBody,
                            onValueChange = { announcementBody = it },
                            label = { Text("Isi Pesan") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 4,
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.broadcastAnnouncement(announcementClassId!!, announcementTitle, announcementBody)
                            announcementClassId = null
                        },
                        enabled = announcementTitle.isNotBlank() && announcementBody.isNotBlank(),
                    ) {
                        Text("Kirim Notifikasi")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { announcementClassId = null }) { Text("Batal") }
                },
            )
        }

        if (materialClassId != null) {
            AlertDialog(
                onDismissRequest = { materialClassId = null },
                title = { Text("Tambah Materi Kelas 📄", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = materialTitle,
                            onValueChange = { materialTitle = it },
                            label = { Text("Judul Materi / Slide") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = materialUri,
                            onValueChange = { materialUri = it },
                            label = { Text("Tautan URL / Drive / PDF") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.addMaterial(materialClassId!!, materialTitle, materialType, materialUri)
                            materialClassId = null
                        },
                        enabled = materialTitle.isNotBlank() && materialUri.isNotBlank(),
                    ) {
                        Text("Simpan Materi")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { materialClassId = null }) { Text("Batal") }
                },
            )
        }
    }
}

@Composable
private fun MetricColumn(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline))
        Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
    }
}

@Composable
private fun OrganizerClassCard(
    summary: ClassSummary,
    onScan: () -> Unit,
    onBroadcast: () -> Unit,
    onAddMaterial: () -> Unit,
    onEdit: () -> Unit,
) {
    val cls = summary.cls

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FormatChip(format = cls.format)
                    CategoryChip(category = cls.category)
                }
                Text(
                    text = "${summary.enrolled}/${cls.quota} Peserta",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = cls.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = "Biaya: ${priceLabel(cls.price)} • ${summary.sessionCount} Sesi",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onScan,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Presensi QR", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onBroadcast,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Siarkan", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onAddMaterial,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Materi", fontSize = 11.sp)
                }
            }
        }
    }
}

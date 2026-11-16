package com.example.studmate.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studmate.ui.viewmodel.OrganizerViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScannerScreen(
    classId: String,
    viewModel: OrganizerViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val attendees by viewModel.attendeesFor(classId).collectAsState()
    val sessions by viewModel.sessionsFor(classId).collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var selectedSessionId by remember(sessions) {
        mutableStateOf(sessions.firstOrNull()?.id.orEmpty())
    }
    var sessionDropdownExpanded by remember { mutableStateOf(false) }

    var manualTicketInput by remember { mutableStateOf("") }

    val qrScannerLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
    ) { result ->
        val contents = result.contents
        if (!contents.isNullOrBlank() && selectedSessionId.isNotBlank()) {
            viewModel.checkInByTicket(classId, selectedSessionId, contents)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Presensi Peserta & Scan QR", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            Text("Pilih Sesi yang Sedang Berlangsung:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(6.dp))

            val currentSession = sessions.find { it.id == selectedSessionId }

            ExposedDropdownMenuBox(
                expanded = sessionDropdownExpanded,
                onExpandedChange = { sessionDropdownExpanded = it },
            ) {
                OutlinedTextField(
                    value = currentSession?.let { "Pertemuan ${it.orderIndex}: ${it.title}" } ?: "Pilih Sesi",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sessionDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded = sessionDropdownExpanded,
                    onDismissRequest = { sessionDropdownExpanded = false },
                ) {
                    sessions.forEach { sess ->
                        DropdownMenuItem(
                            text = { Text("Pertemuan ${sess.orderIndex}: ${sess.title} ${if (sess.isDone) "✅" else ""}") },
                            onClick = {
                                selectedSessionId = sess.id
                                sessionDropdownExpanded = false
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Button(
                        onClick = {
                            val options = ScanOptions().apply {
                                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                                setPrompt("Arahkan kamera ke QR Code Tiket Peserta")
                                setBeepEnabled(true)
                                setOrientationLocked(false)
                            }
                            qrScannerLauncher.launch(options)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buka Kamera Scanner QR")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Atau Masukkan Kode Tiket Manual:", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = manualTicketInput,
                            onValueChange = { manualTicketInput = it },
                            placeholder = { Text("Contoh: SM-KTLN88") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (manualTicketInput.isNotBlank() && selectedSessionId.isNotBlank()) {
                                    viewModel.checkInByTicket(classId, selectedSessionId, manualTicketInput)
                                    manualTicketInput = ""
                                }
                            },
                            enabled = manualTicketInput.isNotBlank(),
                        ) {
                            Text("Absen")
                        }
                    }

                    if (uiState.checkInResult != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Color(0xFFD1FAE5),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = uiState.checkInResult!!,
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF065F46),
                                ),
                            )
                        }
                    }

                    if (uiState.error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (currentSession != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Tandai Sesi Selesai (Memicu Rilis Escrow):", style = MaterialTheme.typography.labelSmall)
                    Checkbox(
                        checked = currentSession.isDone,
                        onCheckedChange = { viewModel.setSessionDone(classId, currentSession.id, it) },
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp))

            Text(
                text = "Daftar Peserta Terdaftar (${attendees.size})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(attendees, key = { it.enrollment.id }) { item ->
                    val isAttended = selectedSessionId in item.attendedSessionIds
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isAttended) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(item.user?.name ?: "Peserta", fontWeight = FontWeight.Bold)
                                Text("Tiket: ${item.enrollment.ticketCode} • ${item.user?.university ?: ""}", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline))
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isAttended) "Hadir ✅" else "Belum Hadir",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isAttended) Color(0xFF16A34A) else MaterialTheme.colorScheme.outline,
                                        fontWeight = if (isAttended) FontWeight.Bold else FontWeight.Normal,
                                    ),
                                )
                                Checkbox(
                                    checked = isAttended,
                                    onCheckedChange = { checked ->
                                        if (selectedSessionId.isNotBlank()) {
                                            viewModel.toggleAttendance(item.enrollment.id, selectedSessionId, checked)
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.studmate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studmate.domain.Catalog
import com.example.studmate.domain.Category
import com.example.studmate.domain.ClassFormat
import com.example.studmate.domain.ProgramType
import com.example.studmate.ui.components.CategoryChip
import com.example.studmate.ui.components.FormatChip
import com.example.studmate.ui.viewmodel.ClassBuilderViewModel
import com.example.studmate.util.DAY
import com.example.studmate.util.NativeIntents
import com.example.studmate.util.priceLabel
import com.example.studmate.util.rupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassBuilderScreen(
    viewModel: ClassBuilderViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.savedClassId) {
        if (uiState.savedClassId != null) {
            onSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.editingClassId != null) "Edit Kelas" else "Class Builder Wizard", fontWeight = FontWeight.Bold) },
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
                .padding(padding),
        ) {
            StepIndicatorRow(currentStep = uiState.currentStep)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                when (uiState.currentStep) {
                    1 -> Step1BasicInfo(viewModel = viewModel)
                    2 -> Step2Curriculum(viewModel = viewModel)
                    3 -> Step3TicketQuota(viewModel = viewModel)
                    4 -> Step4PreviewPublish(viewModel = viewModel)
                }

                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
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
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    if (uiState.currentStep > 1) {
                        OutlinedButton(onClick = { viewModel.setStep(uiState.currentStep - 1) }) {
                            Text("← Kembali")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (uiState.currentStep < 4) {
                        Button(onClick = { viewModel.setStep(uiState.currentStep + 1) }) {
                            Text("Lanjut →")
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.save(publish = false) },
                                enabled = !uiState.isSaving,
                            ) {
                                Text("Simpan Draft")
                            }
                            Button(
                                onClick = { viewModel.save(publish = true) },
                                enabled = !uiState.isSaving,
                            ) {
                                if (uiState.isSaving) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                                } else {
                                    Text("Publikasikan 🚀")
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
private fun StepIndicatorRow(currentStep: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(vertical = 10.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        listOf("1. Info Dasar", "2. Kurikulum", "3. Tiket", "4. Publikasi").forEachIndexed { idx, label ->
            val stepNum = idx + 1
            val isActive = currentStep == stepNum
            val isPassed = currentStep > stepNum
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color = if (isActive) MaterialTheme.colorScheme.primary else if (isPassed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Step1BasicInfo(viewModel: ClassBuilderViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    var title by remember { mutableStateOf(uiState.title) }
    var topicSummary by remember { mutableStateOf(uiState.topicSummary) }
    var courseGrade by remember { mutableStateOf(uiState.courseGrade) }
    var description by remember { mutableStateOf(uiState.description) }
    var category by remember { mutableStateOf(uiState.category) }
    var programType by remember { mutableStateOf(uiState.programType) }
    var format by remember { mutableStateOf(uiState.format) }
    var university by remember { mutableStateOf(uiState.university) }
    var location by remember { mutableStateOf(uiState.location) }
    var meetingLink by remember { mutableStateOf(uiState.meetingLink) }
    var coverStyle by remember { mutableIntStateOf(uiState.coverStyle) }

    var courseDropdownExpanded by remember { mutableStateOf(false) }

    fun sync() {
        viewModel.updateBasicInfo(
            title = title,
            topicSummary = topicSummary,
            courseGrade = courseGrade,
            description = description,
            category = category,
            programType = programType,
            format = format,
            university = university,
            location = location,
            meetingLink = meetingLink,
            coverStyle = coverStyle,
        )
    }

    Text("Langkah 1: Informasi Mata Kuliah", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
    Text("Sesuai aturan, judul wajib menggunakan Nama Mata Kuliah murni tanpa kode atau embel-embel promosi.", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))

    Spacer(modifier = Modifier.height(14.dp))

    ExposedDropdownMenuBox(
        expanded = courseDropdownExpanded,
        onExpandedChange = { courseDropdownExpanded = it },
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                sync()
            },
            label = { Text("Nama Mata Kuliah (Murni)") },
            placeholder = { Text("Pilih atau ketik, cth: Struktur Data") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = courseDropdownExpanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true,
            isError = uiState.titleWarning != null,
        )
        ExposedDropdownMenu(
            expanded = courseDropdownExpanded,
            onDismissRequest = { courseDropdownExpanded = false },
        ) {
            Catalog.standardCourses.forEach { courseName ->
                DropdownMenuItem(
                    text = { Text(courseName) },
                    onClick = {
                        title = courseName
                        courseDropdownExpanded = false
                        sync()
                    },
                )
            }
        }
    }

    if (uiState.titleWarning != null) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "⚠️ ${uiState.titleWarning}",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelSmall,
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = topicSummary,
        onValueChange = { topicSummary = it; sync() },
        label = { Text("Pokok Bahasan / Topik Spesifik") },
        placeholder = { Text("Contoh: Tree, Graph, & Analisis Kompleksitas") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )

    Spacer(modifier = Modifier.height(12.dp))

    Text("Nilai Mata Kuliah Anda (Bukti Penguasaan):", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf("A", "A-", "B+").forEach { g ->
            FilterChip(
                selected = courseGrade == g,
                onClick = { courseGrade = g; sync() },
                label = { Text("Nilai: $g", fontWeight = if (courseGrade == g) FontWeight.Bold else FontWeight.Normal) },
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = description,
        onValueChange = { description = it; sync() },
        label = { Text("Deskripsi Pendampingan Belajar") },
        placeholder = { Text("Jelaskan metode bimbingan dan materi yang akan dibahas") },
        modifier = Modifier.fillMaxWidth(),
        maxLines = 3,
    )

    Spacer(modifier = Modifier.height(14.dp))

    Text("Pilihan Tema Warna Banner:", style = MaterialTheme.typography.labelMedium)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        (0..5).forEach { styleIdx ->
            val (c1, c2) = NativeIntents.gradientFor(styleIdx)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(c1), Color(c2))))
                    .border(
                        width = if (coverStyle == styleIdx) 3.dp else 0.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = CircleShape,
                    )
                    .clickable {
                        coverStyle = styleIdx
                        sync()
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (coverStyle == styleIdx) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    Text("Format Kelas:", style = MaterialTheme.typography.labelMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ClassFormat.entries.forEach { f ->
            FilterChip(
                selected = format == f,
                onClick = { format = f; sync() },
                label = { Text(f.label) },
            )
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    if (format == ClassFormat.ONLINE || format == ClassFormat.HYBRID) {
        OutlinedTextField(
            value = meetingLink,
            onValueChange = { meetingLink = it; sync() },
            label = { Text("Tautan Video Conference (Zoom / Meet)") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(10.dp))
    }

    if (format == ClassFormat.OFFLINE || format == ClassFormat.HYBRID) {
        OutlinedTextField(
            value = location,
            onValueChange = { location = it; sync() },
            label = { Text("Lokasi Gedung / Ruang Kampus") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(10.dp))
    }

    Text("Kategori:", style = MaterialTheme.typography.labelMedium)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Category.entries.take(3).forEach { c ->
            FilterChip(
                selected = category == c,
                onClick = { category = c; sync() },
                label = { Text(c.label, fontSize = 11.sp) },
            )
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Category.entries.drop(3).forEach { c ->
            FilterChip(
                selected = category == c,
                onClick = { category = c; sync() },
                label = { Text(c.label, fontSize = 11.sp) },
            )
        }
    }
}

@Composable
private fun Step2Curriculum(viewModel: ClassBuilderViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var sessionTitle by remember { mutableStateOf("") }
    var sessionDesc by remember { mutableStateOf("") }
    var sessionDuration by remember { mutableIntStateOf(90) }

    Text("Langkah 2: Kurikulum & Jadwal Sesi", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
    Text("Rancang materi per pertemuan yang akan diajarkan", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline))

    Spacer(modifier = Modifier.height(14.dp))

    uiState.sessions.forEachIndexed { idx, sess ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(10.dp),
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Pertemuan ${idx + 1}: ${sess.title}", fontWeight = FontWeight.Bold)
                    Text("Durasi: ${sess.durationMinutes} menit", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline))
                }
                IconButton(onClick = { viewModel.removeSession(idx) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    Button(
        onClick = { showAddDialog = true },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
    ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Spacer(modifier = Modifier.width(6.dp))
        Text("Tambah Pertemuan")
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Tambah Pertemuan Baru", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = sessionTitle,
                        onValueChange = { sessionTitle = it },
                        label = { Text("Judul Sesi") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = sessionDesc,
                        onValueChange = { sessionDesc = it },
                        label = { Text("Deskripsi Materi Sesi") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val now = System.currentTimeMillis()
                        viewModel.addSession(
                            title = sessionTitle,
                            description = sessionDesc,
                            startAt = now + (uiState.sessions.size + 1) * 2 * DAY,
                            durationMinutes = sessionDuration,
                        )
                        sessionTitle = ""
                        sessionDesc = ""
                        showAddDialog = false
                    },
                    enabled = sessionTitle.isNotBlank(),
                ) {
                    Text("Simpan Sesi")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddDialog = false }) { Text("Batal") }
            },
        )
    }
}

@Composable
private fun Step3TicketQuota(viewModel: ClassBuilderViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    var isFree by remember { mutableStateOf(uiState.isFree) }
    var price by remember { mutableLongStateOf(uiState.price) }
    var hasEarlyBird by remember { mutableStateOf(uiState.hasEarlyBird) }
    var earlyBirdPrice by remember { mutableLongStateOf(uiState.earlyBirdPrice) }
    var earlyBirdDays by remember { mutableIntStateOf(uiState.earlyBirdDays) }
    var quota by remember { mutableIntStateOf(uiState.quota) }
    var hasCertificate by remember { mutableStateOf(uiState.hasCertificate) }
    var minAttendancePercent by remember { mutableIntStateOf(uiState.minAttendancePercent) }

    fun sync() {
        viewModel.updateTicketInfo(isFree, price, hasEarlyBird, earlyBirdPrice, earlyBirdDays, quota, hasCertificate, minAttendancePercent)
    }

    Text("Langkah 3: Tiket, Kuota & Sertifikat", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
    Spacer(modifier = Modifier.height(14.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text("Kelas Gratis", fontWeight = FontWeight.Bold)
            Text("Dapat diikuti mahasiswa tanpa biaya", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline))
        }
        Switch(checked = isFree, onCheckedChange = { isFree = it; sync() })
    }

    if (!isFree) {
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = price.toString(),
            onValueChange = { price = it.toLongOrNull() ?: 0L; sync() },
            label = { Text("Harga Tiket Normal (Rp)") },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Sediakan Harga Early Bird", fontWeight = FontWeight.SemiBold)
            Switch(checked = hasEarlyBird, onCheckedChange = { hasEarlyBird = it; sync() })
        }

        if (hasEarlyBird) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = earlyBirdPrice.toString(),
                onValueChange = { earlyBirdPrice = it.toLongOrNull() ?: 0L; sync() },
                label = { Text("Harga Early Bird (Rp)") },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text("Batas Kuota Peserta: $quota orang", fontWeight = FontWeight.SemiBold)
    Slider(
        value = quota.toFloat(),
        onValueChange = { quota = it.toInt(); sync() },
        valueRange = 5f..100f,
    )

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text("Terbitkan E-Sertifikat", fontWeight = FontWeight.Bold)
            Text("Diterbitkan otomatis pasca-kelas", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline))
        }
        Switch(checked = hasCertificate, onCheckedChange = { hasCertificate = it; sync() })
    }
}

@Composable
private fun Step4PreviewPublish(viewModel: ClassBuilderViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val (c1, c2) = NativeIntents.gradientFor(uiState.coverStyle)

    Text("Langkah 4: Pratinjau & Publikasi", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
    Text("Periksa ringkasan kartu kelas sebelum dipublikasikan", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline))

    Spacer(modifier = Modifier.height(14.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(Brush.horizontalGradient(listOf(Color(c1), Color(c2))))
                    .padding(12.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FormatChip(format = uiState.format)
                    CategoryChip(category = uiState.category)
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Text(uiState.title.ifBlank { "Judul Kelas" }, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(4.dp))
                Text(uiState.description.ifBlank { "Deskripsi kelas..." }, maxLines = 2, style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = priceLabel(if (uiState.isFree) 0 else uiState.price),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                    ),
                )
                Text("Total ${uiState.sessions.size} pertemuan • Kuota ${uiState.quota} peserta", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline))
            }
        }
    }
}

package com.example.studmate.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studmate.data.SeedData
import com.example.studmate.domain.ThemeMode
import com.example.studmate.ui.components.VerifiedCampusBadge
import com.example.studmate.ui.viewmodel.AuthViewModel
import com.example.studmate.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    authViewModel: AuthViewModel,
    onNavigateNotifications: () -> Unit,
) {
    val context = LocalContext.current
    val currentUser by profileViewModel.currentUser.collectAsState()
    val isOrganizerMode by profileViewModel.isOrganizerMode.collectAsState()
    val settings by profileViewModel.settings.collectAsState()
    val unreadCount by profileViewModel.unreadCount.collectAsState()
    val authUiState by authViewModel.uiState.collectAsState()

    var showVerificationDialog by remember { mutableStateOf(false) }
    var campusEmailInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Akun & Profil", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateNotifications) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge { Text(unreadCount.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifikasi")
                        }
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            // 1. Info Pengguna Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(56.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = (currentUser?.name ?: "U").take(2).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentUser?.name ?: "Mahasiswa",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                )
                                if (currentUser?.isVerified == true) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    VerifiedCampusBadge(compact = true)
                                }
                            }
                            Text(
                                text = currentUser?.email ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline),
                            )
                            Text(
                                text = "${currentUser?.major} (Semester ${currentUser?.semester ?: 4})",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium),
                            )
                            Text(
                                text = "${currentUser?.faculty?.ifBlank { "" }?.let { "$it • " } ?: ""}${currentUser?.university}",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline),
                            )
                        }
                    }

                    if (currentUser?.bio?.isNotBlank() == true) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(currentUser!!.bio, style = MaterialTheme.typography.bodySmall)
                    }

                    // Status Verifikasi Kampus
                    Spacer(modifier = Modifier.height(12.dp))
                    if (currentUser?.isVerified == true) {
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Akun Terverifikasi Resmi Kampus (${currentUser?.campusEmail ?: "KTM"})",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF1D4ED8), fontWeight = FontWeight.Bold),
                                )
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { showVerificationDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Verifikasi Email Kampus (.ac.id) / KTM")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Dual-Role Switcher Card (One Account, Dual-Role System)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isOrganizerMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isOrganizerMode) "Mode Mengajar (Tutor Studio)" else "Mode Belajar (Learner)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            )
                            Text(
                                text = if (isOrganizerMode) "Kamu sedang dalam dasbor tutor & pengelola kelas" else "Beralih untuk membuka kelas dan mengelola pendapatan",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            )
                        }
                    }

                    Switch(
                        checked = isOrganizerMode,
                        onCheckedChange = { profileViewModel.toggleOrganizerMode() },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Pengaturan Tema Aplikasi
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Pengaturan Tampilan", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tema Aplikasi", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = settings.themeMode == mode,
                                onClick = { profileViewModel.setThemeMode(mode) },
                                label = { Text(mode.label) },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Dynamic Color (Android 12+)", style = MaterialTheme.typography.bodyMedium)
                        }
                        Switch(
                            checked = settings.dynamicColor,
                            onCheckedChange = { profileViewModel.setDynamicColor(it) },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Quick Demo Switcher (Uji Coba Cepat Persona)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("🧪 Ganti Akun Demo Pengujian:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        FilterChip(
                            selected = currentUser?.id == SeedData.DEMO_LEARNER_ID,
                            onClick = { authViewModel.switchDemoUser(SeedData.DEMO_LEARNER_ID) },
                            label = { Text("Rian (UI)", fontSize = 11.sp) },
                        )
                        FilterChip(
                            selected = currentUser?.id == SeedData.DEMO_TUTOR_RAKA_ID,
                            onClick = { authViewModel.switchDemoUser(SeedData.DEMO_TUTOR_RAKA_ID) },
                            label = { Text("Raka (UI)", fontSize = 11.sp) },
                        )
                        FilterChip(
                            selected = currentUser?.id == SeedData.DEMO_TUTOR_NADYA_ID,
                            onClick = { authViewModel.switchDemoUser(SeedData.DEMO_TUTOR_NADYA_ID) },
                            label = { Text("Nadya (ITB)", fontSize = 11.sp) },
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        FilterChip(
                            selected = currentUser?.id == SeedData.DEMO_TUTOR_DIMAS_ID,
                            onClick = { authViewModel.switchDemoUser(SeedData.DEMO_TUTOR_DIMAS_ID) },
                            label = { Text("Dimas (UB)", fontSize = 11.sp) },
                        )
                        FilterChip(
                            selected = currentUser?.id == SeedData.DEMO_TUTOR_SARAH_ID,
                            onClick = { authViewModel.switchDemoUser(SeedData.DEMO_TUTOR_SARAH_ID) },
                            label = { Text("Sarah (UGM)", fontSize = 11.sp) },
                        )
                        FilterChip(
                            selected = currentUser?.id == SeedData.DEMO_ORG_UI_ID,
                            onClick = { authViewModel.switchDemoUser(SeedData.DEMO_ORG_UI_ID) },
                            label = { Text("BEM UI", fontSize = 11.sp) },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tombol Logout
            OutlinedButton(
                onClick = { profileViewModel.logout() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Keluar dari Akun")
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Dialog Verifikasi Kampus (.ac.id / KTM)
        if (showVerificationDialog) {
            AlertDialog(
                onDismissRequest = { showVerificationDialog = false },
                title = { Text("Verifikasi Kampus Resmi", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            "Masukkan email kampus berakhiran .ac.id untuk mendapatkan lencana Verified Campus Badge.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline),
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        if (!authUiState.otpSent) {
                            OutlinedTextField(
                                value = campusEmailInput,
                                onValueChange = { campusEmailInput = it },
                                label = { Text("Email Kampus (.ac.id)") },
                                placeholder = { Text("nama@ui.ac.id") },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        } else {
                            OutlinedTextField(
                                value = otpInput,
                                onValueChange = { otpInput = it },
                                label = { Text("Kode OTP 6-Digit") },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        if (authUiState.infoMessage != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(authUiState.infoMessage!!, color = Color(0xFF16A34A), style = MaterialTheme.typography.labelSmall)
                        }
                        if (authUiState.error != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(authUiState.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(10.dp))

                        // Opsi simulasi unggah foto KTM
                        OutlinedButton(
                            onClick = {
                                authViewModel.submitKtm("content://media/ktm_simulated.jpg")
                                showVerificationDialog = false
                                Toast.makeText(context, "KTM berhasil diverifikasi!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Simulasi Unggah Foto KTM")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (!authUiState.otpSent) {
                                authViewModel.requestCampusOtp(campusEmailInput)
                            } else {
                                authViewModel.verifyCampusOtp(otpInput)
                                showVerificationDialog = false
                            }
                        },
                        enabled = if (!authUiState.otpSent) campusEmailInput.endsWith(".ac.id") else otpInput.length == 6,
                    ) {
                        Text(if (!authUiState.otpSent) "Kirim Kode OTP" else "Verifikasi OTP")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showVerificationDialog = false }) { Text("Batal") }
                },
            )
        }
    }
}

package com.example.studmate.ui.screens

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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studmate.data.SeedData
import com.example.studmate.ui.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onSuccess: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Masuk, 1: Daftar
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Hero Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                        )
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(20.dp),
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "StudMate",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                    ),
                )
                Text(
                    text = "Centralized Campus Peer-Learning Marketplace",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                    ),
                )
            }
        }

        // Form Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Masuk", fontWeight = FontWeight.Bold) },
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Daftar Akun", fontWeight = FontWeight.Bold) },
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (selectedTab == 1) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Lengkap") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Mahasiswa / Umum") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Kata Sandi") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (selectedTab == 0) viewModel.login(email, password)
                        else viewModel.register(name, email, password)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !uiState.isLoading,
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Text(if (selectedTab == 0) "Masuk" else "Daftar Sekarang")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Simulasi Google Sign-In
                OutlinedButton(
                    onClick = { viewModel.signInGoogleSimulated() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Masuk Cepat via Google (Simulasi)")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Demo User Switcher Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🧪 Akun Demo Siap Pakai (1-Klik):",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
                Spacer(modifier = Modifier.height(10.dp))

                DemoUserPill(
                    name = "Rian Pratama",
                    role = "Learner Mahasiswa (UI)",
                    onClick = { viewModel.switchDemoUser(SeedData.DEMO_LEARNER_ID) },
                )
                Spacer(modifier = Modifier.height(6.dp))
                DemoUserPill(
                    name = "Raka Pratama",
                    role = "Tutor Struktur Data (UI • Verified)",
                    onClick = { viewModel.switchDemoUser(SeedData.DEMO_TUTOR_RAKA_ID) },
                )
                Spacer(modifier = Modifier.height(6.dp))
                DemoUserPill(
                    name = "Nadya Putri",
                    role = "Tutor Kalkulus Dasar (ITB • Verified)",
                    onClick = { viewModel.switchDemoUser(SeedData.DEMO_TUTOR_NADYA_ID) },
                )
                Spacer(modifier = Modifier.height(6.dp))
                DemoUserPill(
                    name = "Dimas Arya",
                    role = "Tutor Struktur Data & Akuntansi (UB • Verified)",
                    onClick = { viewModel.switchDemoUser(SeedData.DEMO_TUTOR_DIMAS_ID) },
                )
                Spacer(modifier = Modifier.height(6.dp))
                DemoUserPill(
                    name = "BEM Fasilkom UI",
                    role = "Organisasi Kampus (Verified)",
                    onClick = { viewModel.switchDemoUser(SeedData.DEMO_ORG_UI_ID) },
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DemoUserPill(
    name: String,
    role: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                Text(role, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary))
            }
            Text("Pilih →", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        }
    }
}

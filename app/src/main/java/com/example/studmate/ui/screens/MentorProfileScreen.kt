package com.example.studmate.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studmate.data.repo.CatalogRepository
import com.example.studmate.domain.MentorStats
import com.example.studmate.ui.components.ClassCard
import com.example.studmate.ui.components.VerifiedCampusBadge
import com.example.studmate.util.NativeIntents

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorProfileScreen(
    mentorId: String,
    catalogRepo: CatalogRepository,
    onBack: () -> Unit,
    onClassClick: (String) -> Unit,
) {
    val context = LocalContext.current
    val mentor by catalogRepo.mentor(mentorId).collectAsState(initial = null)
    val classes by catalogRepo.classesBy(mentorId).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil Pengajar", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
            )
        }
    ) { padding ->
        if (mentor == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val user = mentor!!.user

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Header Profil Tutor
                item {
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
                                    modifier = Modifier.size(60.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = user.name.take(2).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = user.displayOrganizerName,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        )
                                        if (user.isVerified) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            VerifiedCampusBadge(compact = true)
                                        }
                                    }
                                    Text(
                                        text = "${user.major} • ${user.university}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline),
                                    )
                                    Text(
                                        text = user.organizerType.label,
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary),
                                    )
                                }
                            }

                            if (user.bio.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(user.bio, style = MaterialTheme.typography.bodyMedium)
                            }

                            if (user.portfolioUrl.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier
                                        .clickable { NativeIntents.openUrl(context, user.portfolioUrl) },
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = user.portfolioUrl,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold,
                                        ),
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Statistik Mengajar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${mentor!!.classCount}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("Kelas Dibuka", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${mentor!!.studentCount}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("Mahasiswa", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(String.format("%.1f", mentor!!.avgRating), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    }
                                    Text("Rating Rata-rata", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline))
                                }
                            }
                        }
                    }
                }

                // Daftar Kelas yang Diajar
                item {
                    Text(
                        text = "Kelas yang Diajarkan (${classes.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    )
                }

                items(classes, key = { it.cls.id }) { clsSummary ->
                    ClassCard(
                        summary = clsSummary,
                        onClick = { onClassClick(clsSummary.cls.id) },
                    )
                }
            }
        }
    }
}

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
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studmate.domain.EnrollmentStatus
import com.example.studmate.domain.LearningItem
import com.example.studmate.ui.components.CategoryChip
import com.example.studmate.ui.components.FormatChip
import com.example.studmate.ui.viewmodel.LearningTab
import com.example.studmate.ui.viewmodel.MyLearningViewModel
import com.example.studmate.util.formatDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyLearningScreen(
    viewModel: MyLearningViewModel,
    onOpenHub: (String) -> Unit,
    onExplore: () -> Unit,
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val filteredItems by viewModel.filteredItems.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelasku", fontWeight = FontWeight.Bold) },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                LearningTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = { Text(tab.label, fontWeight = FontWeight.SemiBold) },
                    )
                }
            }

            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.outline,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Belum ada kelas di tab ini",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Jelajahi kelas matkul, bootcamp, dan workshop di StudMate.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onExplore) {
                            Text("Jelajahi Kelas")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(filteredItems, key = { it.enrollment.id }) { item ->
                        LearningCard(
                            item = item,
                            onClick = { onOpenHub(item.summary.cls.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LearningCard(
    item: LearningItem,
    onClick: () -> Unit,
) {
    val cls = item.summary.cls
    val enr = item.enrollment

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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

                Surface(
                    color = when (enr.status) {
                        EnrollmentStatus.ACTIVE -> Color(0xFFD1FAE5)
                        EnrollmentStatus.COMPLETED -> Color(0xFFE0E7FF)
                        else -> Color(0xFFFEF3C7)
                    },
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text(
                        text = when (enr.status) {
                            EnrollmentStatus.ACTIVE -> "Aktif"
                            EnrollmentStatus.COMPLETED -> "Selesai"
                            EnrollmentStatus.PENDING_PAYMENT -> "Menunggu Bayar"
                            else -> "Batal"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = when (enr.status) {
                                EnrollmentStatus.ACTIVE -> Color(0xFF065F46)
                                EnrollmentStatus.COMPLETED -> Color(0xFF3730A3)
                                else -> Color(0xFF92400E)
                            },
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = cls.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Tutor: ${item.summary.organizer?.displayOrganizerName ?: "StudMate Tutor"} • ${cls.university}",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (item.nextSessionAt != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sesi Berikutnya: ${formatDateTime(item.nextSessionAt)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }
            } else {
                Text(
                    text = "Total Kehadiran: ${item.attended}/${item.summary.sessionCount} sesi",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Tiket: ${enr.ticketCode}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline,
                    ),
                )
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("Class Hub →", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

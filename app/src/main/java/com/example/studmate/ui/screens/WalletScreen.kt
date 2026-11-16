package com.example.studmate.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.studmate.ui.viewmodel.WalletViewModel
import com.example.studmate.util.formatDate
import com.example.studmate.util.priceLabel
import com.example.studmate.util.rupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: WalletViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val wallet by viewModel.walletSummary.collectAsState()
    val payouts by viewModel.payouts.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val destinations = listOf("BCA", "Mandiri", "BNI", "GoPay", "DANA", "ShopeePay")
    var selectedDest by remember { mutableStateOf(destinations.first()) }
    var destDropdownExpanded by remember { mutableStateOf(false) }

    var accountNum by remember { mutableStateOf("") }
    var withdrawAmount by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dompet Escrow & Payout", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Saldo Siap Dicairkan", style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.8f)))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = rupiah(wallet?.availableToWithdraw ?: 0),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color.White),
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = Color.White.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFFEF08A), modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tertahan Escrow", style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.9f)))
                            }
                            Text(
                                rupiah(wallet?.escrowHeld ?: 0),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color.White),
                            )
                        }

                        Column {
                            Text("Total Dicairkan", style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.9f)))
                            Text(
                                rupiah(wallet?.paidOut ?: 0),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color.White),
                            )
                        }

                        Column {
                            Text("Biaya Layanan (5%)", style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.9f)))
                            Text(
                                rupiah(wallet?.platformFee ?: 0),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color.White),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tarik Saldo ke Rekening / E-Wallet", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(10.dp))

                    ExposedDropdownMenuBox(
                        expanded = destDropdownExpanded,
                        onExpandedChange = { destDropdownExpanded = it },
                    ) {
                        OutlinedTextField(
                            value = selectedDest,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tujuan Bank / E-Wallet") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = destDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                        )
                        ExposedDropdownMenu(
                            expanded = destDropdownExpanded,
                            onDismissRequest = { destDropdownExpanded = false },
                        ) {
                            destinations.forEach { dest ->
                                DropdownMenuItem(
                                    text = { Text(dest) },
                                    onClick = {
                                        selectedDest = dest
                                        destDropdownExpanded = false
                                    },
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = accountNum,
                        onValueChange = { accountNum = it },
                        label = { Text("Nomor Rekening / No. HP E-Wallet") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = withdrawAmount,
                        onValueChange = { withdrawAmount = it },
                        label = { Text("Nominal Penarikan (Rp)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    if (uiState.error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(uiState.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    if (uiState.successMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(uiState.successMessage!!, color = Color(0xFF16A34A), style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val amt = withdrawAmount.toLongOrNull() ?: 0L
                            viewModel.withdraw(amt, selectedDest, accountNum)
                            accountNum = ""
                            withdrawAmount = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !uiState.isWithdrawing,
                    ) {
                        if (uiState.isWithdrawing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                        } else {
                            Text("Tarik Saldo Sekarang (Simulasi)")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Riwayat Pencairan Dana", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))

            if (payouts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Belum ada riwayat penarikan saldo", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline))
                }
            } else {
                payouts.forEach { p ->
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("${p.destination} (${p.accountNumber})", fontWeight = FontWeight.Bold)
                                    Text(formatDate(p.createdAt), style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline))
                                }
                            }
                            Text(
                                text = rupiah(p.amount),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFF16A34A)),
                            )
                        }
                    }
                }
            }
        }
    }
}

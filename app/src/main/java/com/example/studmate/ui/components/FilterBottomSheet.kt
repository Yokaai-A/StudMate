package com.example.studmate.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.studmate.domain.Category
import com.example.studmate.domain.ClassFormat
import com.example.studmate.domain.FilterState
import com.example.studmate.domain.PriceFilter
import com.example.studmate.domain.ProgramType
import com.example.studmate.domain.SortOption
import com.example.studmate.util.rupiah

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    filter: FilterState,
    onApply: (FilterState) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var state by remember { mutableStateOf(filter) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Filter Pencarian",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle("Urutkan Berdasarkan")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SortOption.entries.forEach { opt ->
                    FilterChip(
                        selected = state.sort == opt,
                        onClick = { state = state.copy(sort = opt) },
                        label = { Text(opt.label) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle("Format Kelas")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ClassFormat.entries.forEach { f ->
                    val selected = f in state.formats
                    FilterChip(
                        selected = selected,
                        onClick = {
                            state = state.copy(
                                formats = if (selected) state.formats - f else state.formats + f
                            )
                        },
                        label = { Text(f.label) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle("Kategori Bidang")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Category.entries.forEach { c ->
                    val selected = c in state.categories
                    FilterChip(
                        selected = selected,
                        onClick = {
                            state = state.copy(
                                categories = if (selected) state.categories - c else state.categories + c
                            )
                        },
                        label = { Text(c.label) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle("Tipe Program")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ProgramType.entries.forEach { p ->
                    val selected = p in state.types
                    FilterChip(
                        selected = selected,
                        onClick = {
                            state = state.copy(
                                types = if (selected) state.types - p else state.types + p
                            )
                        },
                        label = { Text(p.label) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle("Biaya & Harga")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PriceFilter.entries.forEach { p ->
                    FilterChip(
                        selected = state.price == p,
                        onClick = { state = state.copy(price = p) },
                        label = { Text(p.label) },
                    )
                }
            }

            if (state.price != PriceFilter.FREE) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Maksimal Harga:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        rupiah(state.maxPrice),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    )
                }
                Slider(
                    value = state.maxPrice.toFloat(),
                    onValueChange = { state = state.copy(maxPrice = (it / 10_000).toLong() * 10_000) },
                    valueRange = 0f..FilterState.MAX_PRICE.toFloat(),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = { state = FilterState() },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Reset")
                }
                Button(
                    onClick = {
                        onApply(state)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1.5f),
                ) {
                    Text("Terapkan (${state.activeCount})")
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

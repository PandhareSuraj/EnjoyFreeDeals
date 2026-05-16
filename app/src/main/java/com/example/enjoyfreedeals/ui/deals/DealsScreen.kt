package com.example.enjoyfreedeals.ui.deals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.enjoyfreedeals.data.mock.MockData
import com.example.enjoyfreedeals.data.model.Deal
import com.example.enjoyfreedeals.data.model.DealSort

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealsScreen(
    deals: List<Deal>,
    savedDealIds: List<String>,
    selectedStore: String,
    selectedSort: DealSort,
    searchQuery: String,
    onStore: (String) -> Unit,
    onSort: (DealSort) -> Unit,
    onSearch: (String) -> Unit,
    onRefresh: () -> Unit,
    onView: (Deal) -> Unit,
    onSave: (Deal) -> Unit,
    onShare: (Deal) -> Unit
) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("All Deals", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text("Filter stores and sort the best live offers.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f))
        }
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearch,
                modifier = Modifier.fillParentMaxWidth(),
                label = { Text("Search deals, stores, coupons...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )
        }
        item { OutlinedButton(onClick = onRefresh, modifier = Modifier.fillParentMaxWidth()) { Text("Pull to refresh live deals") } }
        item { FilterChipRow(listOf("All", "Amazon", "Flipkart", "Meesho", "Myntra", "Ajio", "Croma", "Nykaa", "Free Deals", "Lowest Price", "Hot Deals"), selectedStore, onStore) }
        item {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = selectedSort.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Sort by") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor().fillParentMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DealSort.entries.forEach { sort ->
                        DropdownMenuItem(text = { Text(sort.label) }, onClick = { onSort(sort); expanded = false })
                    }
                }
            }
        }
        if (deals.isEmpty()) {
            item { EmptyState("No deals found for your current filters.") }
        } else {
            items(deals.size) { index ->
                val deal = deals[index]
                DealCard(deal, deal.dealId in savedDealIds, onView, onSave, onShare)
            }
        }
    }
}

package com.example.enjoyfreedeals.ui.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.enjoyfreedeals.data.model.Deal
import com.example.enjoyfreedeals.ui.deals.DealCard
import com.example.enjoyfreedeals.ui.deals.EmptyState

@Composable
fun SavedDealsScreen(
    deals: List<Deal>,
    savedDealIds: List<String>,
    onView: (Deal) -> Unit,
    onRemove: (Deal) -> Unit,
    onShare: (Deal) -> Unit
) {
    val savedDeals = deals.filter { it.dealId in savedDealIds }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("Saved Deals", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text("Your bookmarked offers are ready when you are.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f))
        }
        if (savedDeals.isEmpty()) {
            item { EmptyState("You have not saved any deals yet.") }
        } else {
            items(savedDeals.size) { index ->
                val deal = savedDeals[index]
                DealCard(deal, true, onView, onRemove, onShare)
            }
        }
    }
}

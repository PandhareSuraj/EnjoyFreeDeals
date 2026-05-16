package com.example.enjoyfreedeals.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.enjoyfreedeals.data.model.Deal
import com.example.enjoyfreedeals.ui.deals.DealCard
import com.example.enjoyfreedeals.ui.deals.ShimmerDealPlaceholder
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    deals: List<Deal>,
    savedDealIds: List<String>,
    unreadCount: Int,
    searchQuery: String,
    selectedStore: String,
    loading: Boolean,
    onSearch: (String) -> Unit,
    onStore: (String) -> Unit,
    onRefresh: () -> Unit,
    onNotifications: () -> Unit,
    onView: (Deal) -> Unit,
    onSave: (Deal) -> Unit,
    onShare: (Deal) -> Unit
) {
    LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Hello, Deal Hunter", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                        Text("Live prices, lowest-price alerts, coupons and cashback.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                    IconButton(onClick = onNotifications) {
                        BadgedBox(badge = { if (unreadCount > 0) Badge { Text(unreadCount.toString()) } }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }
                }
            }            
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearch,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search deals, stores, coupons...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) { Text("Pull to refresh deals") }
        }
        item { StoreChips(selectedStore, onStore) }
        if (loading) {
            items(3) { ShimmerDealPlaceholder() }
            return@LazyColumn
        }
        item { BannerSlider(deals.filter { it.isHotDeal }.ifEmpty { deals }.take(4)) }
        dealSection("Live Deals", deals, savedDealIds, onView, onSave, onShare)
        dealSection("Lowest Price Today", deals.filter { it.displayCurrentPrice <= it.displayLowestPrice }, savedDealIds, onView, onSave, onShare)
        dealSection("Recently Updated", deals.sortedByDescending { it.updatedAtMillis }.take(4), savedDealIds, onView, onSave, onShare)
        dealSection("Hot Affiliate Deals", deals.filter { it.isHotDeal }, savedDealIds, onView, onSave, onShare)
        dealSection("Free Deals", deals.filter { it.isFreeDeal }, savedDealIds, onView, onSave, onShare)
    }
}

@Composable
private fun StoreChips(selected: String, onSelected: (String) -> Unit) {
    val stores = listOf("All", "Amazon", "Flipkart", "Meesho", "Myntra", "Snapdeal", "Ajio", "TataCliq", "Nykaa", "Croma", "JioMart", "BigBasket")
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        stores.forEach { store ->
            if (store == selected) ElevatedAssistChip(onClick = { onSelected(store) }, label = { Text(store) })
            else AssistChip(onClick = { onSelected(store) }, label = { Text(store) })
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.dealSection(
    title: String,
    deals: List<Deal>,
    savedDealIds: List<String>,
    onView: (Deal) -> Unit,
    onSave: (Deal) -> Unit,
    onShare: (Deal) -> Unit
) {
    if (deals.isNotEmpty()) {
        item { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black) }
        items(deals.size) { index ->
            val deal = deals[index]
            DealCard(deal, deal.dealId in savedDealIds, onView, onSave, onShare)
        }
    }
}

@Composable
private fun BannerSlider(deals: List<Deal>) {
    var index by remember { mutableIntStateOf(0) }
    LaunchedEffect(deals.size) {
        while (deals.isNotEmpty()) {
            delay(2_300)
            index = (index + 1) % deals.size
        }
    }
    val deal = deals.getOrNull(index)
    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        AnimatedContent(deal, label = "banner") { current ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.CenterStart
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("Featured Deal", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                    Text(current?.title ?: "More deals arriving soon", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Text("${current?.discountPercent ?: 0}% OFF", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

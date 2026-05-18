package com.example.enjoyfreedeals.ui.deals

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.enjoyfreedeals.R
import com.example.enjoyfreedeals.data.model.Deal
import com.example.enjoyfreedeals.theme.DealGreen
import com.example.enjoyfreedeals.theme.DealRed
import com.example.enjoyfreedeals.theme.DealYellow
import kotlin.math.max

@Composable
fun DealCard(
    deal: Deal,
    isSaved: Boolean,
    onView: (Deal) -> Unit,
    onSave: (Deal) -> Unit,
    onShare: (Deal) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = deal.productImage,
                    contentDescription = deal.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp),
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.ic_enjoy_free_deals_logo),
                    placeholder = painterResource(R.drawable.ic_enjoy_free_deals_logo)
                )
                FlowRow(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (deal.isHotDeal) Badge("Hot Deal", DealRed)
                    if (deal.isFreeDeal) Badge("Free Deal", DealGreen)
                    if (deal.isPreviewDeal) Badge("Preview", MaterialTheme.colorScheme.primary)
                    if (deal.expiryDate - System.currentTimeMillis() < 86_400_000L) Badge("Limited Time", DealYellow, Color(0xFF1E1E1E))
                    if (deal.isVerified) Badge("Verified", DealGreen)
                    if (deal.displayCurrentPrice <= deal.displayLowestPrice) Badge("Lowest Price Now", DealYellow, Color(0xFF1E1E1E))
                    else if (deal.isNearLowestPrice) Badge("Near Lowest Price", DealGreen)
                }
            }
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (deal.storeLogo.isNotBlank()) {
                            AsyncImage(
                                model = deal.storeLogo,
                                contentDescription = "${deal.storeName} logo",
                                modifier = Modifier.size(34.dp),
                                contentScale = ContentScale.Crop,
                                error = painterResource(R.drawable.ic_enjoy_free_deals_logo)
                            )
                        } else {
                            Text(deal.storeName.take(1), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(deal.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(deal.storeName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                    }
                }
                Text(deal.description, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (deal.couponCode.isNotBlank()) InfoPill("Coupon: ${deal.couponCode}")
                    if (deal.cashbackText.isNotBlank()) InfoPill(deal.cashbackText)
                    InfoPill("Clicks: ${deal.clickCount}")
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("Live Price", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f))
                        Spacer(Modifier.width(8.dp))
                        Text("Rs ${deal.displayCurrentPrice.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("MRP Rs ${deal.originalPrice.toInt()}", textDecoration = TextDecoration.LineThrough, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                        Spacer(Modifier.weight(1f))
                        Text("${deal.discountPercent}% OFF", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                    }
                    Text("Lowest Price: Rs ${deal.displayLowestPrice.toInt()}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = if (deal.priceNeedsRefresh) "Price not recently verified" else "Last checked: ${formatLastChecked(deal.lastPriceCheckedAt)}",
                        color = if (deal.priceNeedsRefresh) DealRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (deal.priceNeedsRefresh) FontWeight.Bold else FontWeight.Normal
                    )
                }
                PriceHistoryChart(deal.priceHistory.ifEmpty { listOf(deal.originalPrice, deal.displayCurrentPrice) })
                DealCountdown(deal.expiryDate)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = { onView(deal) }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("View Offer")
                    }
                    IconButton(onClick = { onSave(deal) }) {
                        Icon(Icons.Default.Bookmark, contentDescription = "Save deal", tint = if (isSaved) DealYellow else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                    IconButton(onClick = { onShare(deal) }) {
                        Icon(Icons.Default.IosShare, contentDescription = "Share deal")
                    }
                }
            }
        }
    }
}

private fun formatLastChecked(value: String): String {
    return value.replace('T', ' ')
        .replace("Z", "")
        .take(16)
        .ifBlank { "unknown" }
}

@Composable
private fun PriceHistoryChart(values: List<Double>) {
    val lineColor = MaterialTheme.colorScheme.secondary
    val fillColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f)
    val points = values.filter { it >= 0.0 }
    if (points.size < 2) return
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
            .padding(6.dp)
    ) {
        val maxValue = points.maxOrNull()?.takeIf { it > 0.0 } ?: 1.0
        val minValue = points.minOrNull() ?: 0.0
        val range = (maxValue - minValue).takeIf { it > 0.0 } ?: 1.0
        val step = size.width / (points.lastIndex).coerceAtLeast(1)
        val path = Path()
        val fill = Path()
        points.forEachIndexed { index, value ->
            val x = index * step
            val y = size.height - (((value - minValue) / range).toFloat() * size.height)
            if (index == 0) {
                path.moveTo(x, y)
                fill.moveTo(x, size.height)
                fill.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fill.lineTo(x, y)
            }
            if (index == points.lastIndex) {
                fill.lineTo(x, size.height)
                fill.close()
            }
        }
        drawPath(fill, fillColor)
        drawPath(path, lineColor, style = Stroke(width = 4f, cap = StrokeCap.Round))
    }
}

@Composable
fun ShimmerDealPlaceholder(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(4) { index ->
                Box(
                    Modifier
                        .fillMaxWidth(if (index == 1) 0.72f else 1f)
                        .height(if (index == 0) 150.dp else 18.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                )
            }
        }
    }
}

@Composable
fun FilterChipRow(items: List<String>, selected: String, onSelected: (String) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items.forEach { item ->
            if (item == selected) {
                ElevatedAssistChip(onClick = { onSelected(item) }, label = { Text(item) })
            } else {
                AssistChip(onClick = { onSelected(item) }, label = { Text(item) })
            }
        }
    }
}

@Composable
fun EmptyState(text: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("No deals", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
    }
}

@Composable
private fun InfoPill(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        color = MaterialTheme.colorScheme.secondary,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun DealCountdown(expiryDate: Long) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(expiryDate) {
        while (true) {
            kotlinx.coroutines.delay(1_000)
            now = System.currentTimeMillis()
        }
    }
    val remaining = max(0L, expiryDate - now)
    val hours = remaining / 3_600_000
    val minutes = (remaining / 60_000) % 60
    val seconds = (remaining / 1_000) % 60
    Text(
        text = if (remaining == 0L) "Expired" else "Ends in %02d:%02d:%02d".format(hours, minutes, seconds),
        color = if (remaining == 0L) DealRed else MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun Badge(text: String, color: Color, contentColor: Color = Color.White) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        color = contentColor,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold
    )
}

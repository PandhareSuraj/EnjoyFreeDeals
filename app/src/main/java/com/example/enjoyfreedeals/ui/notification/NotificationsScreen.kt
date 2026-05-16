package com.example.enjoyfreedeals.ui.notification

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.enjoyfreedeals.data.model.DealNotification

@Composable
fun NotificationsScreen(notifications: List<DealNotification>, onClick: (DealNotification) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Notifications", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text("Hot deals, free deals, and expiring soon alerts.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f))
        }
        if (notifications.isEmpty()) {
            item { Text("Notifications could not be loaded right now.") }
        } else {
            items(notifications.size) { index ->
                val notification = notifications[index]
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = if (notification.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth().clickable { onClick(notification) }
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(notification.title, fontWeight = FontWeight.Black)
                            Text(notification.message, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f))
                        }
                        if (!notification.isRead) Badge { Text("New") }
                    }
                }
            }
        }
    }
}

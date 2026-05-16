package com.example.enjoyfreedeals.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.enjoyfreedeals.R
import com.example.enjoyfreedeals.data.model.UserProfile

@Composable
fun ProfileScreen(
    user: UserProfile?,
    darkMode: Boolean,
    notificationsEnabled: Boolean,
    onDark: () -> Unit,
    onNotifications: () -> Unit,
    onSaved: () -> Unit,
    onAbout: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, modifier = Modifier.fillMaxWidth())
        Image(painterResource(R.drawable.ic_enjoy_free_deals_logo), contentDescription = "Profile avatar", modifier = Modifier.size(92.dp))
        Text(user?.name.orEmpty().ifBlank { "Deal Hunter" }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        Text(user?.email.orEmpty().ifBlank { "hunter@bizflow.in" }, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f))
        Text(user?.mobile.orEmpty().ifBlank { "Mobile number not added" }, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Saved deals", (user?.savedDeals?.size ?: 0).toString(), Modifier.weight(1f))
            StatCard("Shared deals", (user?.sharedDeals ?: 0).toString(), Modifier.weight(1f))
        }
        StatCard("Clicked deals", (user?.clickedDeals?.size ?: 0).toString(), Modifier.fillMaxWidth())

        Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
            Column {
                ListItem(headlineContent = { Text("Dark mode") }, trailingContent = { Switch(checked = darkMode, onCheckedChange = { onDark() }) })
                ListItem(headlineContent = { Text("Notifications") }, trailingContent = { Switch(checked = notificationsEnabled, onCheckedChange = { onNotifications() }) })
            }
        }
        OutlinedButton(onClick = onSaved, modifier = Modifier.fillMaxWidth()) {
            Text("Saved Deals")
        }
        OutlinedButton(onClick = onAbout, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Info, null)
            Text(" About App")
        }
        Button(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Logout, null)
            Text(" Logout")
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(8.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

package com.example.enjoyfreedeals.ui.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.enjoyfreedeals.R
import com.example.enjoyfreedeals.data.mock.MockData

@Composable
fun AboutScreen() {
    val info = MockData.appInfo
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Image(painterResource(R.drawable.ic_enjoy_free_deals_logo), contentDescription = "EnjoyFreeDeals logo", modifier = Modifier.size(104.dp))
        Text(info.appName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        Text("Save More. Earn More.", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AboutLine("Created By", info.createdBy)
                AboutLine("Version", info.version)
                AboutLine("Contact", info.contactEmail)
                Text("Description", fontWeight = FontWeight.Black)
                Text(info.description)
            }
        }
    }
}

@Composable
private fun AboutLine(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f))
        Text(value, fontWeight = FontWeight.Bold)
    }
}

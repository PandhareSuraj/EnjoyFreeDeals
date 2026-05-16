package com.example.enjoyfreedeals.ui.blog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.enjoyfreedeals.R
import com.example.enjoyfreedeals.data.model.Blog

@Composable
fun BlogScreen(blogs: List<Blog>) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("Blog", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text("Guides to shop smarter across your favorite stores.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f))
        }
        items(blogs.size) { index ->
            val blog = blogs[index]
            Card(shape = RoundedCornerShape(8.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                Column {
                    AsyncImage(
                        model = blog.image,
                        contentDescription = blog.title,
                        modifier = Modifier.fillMaxWidth().height(155.dp),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.ic_enjoy_free_deals_logo),
                        error = painterResource(R.drawable.ic_enjoy_free_deals_logo)
                    )
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(blog.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                        Text(blog.shortDescription, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f))
                        Button(onClick = {}) { Text("Read More") }
                    }
                }
            }
        }
    }
}

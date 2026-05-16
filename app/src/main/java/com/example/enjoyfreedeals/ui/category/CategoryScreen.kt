package com.example.enjoyfreedeals.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.enjoyfreedeals.data.model.Category

@Composable
fun CategoryScreen(categories: List<Category>, onCategory: (String) -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Category", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Text("Pick a category to see matching deals.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f))
        if (categories.isEmpty()) {
            Text("No categories are available right now.")
        } else {
            LazyVerticalGrid(columns = GridCells.Adaptive(148.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(categories) { category ->
                    CategoryCard(category, onCategory)
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(category: Category, onCategory: (String) -> Unit) {
    val start = runCatching { Color(android.graphics.Color.parseColor(category.gradientColor1)) }.getOrDefault(MaterialTheme.colorScheme.primary)
    val end = runCatching { Color(android.graphics.Color.parseColor(category.gradientColor2)) }.getOrDefault(MaterialTheme.colorScheme.secondary)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(118.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.linearGradient(listOf(start, end)))
            .clickable { onCategory(category.categoryId) }
            .padding(14.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Text(category.categoryName, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
    }
}

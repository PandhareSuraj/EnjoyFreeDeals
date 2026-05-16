package com.example.enjoyfreedeals.data.remote

/**
 * Android does not scrape ecommerce pages.
 *
 * Supported live deal flow:
 * Affiliate APIs, public deal APIs, price-history APIs, WordPress REST API, or Supabase Admin Panel
 * -> custom backend or Supabase Edge Function
 * -> normalized Supabase `deals` rows
 * -> Android live deals listener.
 */
data class BackendDealPipeline(
    val source: String,
    val normalizedIntoSupabase: Boolean = true
)

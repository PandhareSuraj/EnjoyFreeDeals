package com.example.enjoyfreedeals.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import com.example.enjoyfreedeals.data.model.Deal
import java.net.URI

object DealUrlUtils {
    fun bestDealUrl(deal: Deal): String = listOf(deal.affiliateUrl, deal.dealUrl, deal.targetUrl)
        .firstOrNull { it.isNotBlank() }
        ?.let(::normalizeUrl)
        .orEmpty()

    fun normalizeUrl(url: String): String {
        val trimmed = url.trim()
        if (trimmed.isBlank()) return ""
        return if (trimmed.startsWith("http://", true) || trimmed.startsWith("https://", true)) trimmed else "https://$trimmed"
    }

    fun isValidWebUrl(url: String): Boolean {
        val uri = runCatching { URI(normalizeUrl(url)) }.getOrNull() ?: return false
        return uri.scheme in listOf("http", "https") && !uri.host.isNullOrBlank()
    }

    fun openDealUrl(context: Context, deal: Deal): Boolean {
        val url = bestDealUrl(deal)
        if (url.isBlank()) {
            Toast.makeText(context, "Deal link is currently unavailable.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!isValidWebUrl(url)) {
            Toast.makeText(context, "Unable to open this deal right now.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!isExactDealUrl(url)) {
            Toast.makeText(context, "Exact deal link unavailable.", Toast.LENGTH_SHORT).show()
            return false
        }
        val uri = Uri.parse(url)
        runCatching {
            CustomTabsIntent.Builder().build().launchUrl(context, uri)
        }.onFailure {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
        return true
    }

    fun isExactDealUrl(url: String): Boolean {
        val uri = runCatching { URI(normalizeUrl(url)) }.getOrNull() ?: return false
        val path = uri.path.orEmpty().trim('/')
        val query = uri.query.orEmpty().lowercase()
        if (path.isBlank()) return false
        val searchSignals = listOf("search", "s?k=", "q=", "keyword=", "text=", "searchb")
        return searchSignals.none { signal ->
            path.lowercase().contains(signal.trimEnd('=')) || query.contains(signal)
        }
    }

    fun shareText(deal: Deal): String = """
        Check this amazing deal on EnjoyFreeDeals!

        Product: ${deal.title}
        Price: Rs ${deal.displayCurrentPrice.toInt()}
        Discount: ${deal.discountPercent}%
        Store: ${deal.storeName}

        View Offer: ${bestDealUrl(deal)}

        Save More. Earn More.
    """.trimIndent()
}

package com.example.enjoyfreedeals.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import com.example.enjoyfreedeals.data.model.Deal
import java.net.URI
import java.net.URLDecoder

object DealUrlUtils {
    fun bestDealUrl(deal: Deal): String = listOf(deal.affiliateUrl, deal.dealUrl, deal.targetUrl)
        .firstOrNull { rawUrl ->
            val url = normalizeUrl(rawUrl)
            url.isNotBlank() && isValidWebUrl(url) && isExactDealUrl(url)
        }
        ?.let(::normalizeUrl)
        .orEmpty()

    fun firstAvailableUrl(deal: Deal): String = listOf(deal.affiliateUrl, deal.dealUrl, deal.targetUrl)
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
        val availableUrl = firstAvailableUrl(deal)
        if (availableUrl.isBlank()) {
            Toast.makeText(context, "Deal link is currently unavailable.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!isValidWebUrl(availableUrl)) {
            Toast.makeText(context, "Unable to open this deal right now.", Toast.LENGTH_SHORT).show()
            return false
        }
        val url = bestDealUrl(deal)
        if (url.isBlank()) {
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
        val host = uri.host.orEmpty().lowercase().removePrefix("www.")
        val path = uri.path.orEmpty().trim('/')
        val query = uri.query.orEmpty().lowercase()
        val normalized = normalizeUrl(url).lowercase()
        val nestedOfferUrl = extractNestedOfferUrl(uri.query.orEmpty())
        if (!nestedOfferUrl.isNullOrBlank() && normalizeUrl(nestedOfferUrl).lowercase() != normalized) {
            return isExactDealUrl(nestedOfferUrl)
        }
        if (path.isBlank()) return false
        if (listOf("mock", "placeholder", "dummy", "example.com", "enjoyfreedeals.example").any { normalized.contains(it) }) return false

        val homeOrListingPaths = setOf(
            "deals", "offers", "sale", "coupon", "coupons", "category", "categories",
            "shop", "collections", "c", "s", "search", "store", "stores"
        )
        if (path.lowercase() in homeOrListingPaths) return false

        val searchSignals = listOf("search", "s?k=", "q=", "query=", "keyword=", "text=", "searchb", "searchtext=")
        if (searchSignals.any { signal -> path.lowercase().contains(signal.trimEnd('=')) || query.contains(signal) }) return false

        return when {
            host.contains("amazon.") -> Regex("/(dp|gp/product)/[a-z0-9]{6,}", RegexOption.IGNORE_CASE).containsMatchIn("/$path")
            host.contains("flipkart.") -> "/p/" in "/$path/"
            host.contains("meesho.") -> "/p/" in "/$path/"
            host.contains("myntra.") -> path.split('/').size >= 2 && path.any(Char::isDigit)
            host.contains("ajio.") -> "/p/" in "/$path/"
            host.contains("tatacliq.") -> Regex("/p-[a-z0-9-]+", RegexOption.IGNORE_CASE).containsMatchIn("/$path")
            host.contains("nykaa.") -> "/p/" in "/$path/" || path.any(Char::isDigit)
            host.contains("croma.") -> "/p/" in "/$path/"
            host.contains("jiomart.") -> "/p/" in "/$path/"
            host.contains("bigbasket.") -> "/pd/" in "/$path/"
            else -> path.split('/').any { segment -> segment.any(Char::isDigit) || segment.length >= 12 }
        }
    }

    private fun extractNestedOfferUrl(query: String): String? {
        return query.split('&')
            .firstOrNull { it.substringBefore('=') == "url" }
            ?.substringAfter('=', "")
            ?.takeIf { it.isNotBlank() }
            ?.let { encoded -> runCatching { URLDecoder.decode(encoded, Charsets.UTF_8.name()) }.getOrElse { encoded } }
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

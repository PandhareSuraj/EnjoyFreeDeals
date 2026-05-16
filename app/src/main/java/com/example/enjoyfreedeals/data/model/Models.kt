package com.example.enjoyfreedeals.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Serializable
data class UserProfile(
    @SerialName("user_id")
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val mobile: String = "",
    @SerialName("profile_image")
    val profileImage: String = "",
    @SerialName("saved_deals")
    val savedDeals: List<String> = emptyList(),
    @SerialName("clicked_deals")
    val clickedDeals: List<String> = emptyList(),
    @SerialName("shared_deals")
    val sharedDeals: Int = 0,
    @SerialName("notification_enabled")
    val notificationEnabled: Boolean = true,
    @SerialName("dark_mode_enabled")
    val darkModeEnabled: Boolean = false,
    @SerialName("created_at")
    val createdAt: String = nowIsoString()
)

@Serializable
data class Deal(
    @SerialName("deal_id")
    val dealId: String = "",
    @SerialName("external_product_id")
    val externalProductId: String = "",
    val title: String = "",
    val description: String = "",
    @SerialName("product_image")
    val productImage: String = "",
    @SerialName("original_price")
    val originalPrice: Double = 0.0,
    @SerialName("discounted_price")
    val discountedPrice: Double = 0.0,
    @SerialName("current_price")
    val currentPrice: Double = 0.0,
    @SerialName("lowest_price")
    val lowestPrice: Double = 0.0,
    @SerialName("highest_price")
    val highestPrice: Double = 0.0,
    @SerialName("price_history")
    val priceHistory: List<Double> = emptyList(),
    @SerialName("is_lowest_price_now")
    val isLowestPriceNow: Boolean = false,
    @SerialName("discount_percent")
    val discountPercent: Int = 0,
    @SerialName("store_name")
    val storeName: String = "",
    @SerialName("store_logo")
    val storeLogo: String = "",
    @SerialName("category_id")
    val categoryId: String = "",
    @SerialName("deal_type")
    val dealType: String = "Discount",
    @SerialName("coupon_code")
    val couponCode: String = "",
    @SerialName("cashback_text")
    val cashbackText: String = "",
    @SerialName("deal_url")
    val dealUrl: String = "",
    @SerialName("affiliate_url")
    val affiliateUrl: String = "",
    @SerialName("target_url")
    val targetUrl: String = "",
    @SerialName("source_platform")
    val sourcePlatform: String = "",
    @SerialName("source_type")
    val sourceType: String = "Live Affiliate API",
    @SerialName("is_hot_deal")
    val isHotDeal: Boolean = false,
    @SerialName("is_free_deal")
    val isFreeDeal: Boolean = false,
    @SerialName("is_verified")
    val isVerified: Boolean = true,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("availability")
    val availability: String = "in_stock",
    @SerialName("click_count")
    val clickCount: Int = 0,
    @SerialName("share_count")
    val shareCount: Int = 0,
    @SerialName("save_count")
    val saveCount: Int = 0,
    @SerialName("created_at")
    val createdAt: String = nowIsoString(),
    @SerialName("updated_at")
    val updatedAt: String = nowIsoString(),
    @SerialName("last_price_checked_at")
    val lastPriceCheckedAt: String = nowIsoString(),
    @SerialName("expiry_date")
    val expiryDate: Long = System.currentTimeMillis() + 86_400_000L
) {
    val bestUrl: String get() = listOf(affiliateUrl, dealUrl, targetUrl).firstOrNull { it.isNotBlank() }.orEmpty()
    val displayCurrentPrice: Double get() = currentPrice.takeIf { it > 0.0 } ?: discountedPrice
    val displayLowestPrice: Double get() = lowestPrice.takeIf { it > 0.0 } ?: displayCurrentPrice
    val isNearLowestPrice: Boolean get() = displayLowestPrice > 0.0 && displayCurrentPrice <= displayLowestPrice * 1.10
    val createdAtMillis: Long get() = parseTimestampMillis(createdAt)
    val updatedAtMillis: Long get() = parseTimestampMillis(updatedAt)
    val lastPriceCheckedAtMillis: Long get() = parseTimestampMillis(lastPriceCheckedAt)
    val priceNeedsRefresh: Boolean get() = System.currentTimeMillis() - lastPriceCheckedAtMillis > 86_400_000L
}

@Serializable
data class Category(
    @SerialName("category_id")
    val categoryId: String = "",
    @SerialName("name")
    val categoryName: String = "",
    @SerialName("icon")
    val categoryIcon: String = "",
    @SerialName("category_image")
    val categoryImage: String = "",
    @SerialName("gradient_color_1")
    val gradientColor1: String = "#E91B23",
    @SerialName("gradient_color_2")
    val gradientColor2: String = "#006B2E",
    @SerialName("is_active")
    val isActive: Boolean = true
)

@Serializable
data class Blog(
    @SerialName("blog_id")
    val blogId: String = "",
    val title: String = "",
    val image: String = "",
    @SerialName("summary")
    val shortDescription: String = "",
    @SerialName("content")
    val fullContent: String = "",
    @SerialName("created_at")
    val createdAt: String = nowIsoString()
)

@Serializable
data class DealNotification(
    @SerialName("notification_id")
    val notificationId: String = "",
    val title: String = "",
    val message: String = "",
    val image: String = "",
    @SerialName("deal_id")
    val dealId: String = "",
    @SerialName("target_url")
    val targetUrl: String = "",
    @SerialName("created_at")
    val createdAt: String = nowIsoString(),
    @SerialName("is_read")
    val isRead: Boolean = false
)

@Serializable
data class AppInfo(
    val appName: String = "EnjoyFreeDeals",
    val version: String = "1.0.0",
    val createdBy: String = "BizFlow Team",
    val description: String = "EnjoyFreeDeals helps users find free deals, coupons, cashback offers, discount offers, and shopping deals from multiple e-commerce platforms like Amazon, Flipkart, Meesho, Myntra, Snapdeal, Ajio, TataCliq, Nykaa, Croma, JioMart, BigBasket, and more.",
    val contactEmail: String = "support@bizflow.in"
)

enum class DealSort(val label: String) {
    Newest("Newest"),
    HighestDiscount("Highest Discount"),
    LowestPrice("Live price low to high"),
    FreeDeals("Free Deals"),
    ExpiringSoon("Expiring Soon"),
    MostClicked("Most Clicked"),
    VerifiedDeals("Verified Deals"),
    HotDeals("Hot Deals"),
    RecentlyUpdated("Recently updated"),
    LowestPriceNow("Lowest price now")
}

fun nowIsoString(): String {
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    format.timeZone = TimeZone.getTimeZone("UTC")
    return format.format(System.currentTimeMillis())
}

fun parseTimestampMillis(value: String): Long {
    if (value.isBlank()) return 0L
    value.toLongOrNull()?.let { return it }
    val normalized = value.trim().replace(Regex("\\.\\d+"), "")
    val formats = listOf(
        "yyyy-MM-dd'T'HH:mm:ssX",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd HH:mm:ss"
    )
    return formats.firstNotNullOfOrNull { pattern ->
        runCatching {
            SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.parse(normalized)?.time
        }.getOrNull()
    } ?: 0L
}

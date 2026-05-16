package com.example.enjoyfreedeals.data.mock

import com.example.enjoyfreedeals.data.model.AppInfo
import com.example.enjoyfreedeals.data.model.Blog
import com.example.enjoyfreedeals.data.model.Category
import com.example.enjoyfreedeals.data.model.Deal
import com.example.enjoyfreedeals.data.model.DealNotification
import com.example.enjoyfreedeals.data.model.UserProfile

object MockData {
    private const val imageBase = "https://images.unsplash.com/"

    val stores = listOf("Amazon", "Flipkart", "Meesho", "Myntra", "Snapdeal", "Ajio", "TataCliq", "Nykaa", "Croma", "JioMart", "BigBasket")
    val filters = stores + listOf("Free Deals", "Coupons", "Cashback", "Bank Offers")

    val categories = listOf(
        Category("electronics", "Electronics", "Bolt", gradientColor1 = "#E91B23", gradientColor2 = "#FF8A00"),
        Category("mobiles", "Mobiles", "Phone", gradientColor1 = "#006B2E", gradientColor2 = "#21B573"),
        Category("fashion", "Fashion", "Shirt", gradientColor1 = "#E91B23", gradientColor2 = "#7C3AED"),
        Category("beauty", "Beauty", "Sparkles", gradientColor1 = "#D946EF", gradientColor2 = "#F97316"),
        Category("grocery", "Grocery", "Basket", gradientColor1 = "#006B2E", gradientColor2 = "#FFD600"),
        Category("home", "Home & Kitchen", "Home", gradientColor1 = "#0F766E", gradientColor2 = "#06B6D4"),
        Category("free_samples", "Free Samples", "Gift", gradientColor1 = "#E91B23", gradientColor2 = "#FFD600"),
        Category("coupons", "Coupons", "Badge", gradientColor1 = "#111827", gradientColor2 = "#E91B23"),
        Category("recharge", "Recharge Offers", "Zap", gradientColor1 = "#2563EB", gradientColor2 = "#22C55E"),
        Category("bank", "Bank Offers", "Card", gradientColor1 = "#1E1E1E", gradientColor2 = "#006B2E"),
        Category("student", "Student Deals", "Book", gradientColor1 = "#7C2D12", gradientColor2 = "#FFD600"),
        Category("festival", "Festival Deals", "Star", gradientColor1 = "#E91B23", gradientColor2 = "#EC4899"),
        Category("travel", "Travel Deals", "Plane", gradientColor1 = "#0891B2", gradientColor2 = "#0F172A"),
        Category("food", "Food Deals", "Utensils", gradientColor1 = "#EA580C", gradientColor2 = "#16A34A"),
        Category("baby", "Baby Products", "Heart", gradientColor1 = "#BE185D", gradientColor2 = "#FDE68A"),
        Category("gaming", "Gaming", "Gamepad", gradientColor1 = "#4F46E5", gradientColor2 = "#111827")
    )

    val deals = listOf(
        deal("amazon-boat-earbuds", "boAt Earbuds", "Immersive sound, fast charging, and long battery backup.", 2999.0, 1199.0, 60, "Amazon", "electronics", true),
        deal("flipkart-realme-phone", "Realme Smartphone", "5G performance phone with smooth display and fast charging.", 14999.0, 11999.0, 20, "Flipkart", "mobiles"),
        deal("meesho-kurti", "Women Kurti", "Printed cotton kurti for daily and festive wear.", 999.0, 299.0, 70, "Meesho", "fashion", true),
        deal("myntra-shoes", "Sports Shoes", "Lightweight running shoes with cushioned sole.", 3999.0, 1799.0, 55, "Myntra", "fashion"),
        deal("snapdeal-kitchen", "Kitchen Storage Set", "Airtight containers for organized kitchens.", 1499.0, 824.0, 45, "Snapdeal", "home"),
        deal("ajio-tshirt", "Men's T-Shirt", "Soft cotton T-shirt with modern fit.", 799.0, 399.0, 50, "Ajio", "fashion"),
        deal("tatacliq-smartwatch", "Smartwatch", "Fitness tracking, calling, and bright display.", 4999.0, 2999.0, 40, "TataCliq", "electronics", true),
        deal("nykaa-beauty", "Beauty Combo", "Skincare and makeup essentials in one kit.", 1999.0, 1299.0, 35, "Nykaa", "beauty"),
        deal("croma-speaker", "Bluetooth Speaker", "Portable speaker with punchy bass.", 2499.0, 1749.0, 30, "Croma", "electronics"),
        deal("jiomart-grocery", "Grocery Combo", "Monthly essentials bundled at a lower price.", 1200.0, 899.0, 25, "JioMart", "grocery"),
        deal("bigbasket-fruit", "Fruit Basket", "Fresh seasonal fruits delivered home.", 999.0, 799.0, 20, "BigBasket", "grocery"),
        deal("free-skincare-kit", "Skincare Sample Kit", "Try a curated skincare sample kit for free.", 499.0, 0.0, 100, "Free Sample", "free_samples", true, true),
        deal("amazon-coupon-deal", "Amazon Coupon Deal", "Extra coupon on selected home and electronics picks.", 2499.0, 1899.0, 24, "Amazon", "coupons", coupon = "EFDAMZ24"),
        deal("flipkart-bank-offer", "Flipkart Bank Offer", "Instant card discount on smartphones and appliances.", 22999.0, 19999.0, 13, "Flipkart", "bank", coupon = "BANK10"),
        deal("myntra-fashion-sale", "Myntra Fashion Sale", "Curated fashion sale with extra app-only discounts.", 2999.0, 1199.0, 60, "Myntra", "fashion", true, coupon = "STYLE60"),
        deal("meesho-free-delivery", "Meesho Free Delivery Offer", "Free delivery on trending fashion and home products.", 699.0, 499.0, 29, "Meesho", "coupons", coupon = "FREEDEL"),
        deal("ajio-cashback", "Ajio Cashback Offer", "Earn wallet cashback on selected apparel.", 1999.0, 999.0, 50, "Ajio", "fashion", cashback = "Up to Rs 150 cashback"),
        deal("croma-electronics-sale", "Croma Electronics Sale", "Limited-time prices on speakers, wearables, and accessories.", 5999.0, 3499.0, 42, "Croma", "electronics", true),
        deal("jiomart-grocery-discount", "JioMart Grocery Discount", "Save more on pantry staples and household essentials.", 1599.0, 1099.0, 31, "JioMart", "grocery", coupon = "GROCERY31"),
        deal("bigbasket-weekend", "BigBasket Weekend Offer", "Weekend produce bundle and staples offer.", 1299.0, 949.0, 27, "BigBasket", "grocery", cashback = "5% wallet cashback")
    )

    val blogs = listOf(
        Blog("best-deals-online", "How to Find Best Deals Online", imageBase + "photo-1472851294608-062f824d29cc", "Simple habits that help you spot real discounts faster.", "Compare prices, check bank offers, follow trusted deal pages, and avoid impulse buying."),
        Blog("amazon-sale-tips", "Top 10 Amazon Sale Tips", imageBase + "photo-1523474253046-8cd2748b5fd2", "Plan wishlists, cards, coupons, and lightning deals before the sale starts.", "Use wishlists, price alerts, coupons, and bank offers together for better savings."),
        Blog("flipkart-tricks", "Best Flipkart Shopping Tricks", imageBase + "photo-1556742049-0cfed4f6a45d", "Make the most of sale days and exchange bonuses.", "Track price history and stack payment offers with exchange bonuses when useful."),
        Blog("coupon-codes", "How to Use Coupon Codes", imageBase + "photo-1563013544-824ae1b704d3", "Find, test, and stack coupons without wasting time.", "Read coupon terms, minimum cart value, and category restrictions before checkout."),
        Blog("cashback-guide", "Cashback and Bank Offer Guide", imageBase + "photo-1554224155-6726b3ff858f", "Understand instant discounts, wallet cashback, and card offers.", "Instant discount saves now; cashback may arrive later and should be tracked."),
        Blog("meesho-guide", "Meesho Shopping Guide", imageBase + "photo-1555529669-e69e7aa0ba9a", "Tips for checking seller quality and product fit.", "Read reviews, return windows, and size charts carefully."),
        Blog("myntra-fashion", "Myntra Fashion Sale Tips", imageBase + "photo-1483985988355-763728e1935b", "Build carts smartly during fashion sale events.", "Use filters, brand coupons, and size availability alerts.")
    )

    val notifications = listOf(
        DealNotification("n1", "Hot deal live", "boAt earbuds are now 60% off.", dealId = "amazon-boat-earbuds"),
        DealNotification("n2", "Free deal alert", "Skincare Sample Kit is available as a free deal.", dealId = "free-skincare-kit"),
        DealNotification("n3", "Expiring soon", "Smartwatch deal may expire today.", dealId = "tatacliq-smartwatch")
    )

    val user = UserProfile("mock-user", "Deal Hunter", "hunter@bizflow.in", "9876543210", savedDeals = listOf("amazon-boat-earbuds"))
    val appInfo = AppInfo()

    private fun deal(id: String, title: String, description: String, original: Double, price: Double, discount: Int, store: String, category: String, hot: Boolean = false, free: Boolean = false, coupon: String = "", cashback: String = "") = Deal(
        dealId = id,
        title = "$store - $title",
        description = description,
        productImage = "$imageBase${when (category) { "fashion" -> "photo-1445205170230-053b83016050"; "beauty" -> "photo-1596462502278-27bfdc403348"; "grocery" -> "photo-1542838132-92c53300491e"; "home" -> "photo-1556911220-bff31c812dba"; "mobiles" -> "photo-1511707171634-5f897ff02aa9"; else -> "photo-1505740420928-5e560c06d30e" }}?auto=format&fit=crop&w=900&q=80",
        originalPrice = original,
        discountedPrice = price,
        discountPercent = discount,
        storeName = store,
        storeLogo = "",
        categoryId = category,
        dealType = when {
            free -> "Free Deal"
            coupon.isNotBlank() -> "Coupon"
            cashback.isNotBlank() -> "Cashback"
            category == "bank" -> "Bank Offer"
            else -> "Discount"
        },
        couponCode = coupon,
        cashbackText = cashback,
        currentPrice = price,
        lowestPrice = if (free) 0.0 else (price * 0.97).toInt().toDouble(),
        highestPrice = original,
        priceHistory = listOf(original, original * 0.88, original * 0.76, price * 1.08, price),
        isLowestPriceNow = free,
        dealUrl = realStoreUrl(store, title),
        affiliateUrl = "",
        targetUrl = "",
        sourcePlatform = store,
        sourceType = if (free) "URL unavailable" else "Live Affiliate API",
        isHotDeal = hot,
        isFreeDeal = free,
        isVerified = true,
        availability = if (free) "out_of_stock" else "in_stock",
        clickCount = discount * 3,
        expiryDate = System.currentTimeMillis() + if (hot) 6 * 60 * 60 * 1000 else 3 * 86_400_000L
    )

    private fun realStoreUrl(store: String, title: String): String = when (store) {
        "Amazon" -> "https://www.amazon.in/dp/B0${query(title).take(8).uppercase().padEnd(8, '0')}"
        "Flipkart" -> "https://www.flipkart.com/${query(title).replace("+", "-")}/p/mock-${query(title)}"
        "Meesho" -> "https://www.meesho.com/${query(title).replace("+", "-")}/p/mock${query(title).take(5)}"
        "Myntra" -> "https://www.myntra.com/${query(title).replace("+", "-")}/mock-product"
        "Snapdeal" -> "https://www.snapdeal.com/product/${query(title).replace("+", "-")}/mock"
        "Ajio" -> "https://www.ajio.com/${query(title).replace("+", "-")}/p/mock"
        "TataCliq" -> "https://www.tatacliq.com/${query(title).replace("+", "-")}/p-mock"
        "Nykaa" -> "https://www.nykaa.com/${query(title).replace("+", "-")}/p/mock"
        "Croma" -> "https://www.croma.com/${query(title).replace("+", "-")}/p/mock"
        "JioMart" -> "https://www.jiomart.com/p/${query(title).replace("+", "-")}/mock"
        "BigBasket" -> "https://www.bigbasket.com/pd/mock/${query(title).replace("+", "-")}/"
        else -> ""
    }

    private fun query(value: String): String = value.trim().lowercase().replace(Regex("[^a-z0-9]+"), "+").trim('+')
}

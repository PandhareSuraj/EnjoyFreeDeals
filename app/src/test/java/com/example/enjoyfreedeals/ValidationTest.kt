package com.example.enjoyfreedeals

import com.example.enjoyfreedeals.data.mock.MockData
import com.example.enjoyfreedeals.data.model.Deal
import com.example.enjoyfreedeals.data.repository.Validators
import com.example.enjoyfreedeals.utils.DealUrlUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationTest {
    @Test
    fun loginValidationCoversEmptyInvalidAndValidInput() {
        assertNotNull(Validators.loginError("", ""))
        assertNotNull(Validators.loginError("wrong-email", "secret123"))
        assertNull(Validators.loginError("hunter@bizflow.in", "secret123"))
    }

    @Test
    fun registrationValidationCoversRequiredMobilePasswordAndSuccess() {
        assertNotNull(Validators.registrationError("", "", "", "", ""))
        assertNotNull(Validators.registrationError("Deal Hunter", "hunter@bizflow.in", "1234", "secret123", "secret123"))
        assertNotNull(Validators.registrationError("Deal Hunter", "hunter@bizflow.in", "9876543210", "secret123", "different"))
        assertNull(Validators.registrationError("Deal Hunter", "hunter@bizflow.in", "9876543210", "secret123", "secret123"))
    }

    @Test
    fun mockDealsContainExpectedStoresAndFreeDeal() {
        assertEquals(20, MockData.deals.size)
        assertTrue(MockData.deals.any { it.storeName == "Amazon" && it.discountPercent == 60 })
        assertTrue(MockData.deals.any { it.isFreeDeal && it.discountedPrice == 0.0 })
        assertTrue(MockData.deals.none { it.bestUrl.contains("example", ignoreCase = true) })
        assertTrue(MockData.deals.all { it.sourceType.contains("URL unavailable", ignoreCase = true) })
        assertTrue(MockData.deals.all { it.bestUrl.isBlank() })
    }

    @Test
    fun checkDealUsesAffiliateDealThenTargetUrlPriority() {
        val affiliateDeal = Deal(affiliateUrl = "affiliate.partner.test/offer/amazon-boat-earbuds-123456", dealUrl = "https://store.test/product/123456", targetUrl = "https://target.test/product/123456")
        val directDeal = Deal(dealUrl = "store.test/product/123456", targetUrl = "https://target.test/product/123456")
        val targetDeal = Deal(targetUrl = "https://target.test/product/123456")
        assertEquals("https://affiliate.partner.test/offer/amazon-boat-earbuds-123456", DealUrlUtils.bestDealUrl(affiliateDeal))
        assertEquals("https://store.test/product/123456", DealUrlUtils.bestDealUrl(directDeal))
        assertEquals("https://target.test/product/123456", DealUrlUtils.bestDealUrl(targetDeal))
        assertTrue(DealUrlUtils.isValidWebUrl("target.test/product/123456"))
        assertTrue(!DealUrlUtils.isValidWebUrl(""))
    }

    @Test
    fun exactDealValidationRejectsSearchHomeAndMockUrls() {
        assertTrue(DealUrlUtils.isExactDealUrl("https://www.amazon.in/dp/B09ABC1234"))
        assertTrue(DealUrlUtils.isExactDealUrl("https://go.enjoyfreedeals.in/offer?url=https%3A%2F%2Fwww.amazon.in%2Fdp%2FB09ABC1234"))
        assertTrue(!DealUrlUtils.isExactDealUrl("https://www.amazon.in/"))
        assertTrue(!DealUrlUtils.isExactDealUrl("https://www.amazon.in/s?k=earbuds"))
        assertTrue(!DealUrlUtils.isExactDealUrl("https://www.flipkart.com/search?q=phone"))
        assertTrue(!DealUrlUtils.isExactDealUrl("https://www.flipkart.com/realme-phone/p/mock-realme-phone"))
    }

    @Test
    fun priceQualityFlagsLowestAndNearLowestDeals() {
        val lowest = Deal(currentPrice = 90.0, discountedPrice = 90.0, lowestPrice = 90.0, originalPrice = 200.0)
        val nearLowest = Deal(currentPrice = 99.0, discountedPrice = 99.0, lowestPrice = 90.0, originalPrice = 200.0)
        val expensive = Deal(currentPrice = 140.0, discountedPrice = 140.0, lowestPrice = 90.0, originalPrice = 200.0, discountPercent = 10)
        assertTrue(lowest.displayCurrentPrice <= lowest.displayLowestPrice)
        assertTrue(nearLowest.isNearLowestPrice)
        assertTrue(!expensive.isNearLowestPrice)
    }

    @Test
    fun saveRemoveAndShareModelUpdatesAreRepresentable() {
        val user = MockData.user
        val dealId = "amazon-boat-earbuds"
        assertTrue(dealId in user.savedDeals)
        val removed = user.copy(savedDeals = user.savedDeals - dealId)
        assertTrue(dealId !in removed.savedDeals)
        val shared = user.copy(sharedDeals = user.sharedDeals + 1)
        assertEquals(user.sharedDeals + 1, shared.sharedDeals)
    }

    @Test
    fun categoryFilteringHasEmptyStateScenario() {
        val gamingDeals = MockData.deals.filter { it.categoryId == "gaming" }
        assertTrue(gamingDeals.isEmpty())
        assertTrue(MockData.categories.any { it.categoryId == "gaming" })
    }

    @Test
    fun notificationUnreadBadgeCanUpdate() {
        val notifications = MockData.notifications
        val unread = notifications.count { !it.isRead }
        val read = notifications.map { if (it.notificationId == "n1") it.copy(isRead = true) else it }
        assertEquals(unread - 1, read.count { !it.isRead })
    }
}

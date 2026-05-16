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
        assertTrue(MockData.deals.filterNot { it.sourceType == "URL unavailable" }.all { it.bestUrl.isNotBlank() })
    }

    @Test
    fun checkDealUsesAffiliateDealThenTargetUrlPriority() {
        val affiliateDeal = Deal(affiliateUrl = "affiliate.partner.test/deal", dealUrl = "https://store.test/deal", targetUrl = "https://target.test/deal")
        val directDeal = Deal(dealUrl = "store.test/deal", targetUrl = "https://target.test/deal")
        val targetDeal = Deal(targetUrl = "https://target.test/deal")
        assertEquals("https://affiliate.partner.test/deal", DealUrlUtils.bestDealUrl(affiliateDeal))
        assertEquals("https://store.test/deal", DealUrlUtils.bestDealUrl(directDeal))
        assertEquals("https://target.test/deal", DealUrlUtils.bestDealUrl(targetDeal))
        assertTrue(DealUrlUtils.isValidWebUrl("target.test/deal"))
        assertTrue(!DealUrlUtils.isValidWebUrl(""))
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

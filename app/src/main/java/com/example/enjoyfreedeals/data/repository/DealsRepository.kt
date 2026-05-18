package com.example.enjoyfreedeals.data.repository

import android.content.Context
import com.example.enjoyfreedeals.data.mock.MockData
import com.example.enjoyfreedeals.data.model.Blog
import com.example.enjoyfreedeals.data.model.Category
import com.example.enjoyfreedeals.data.model.Deal
import com.example.enjoyfreedeals.data.model.DealNotification
import com.example.enjoyfreedeals.data.remote.SupabaseApi
import com.example.enjoyfreedeals.data.remote.SupabaseService
import com.example.enjoyfreedeals.utils.DealUrlUtils
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecordOrNull
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.net.URLEncoder

class DealsRepository(private val context: Context) {
    private val readNotifications = mutableSetOf<String>()

    fun getLiveDeals(): Flow<List<Deal>> = callbackFlow {
        val currentDeals = refreshLiveDeals().getOrElse { emptyList() }.toMutableList()
        trySend(currentDeals.toList())

        if (!SupabaseService.isConfigured) {
            close()
            return@callbackFlow
        }

        val channel = SupabaseService.client.channel("live-deals")
        val insertFlow = channel.postgresChangeFlow<PostgresAction.Insert>("public") { table = "deals" }
        val updateFlow = channel.postgresChangeFlow<PostgresAction.Update>("public") { table = "deals" }

        val insertJob = launch {
            insertFlow.collect { action ->
                val deal = action.decodeRecordOrNull<Deal>() ?: return@collect
                handleDealInsert(currentDeals, deal)
                trySend(currentDeals.toList())
            }
        }
        val updateJob = launch {
            updateFlow.collect { action ->
                val deal = action.decodeRecordOrNull<Deal>() ?: return@collect
                handleDealUpdate(currentDeals, deal)
                trySend(currentDeals.toList())
            }
        }

        channel.subscribe()
        awaitClose {
            insertJob.cancel()
            updateJob.cancel()
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.launch { SupabaseService.client.realtime.removeChannel(channel) }
        }
    }

    suspend fun refreshLiveDeals(): Result<List<Deal>> = runCatching {
        if (!SupabaseService.isConfigured) return@runCatching previewDeals()
        val response = SupabaseApi.get("deals", "is_active=eq.true&order=created_at.desc")
        filterBestPriceDeals(SupabaseApi.json.decodeFromString<List<Deal>>(response))
    }.recoverCatching { if (SupabaseService.isConfigured) emptyList() else previewDeals() }

    suspend fun getDeals(): Result<List<Deal>> = refreshLiveDeals()

    suspend fun getBestPriceDeals(): Result<List<Deal>> = getDeals().map(::filterBestPriceDeals)

    suspend fun getDealsByStore(storeName: String): Result<List<Deal>> = getDeals().map { deals ->
        deals.filter { it.storeName.equals(storeName, ignoreCase = true) }
    }

    suspend fun getDealsByCategory(categoryId: String): Result<List<Deal>> = getDeals().map { deals ->
        deals.filter { it.categoryId == categoryId }
    }

    suspend fun searchDeals(query: String): Result<List<Deal>> = getDeals().map { deals ->
        deals.filter { deal ->
            listOf(deal.title, deal.description, deal.storeName, deal.dealType, deal.couponCode, deal.cashbackText)
                .any { it.contains(query, ignoreCase = true) }
        }
    }

    suspend fun saveDeal(userId: String, dealId: String): Result<Unit> = runCatching {
        if (SupabaseService.isConfigured && dealId.isNotBlank()) {
            val deal = fetchDeal(dealId)
            SupabaseApi.patch("deals", buildJsonObject { put("save_count", deal.saveCount + 1) }, "deal_id=eq.${encode(dealId)}")
        }
    }

    suspend fun removeSavedDeal(userId: String, dealId: String): Result<Unit> = runCatching {
        if (SupabaseService.isConfigured && dealId.isNotBlank()) {
            val deal = fetchDeal(dealId)
            SupabaseApi.patch("deals", buildJsonObject { put("save_count", (deal.saveCount - 1).coerceAtLeast(0)) }, "deal_id=eq.${encode(dealId)}")
        }
    }

    suspend fun incrementClickCount(dealId: String): Result<Unit> = runCatching {
        if (SupabaseService.isConfigured && dealId.isNotBlank()) {
            val deal = fetchDeal(dealId)
            SupabaseApi.patch("deals", buildJsonObject { put("click_count", deal.clickCount + 1) }, "deal_id=eq.${encode(dealId)}")
        }
    }

    suspend fun increaseClickCount(dealId: String): Result<Unit> = incrementClickCount(dealId)

    suspend fun shareDeal(deal: Deal): Result<Unit> = runCatching {
        if (SupabaseService.isConfigured && deal.dealId.isNotBlank()) {
            SupabaseApi.patch("deals", buildJsonObject { put("share_count", deal.shareCount + 1) }, "deal_id=eq.${encode(deal.dealId)}")
        }
    }

    fun openDealUrl(context: Context, deal: Deal): Boolean = DealUrlUtils.openDealUrl(context, deal)

    fun subscribeToDealChanges(): Flow<List<Deal>> = getLiveDeals()

    fun handleDealInsert(currentDeals: MutableList<Deal>, deal: Deal) {
        if (!isBestPriceDeal(deal)) return
        currentDeals.removeAll { it.dealId == deal.dealId }
        currentDeals.add(0, deal)
        DealNotificationManager.showNewDealNotification(context, deal)
    }

    fun handleDealUpdate(currentDeals: MutableList<Deal>, deal: Deal) {
        currentDeals.removeAll { it.dealId == deal.dealId }
        if (isBestPriceDeal(deal)) currentDeals.add(0, deal)
        currentDeals.sortByDescending { it.updatedAtMillis }
    }

    suspend fun getCategories(): Result<List<Category>> = runCatching {
        if (!SupabaseService.isConfigured) return@runCatching MockData.categories
        val response = SupabaseApi.get("categories", "is_active=eq.true&order=name.asc")
        SupabaseApi.json.decodeFromString<List<Category>>(response).ifEmpty { MockData.categories }
    }.recoverCatching { MockData.categories }

    suspend fun getBlogs(): Result<List<Blog>> = runCatching {
        if (!SupabaseService.isConfigured) return@runCatching MockData.blogs
        val response = SupabaseApi.get("blogs", "order=created_at.desc")
        SupabaseApi.json.decodeFromString<List<Blog>>(response).ifEmpty { MockData.blogs }
    }.recoverCatching { MockData.blogs }

    suspend fun getNotifications(): Result<List<DealNotification>> = runCatching {
        if (!SupabaseService.isConfigured) return@runCatching MockData.notifications.map { it.copy(isRead = it.notificationId in readNotifications) }
        val response = SupabaseApi.get("notifications", "order=created_at.desc")
        SupabaseApi.json.decodeFromString<List<DealNotification>>(response)
            .ifEmpty { MockData.notifications }
            .map { it.copy(isRead = it.isRead || it.notificationId in readNotifications) }
    }.recoverCatching { MockData.notifications.map { it.copy(isRead = it.notificationId in readNotifications) } }

    suspend fun markNotificationRead(notification: DealNotification): Result<Unit> = runCatching {
        readNotifications += notification.notificationId
        if (SupabaseService.isConfigured && notification.notificationId.isNotBlank()) {
            SupabaseApi.patch("notifications", buildJsonObject { put("is_read", true) }, "notification_id=eq.${encode(notification.notificationId)}")
        }
    }

    private suspend fun fetchDeal(dealId: String): Deal {
        val response = SupabaseApi.get("deals", "deal_id=eq.${encode(dealId)}&limit=1")
        return SupabaseApi.json.decodeFromString<List<Deal>>(response).firstOrNull() ?: Deal(dealId = dealId)
    }

    private fun filterBestPriceDeals(deals: List<Deal>): List<Deal> = deals.filter(::isBestPriceDeal)

    private fun previewDeals(): List<Deal> = MockData.deals
        .filter { deal ->
            val current = deal.displayCurrentPrice
            val lowest = deal.displayLowestPrice
            val isNotExpired = deal.expiryDate == 0L || deal.expiryDate > System.currentTimeMillis()
            isNotExpired &&
                deal.availability.equals("in_stock", ignoreCase = true) &&
                (current > 0.0 || deal.isFreeDeal) &&
                (deal.discountPercent >= 60 || deal.isNearLowestPrice || deal.isHotDeal || deal.isFreeDeal || (lowest > 0.0 && current <= lowest * 1.10))
        }
        .sortedWith(compareByDescending<Deal> { it.discountPercent >= 60 }.thenByDescending { it.updatedAtMillis })

    private fun isBestPriceDeal(deal: Deal): Boolean {
        val current = deal.displayCurrentPrice
        val lowest = deal.displayLowestPrice
        val hasExactUrl = DealUrlUtils.bestDealUrl(deal).isNotBlank() && DealUrlUtils.isExactDealUrl(DealUrlUtils.bestDealUrl(deal))
        val hasPrice = current > 0.0 || deal.isFreeDeal
        val isNotExpired = deal.expiryDate == 0L || deal.expiryDate > System.currentTimeMillis()
        val hasQualitySignal = deal.isFreeDeal ||
            deal.isHotDeal ||
            deal.discountPercent >= 60 ||
            (current > 0.0 && current <= lowest) ||
            (lowest > 0.0 && current <= lowest * 1.10)
        return deal.isActive &&
            isNotExpired &&
            deal.availability.equals("in_stock", ignoreCase = true) &&
            hasExactUrl &&
            hasPrice &&
            hasQualitySignal
    }

    private fun encode(value: String): String = URLEncoder.encode(value, Charsets.UTF_8.name())
}

package com.example.enjoyfreedeals.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.enjoyfreedeals.data.mock.MockData
import com.example.enjoyfreedeals.data.model.Blog
import com.example.enjoyfreedeals.data.model.Category
import com.example.enjoyfreedeals.data.model.Deal
import com.example.enjoyfreedeals.data.model.DealNotification
import com.example.enjoyfreedeals.data.model.DealSort
import com.example.enjoyfreedeals.data.model.UserProfile
import com.example.enjoyfreedeals.data.repository.AuthRepository
import com.example.enjoyfreedeals.data.repository.DealNotificationManager
import com.example.enjoyfreedeals.data.repository.DealsRepository
import com.example.enjoyfreedeals.data.remote.SupabaseService
import com.example.enjoyfreedeals.utils.DealUrlUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppUiState(
    val isLoading: Boolean = true,
    val authLoading: Boolean = false,
    val user: UserProfile? = MockData.user,
    val deals: List<Deal> = emptyList(),
    val categories: List<Category> = emptyList(),
    val blogs: List<Blog> = emptyList(),
    val notifications: List<DealNotification> = emptyList(),
    val selectedStore: String = "All",
    val selectedSort: DealSort = DealSort.Newest,
    val selectedCategoryId: String? = null,
    val searchQuery: String = "",
    val darkMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val message: String? = null,
    val loggedIn: Boolean = false
) {
    val savedDealsCount: Int get() = user?.savedDeals?.size ?: 0
    val sharedDealsCount: Int get() = user?.sharedDeals ?: 0
    val clickedDealsCount: Int get() = user?.clickedDeals?.size ?: 0
    val unreadCount: Int get() = notifications.count { !it.isRead }
}

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository(application)
    private val dealsRepository = DealsRepository(application)
    private val _state = MutableStateFlow(AppUiState())
    val state: StateFlow<AppUiState> = _state

    val visibleDeals: StateFlow<List<Deal>> = combine(_state) { array ->
        val state = array.first()
        state.deals
            .filter { deal -> filterDeal(deal, state.selectedStore) }
            .filter { state.selectedCategoryId == null || it.categoryId == state.selectedCategoryId }
            .filter { deal ->
                val query = state.searchQuery.trim()
                query.isBlank() || listOf(deal.title, deal.description, deal.storeName, deal.dealType).any { it.contains(query, ignoreCase = true) }
            }
            .let { deals ->
                when (state.selectedSort) {
                    DealSort.Newest -> deals.sortedByDescending { it.createdAtMillis }
                    DealSort.HighestDiscount -> deals.sortedByDescending { it.discountPercent }
                    DealSort.LowestPrice -> deals.sortedBy { it.displayCurrentPrice }
                    DealSort.FreeDeals -> deals.filter { it.isFreeDeal || it.displayCurrentPrice == 0.0 }
                    DealSort.ExpiringSoon -> deals.sortedBy { it.expiryDate }
                    DealSort.MostClicked -> deals.sortedByDescending { it.clickCount }
                    DealSort.VerifiedDeals -> deals.filter { it.isVerified }
                    DealSort.HotDeals -> deals.filter { it.isHotDeal }
                    DealSort.RecentlyUpdated -> deals.sortedByDescending { it.updatedAtMillis }
                    DealSort.LowestPriceNow -> deals.filter { it.displayCurrentPrice <= it.displayLowestPrice }
                }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        _state.update { it.copy(loggedIn = authRepository.hasActiveSession()) }
        refreshAll()
        observeLiveDeals()
    }

    private fun observeLiveDeals() = viewModelScope.launch {
        dealsRepository.getLiveDeals()
            .catch { _state.update { it.copy(message = "Live deals are taking longer than expected. Showing saved sample deals.") } }
            .collect { liveDeals ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        deals = liveDeals,
                        message = when {
                            liveDeals.isEmpty() && SupabaseService.isConfigured -> "No live deals found. Add approved API sources in Supabase."
                            liveDeals.any { deal -> deal.isPreviewDeal } -> "Connect Supabase to load live affiliate deals. Showing preview deals."
                            else -> it.message
                        }
                    )
                }
            }
    }

    fun refreshAll() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, message = null) }
        val deals = dealsRepository.refreshLiveDeals().getOrElse { MockData.deals }
        val categories = dealsRepository.getCategories().getOrElse { MockData.categories }
        val blogs = dealsRepository.getBlogs().getOrElse { MockData.blogs }
        val notifications = dealsRepository.getNotifications().getOrElse { MockData.notifications }
        val user = authRepository.getUserProfile().getOrElse { MockData.user }
        _state.update {
            it.copy(
                isLoading = false,
                user = user,
                deals = deals,
                categories = categories,
                blogs = blogs,
                notifications = notifications,
                message = when {
                    deals.isEmpty() -> "No deals are available right now. Please check again soon."
                    deals.any { deal -> deal.isPreviewDeal } -> "Connect Supabase to load live affiliate deals. Showing preview deals."
                    categories.isEmpty() -> "Categories are loading slowly. Please try again."
                    else -> null
                }
            )
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) = viewModelScope.launch {
        _state.update { it.copy(authLoading = true, message = null) }
        authRepository.login(email, password)
            .onSuccess { profile ->
                _state.update { it.copy(authLoading = false, user = profile, loggedIn = true, message = null) }
                onSuccess()
            }
            .onFailure { error -> _state.update { it.copy(authLoading = false, message = error.message ?: "Invalid login details.") } }
    }

    fun register(name: String, email: String, mobile: String, password: String, confirmPassword: String, onSuccess: () -> Unit) = viewModelScope.launch {
        _state.update { it.copy(authLoading = true, message = null) }
        authRepository.register(name, email, mobile, password, confirmPassword)
            .onSuccess { profile ->
                _state.update { it.copy(authLoading = false, user = profile, loggedIn = true, message = null) }
                onSuccess()
            }
            .onFailure { error -> _state.update { it.copy(authLoading = false, message = error.message ?: "Invalid registration details.") } }
    }

    fun saveOrRemoveDeal(deal: Deal) = viewModelScope.launch {
        val user = _state.value.user ?: MockData.user
        val saved = if (deal.dealId in user.savedDeals) user.savedDeals - deal.dealId else user.savedDeals + deal.dealId
        val updated = user.copy(savedDeals = saved)
        _state.update { it.copy(user = updated, message = if (deal.dealId in saved) "Deal saved." else "Deal removed from saved deals.") }
        authRepository.updateSavedDeals(saved)
        val userId = updated.userId
        if (deal.dealId in saved) dealsRepository.saveDeal(userId, deal.dealId) else dealsRepository.removeSavedDeal(userId, deal.dealId)
    }

    fun countShare(deal: Deal) = viewModelScope.launch {
        val user = _state.value.user ?: MockData.user
        _state.update { it.copy(user = user.copy(sharedDeals = user.sharedDeals + 1), message = "Ready to share ${deal.title}.") }
        dealsRepository.shareDeal(deal)
    }

    fun recordDealClick(deal: Deal) = viewModelScope.launch {
        val user = _state.value.user ?: MockData.user
        val clicked = (user.clickedDeals + deal.dealId).filter { it.isNotBlank() }.distinct()
        _state.update { state ->
            state.copy(
                user = user.copy(clickedDeals = clicked),
                deals = state.deals.map { if (it.dealId == deal.dealId) it.copy(clickCount = it.clickCount + 1) else it }
            )
        }
        authRepository.updateClickedDeals(clicked)
        dealsRepository.increaseClickCount(deal.dealId)
    }

    fun markNotificationRead(notification: DealNotification, onOpenDeal: (Deal?) -> Unit) = viewModelScope.launch {
        dealsRepository.markNotificationRead(notification)
        _state.update { state ->
            state.copy(notifications = state.notifications.map { if (it.notificationId == notification.notificationId) it.copy(isRead = true) else it })
        }
        onOpenDeal(_state.value.deals.firstOrNull { it.dealId == notification.dealId })
    }

    fun selectStore(store: String) = _state.update { it.copy(selectedStore = store) }
    fun selectSort(sort: DealSort) = _state.update { it.copy(selectedSort = sort) }
    fun selectCategory(categoryId: String?) = _state.update { it.copy(selectedCategoryId = categoryId) }
    fun search(query: String) = _state.update { it.copy(searchQuery = query) }
    fun refreshDeals() = refreshAll()
    fun toggleDarkMode() = _state.update { it.copy(darkMode = !it.darkMode) }
    fun toggleNotifications() = _state.update {
        val enabled = !it.notificationsEnabled
        DealNotificationManager.notificationsEnabled = enabled
        it.copy(notificationsEnabled = enabled)
    }
    fun consumeMessage() = _state.update { it.copy(message = null) }

    fun logout(onDone: () -> Unit) {
        authRepository.logout()
        _state.update { it.copy(user = MockData.user, loggedIn = false, message = null) }
        onDone()
    }
}

private fun filterDeal(deal: Deal, selectedFilter: String): Boolean = when (selectedFilter) {
    "All" -> deal.isVisibleLiveDeal()
    "Free Deals" -> deal.isVisibleLiveDeal() && (deal.isFreeDeal || deal.displayCurrentPrice == 0.0 || deal.dealType.equals("Free Deal", true))
    "Lowest Price" -> deal.isVisibleLiveDeal() && deal.displayCurrentPrice <= deal.displayLowestPrice
    "60%+ Deals" -> deal.isVisibleLiveDeal() && deal.discountPercent >= 60
    "Hot Deals" -> deal.isVisibleLiveDeal() && deal.isHotDeal
    "Coupons" -> deal.isVisibleLiveDeal() && (deal.couponCode.isNotBlank() || deal.dealType.equals("Coupon", true))
    "Cashback" -> deal.isVisibleLiveDeal() && (deal.cashbackText.isNotBlank() || deal.dealType.equals("Cashback", true))
    "Bank Offers" -> deal.isVisibleLiveDeal() && (deal.categoryId == "bank" || deal.dealType.equals("Bank Offer", true))
    else -> deal.isVisibleLiveDeal() && deal.storeName.equals(selectedFilter, ignoreCase = true)
}

private fun Deal.isVisibleLiveDeal(): Boolean {
    val current = displayCurrentPrice
    val lowest = displayLowestPrice
    val notExpired = expiryDate == 0L || expiryDate > System.currentTimeMillis()
    val hasQualitySignal = isFreeDeal ||
        isHotDeal ||
        discountPercent >= 60 ||
        (current > 0.0 && current <= lowest) ||
        (lowest > 0.0 && current <= lowest * 1.10)
    if (!SupabaseService.isConfigured && isPreviewDeal) {
        return notExpired &&
            availability.equals("in_stock", ignoreCase = true) &&
            (current > 0.0 || isFreeDeal) &&
            hasQualitySignal
    }
    return isActive &&
        notExpired &&
        availability.equals("in_stock", ignoreCase = true) &&
        DealUrlUtils.bestDealUrl(this).isNotBlank() &&
        (current > 0.0 || isFreeDeal) &&
        hasQualitySignal
}

package com.example.enjoyfreedeals.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.enjoyfreedeals.data.model.Deal
import com.example.enjoyfreedeals.ui.about.AboutScreen
import com.example.enjoyfreedeals.ui.auth.LoginScreen
import com.example.enjoyfreedeals.ui.auth.RegisterScreen
import com.example.enjoyfreedeals.ui.blog.BlogScreen
import com.example.enjoyfreedeals.ui.category.CategoryScreen
import com.example.enjoyfreedeals.ui.deals.DealsScreen
import com.example.enjoyfreedeals.ui.home.HomeScreen
import com.example.enjoyfreedeals.ui.notification.NotificationsScreen
import com.example.enjoyfreedeals.ui.profile.ProfileScreen
import com.example.enjoyfreedeals.ui.saved.SavedDealsScreen
import com.example.enjoyfreedeals.ui.splash.SplashScreen
import com.example.enjoyfreedeals.viewmodel.AppViewModel

@Composable
fun AppNavigation(viewModel: AppViewModel, onViewDeal: (Deal) -> Boolean, onShareDeal: (Deal) -> Unit) {
    val navController = rememberNavController()
    val state by viewModel.state.collectAsState()
    val visibleDeals by viewModel.visibleDeals.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val bottomRoutes = listOf(Route.Deals, Route.Category, Route.Blog, Route.Notifications, Route.Profile)
    val showBottom = route in bottomRoutes.map { it.value } || route == Route.Home.value

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottom) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    bottomRoutes.forEach { item ->
                        val selected = backStackEntry?.destination?.hierarchy?.any { it.route == item.value } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.value) {
                                    popUpTo(Route.Home.value) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                BadgedBox(badge = { if (item == Route.Notifications && state.unreadCount > 0) Badge { Text(state.unreadCount.toString()) } }) {
                                    Icon(iconFor(item), contentDescription = item.value)
                                }
                            },
                            label = { Text(labelFor(item)) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(navController, startDestination = Route.Splash.value, Modifier.padding(padding)) {
            composable(Route.Splash.value) {
                SplashScreen {
                    navController.navigate(if (state.loggedIn) Route.Home.value else Route.Login.value) {
                        popUpTo(Route.Splash.value) { inclusive = true }
                    }
                }
            }
            composable(Route.Login.value) {
                LoginScreen(
                    loading = state.authLoading,
                    onLogin = { email, password -> viewModel.login(email, password) { navController.navigate(Route.Home.value) { popUpTo(Route.Login.value) { inclusive = true } } } },
                    onRegister = { navController.navigate(Route.Register.value) }
                )
            }
            composable(Route.Register.value) {
                RegisterScreen(
                    loading = state.authLoading,
                    onRegister = { name, email, mobile, password, confirm -> viewModel.register(name, email, mobile, password, confirm) { navController.navigate(Route.Home.value) { popUpTo(Route.Login.value) { inclusive = true } } } },
                    onLogin = { navController.popBackStack() }
                )
            }
            composable(Route.Home.value) {
                HomeScreen(
                    deals = visibleDeals,
                    savedDealIds = state.user?.savedDeals.orEmpty(),
                    unreadCount = state.unreadCount,
                    searchQuery = state.searchQuery,
                    selectedStore = state.selectedStore,
                    loading = state.isLoading,
                    onSearch = viewModel::search,
                    onStore = viewModel::selectStore,
                    onRefresh = viewModel::refreshAll,
                    onNotifications = { navController.navigate(Route.Notifications.value) },
                    onView = { deal -> if (onViewDeal(deal)) viewModel.recordDealClick(deal) },
                    onSave = viewModel::saveOrRemoveDeal
                ) {
                    viewModel.countShare(it)
                    onShareDeal(it)
                }
            }
            composable(Route.Deals.value) {
                DealsScreen(visibleDeals, state.user?.savedDeals.orEmpty(), state.selectedStore, state.selectedSort, state.searchQuery, viewModel::selectStore, viewModel::selectSort, viewModel::search, viewModel::refreshDeals, { deal -> if (onViewDeal(deal)) viewModel.recordDealClick(deal) }, viewModel::saveOrRemoveDeal) {
                    viewModel.countShare(it)
                    onShareDeal(it)
                }
            }
            composable(Route.Category.value) {
                CategoryScreen(state.categories) { categoryId ->
                    viewModel.selectCategory(categoryId)
                    navController.navigate(Route.Deals.value)
                }
            }
            composable(Route.Blog.value) { BlogScreen(state.blogs) }
            composable(Route.Notifications.value) {
                NotificationsScreen(state.notifications) { notification ->
                    viewModel.markNotificationRead(notification) { deal ->
                        deal?.let { if (onViewDeal(it)) viewModel.recordDealClick(it) } ?: notification.targetUrl.takeIf { it.isNotBlank() }?.let { onViewDeal(Deal(title = notification.title, targetUrl = it)) }
                    }
                }
            }
            composable(Route.Profile.value) {
                ProfileScreen(state.user, state.darkMode, state.notificationsEnabled, viewModel::toggleDarkMode, viewModel::toggleNotifications, { navController.navigate(Route.Saved.value) }, { navController.navigate(Route.About.value) }) {
                    viewModel.logout { navController.navigate(Route.Login.value) { popUpTo(0) } }
                }
            }
            composable(Route.Saved.value) {
                SavedDealsScreen(state.deals, state.user?.savedDeals.orEmpty(), { deal -> if (onViewDeal(deal)) viewModel.recordDealClick(deal) }, viewModel::saveOrRemoveDeal) {
                    viewModel.countShare(it)
                    onShareDeal(it)
                }
            }
            composable(Route.About.value) { AboutScreen() }
        }
    }
}

private fun iconFor(route: Route) = when (route) {
    Route.Deals -> Icons.Default.LocalOffer
    Route.Category -> Icons.Default.Category
    Route.Blog -> Icons.Default.Article
    Route.Notifications -> Icons.Default.Notifications
    else -> Icons.Default.Person
}

private fun labelFor(route: Route) = when (route) {
    Route.Deals -> "All Deals"
    Route.Category -> "Category"
    Route.Blog -> "Blog"
    Route.Notifications -> "Notifications"
    else -> "Profile"
}

package com.example.enjoyfreedeals.navigation

sealed class Route(val value: String) {
    data object Splash : Route("splash")
    data object Login : Route("login")
    data object Register : Route("register")
    data object Home : Route("home")
    data object Deals : Route("deals")
    data object Category : Route("category")
    data object Blog : Route("blog")
    data object Notifications : Route("notifications")
    data object Profile : Route("profile")
    data object Saved : Route("saved")
    data object About : Route("about")
}

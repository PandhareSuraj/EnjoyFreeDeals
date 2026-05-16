package com.example.enjoyfreedeals.data.repository

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.example.enjoyfreedeals.MainActivity
import com.example.enjoyfreedeals.R
import com.example.enjoyfreedeals.data.model.Deal
import com.example.enjoyfreedeals.utils.NotificationHelper

object DealNotificationManager {
    private const val CHANNEL_ID = "new_deals"
    private const val CHANNEL_NAME = "New Deals"
    var notificationsEnabled: Boolean
        get() = NotificationHelper.notificationsEnabled
        set(value) {
            NotificationHelper.notificationsEnabled = value
        }

    fun ensureChannel(context: Context) {
        NotificationHelper.createNotificationChannel(context)
    }

    fun showNewDealNotification(context: Context, deal: Deal) {
        NotificationHelper.showNewDealNotification(context, deal)
    }
}

package com.example.enjoyfreedeals.utils

import android.Manifest
import android.app.Notification
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

object NotificationHelper {
    private const val CHANNEL_ID = "new_deals"
    private const val CHANNEL_NAME = "New Deals"
    var notificationsEnabled: Boolean = true

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }
    }

    fun areNotificationsEnabled(context: Context): Boolean {
        if (!notificationsEnabled) return false
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    fun showNewDealNotification(context: Context, deal: Deal) {
        if (!areNotificationsEnabled(context)) return
        createNotificationChannel(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("deal_id", deal.dealId)
            putExtra("open_offer_url", DealUrlUtils.bestDealUrl(deal))
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            deal.dealId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = notificationBuilder(context)
            .setSmallIcon(R.drawable.ic_enjoy_free_deals_logo)
            .setContentTitle("New Deal Added")
            .setContentText("${deal.storeName}: ${deal.title} now at ₹${deal.displayCurrentPrice.toInt()}")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(deal.dealId.hashCode(), notification)
    }

    private fun notificationBuilder(context: Context): Notification.Builder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context)
        }
    }
}

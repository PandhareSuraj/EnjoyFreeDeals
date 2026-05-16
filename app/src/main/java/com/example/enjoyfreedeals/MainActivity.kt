package com.example.enjoyfreedeals

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.enjoyfreedeals.data.model.Deal
import com.example.enjoyfreedeals.data.repository.DealNotificationManager
import com.example.enjoyfreedeals.navigation.AppNavigation
import com.example.enjoyfreedeals.theme.EnjoyDealsTheme
import com.example.enjoyfreedeals.utils.DealUrlUtils
import com.example.enjoyfreedeals.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        DealNotificationManager.ensureChannel(this)
        requestNotificationPermissionIfNeeded()
        handleNotificationIntent(intent)
        setContent {
            val state by viewModel.state.collectAsState()
            EnjoyDealsTheme(darkTheme = state.darkMode) {
                AppNavigation(
                    viewModel = viewModel,
                    onViewDeal = ::openDeal,
                    onShareDeal = ::shareDeal
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun openDeal(deal: Deal): Boolean {
        return DealUrlUtils.openDealUrl(this, deal)
    }

    private fun shareDeal(deal: Deal) {
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, DealUrlUtils.shareText(deal))
        }, "Share deal"))
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val url = intent?.getStringExtra("open_offer_url").orEmpty()
        if (url.isNotBlank()) {
            DealUrlUtils.openDealUrl(this, Deal(title = "New Deal", targetUrl = url))
        }
    }
}

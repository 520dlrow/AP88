package com.mc88.applock88

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LockService : Service() {

    private var job: Job? = null
    private var lastApp = ""

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        startMonitoring()
    }

    private fun startForegroundService() {
        val channelId = "applock_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "AppLock Service", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("AppLock88 Active")
            .setContentText("Protecting your selected apps.")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .build()
        startForeground(1, notification)
    }

    private fun startMonitoring() {
        job = CoroutineScope(Dispatchers.Default).launch {
            val prefs = getSharedPreferences("AppLockPrefs", Context.MODE_PRIVATE)
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

            while (true) {
                val lockedApps = prefs.getStringSet("locked_apps", emptySet()) ?: emptySet()
                val currentApp = getForegroundApp(usageStatsManager)
                
                val unlockedApp = prefs.getString("unlocked_app", null)
                val unlockTime = prefs.getLong("unlock_time", 0)
                val isRecentlyUnlocked = (currentApp == unlockedApp && System.currentTimeMillis() - unlockTime < 3000)

                if (currentApp != null && currentApp != lastApp && lockedApps.contains(currentApp) && currentApp != packageName && !isRecentlyUnlocked) {
                    val intent = Intent(this@LockService, LockActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    intent.putExtra("locked_package", currentApp)
                    startActivity(intent)
                }
                lastApp = currentApp ?: ""
                delay(500)
            }
        }
    }

    private fun getForegroundApp(usageStatsManager: UsageStatsManager): String? {
        val currentTime = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - 1000 * 10, currentTime)
        if (stats == null || stats.isEmpty()) return null
        var recentStats: android.app.usage.UsageStats? = null
        var lastTimeUsed = 0L
        for (usageStats in stats) {
            if (usageStats.lastTimeUsed > lastTimeUsed) {
                recentStats = usageStats
                lastTimeUsed = usageStats.lastTimeUsed
            }
        }
        return recentStats?.packageName
    }

    override fun onDestroy() {
        job?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

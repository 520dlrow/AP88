package com.mc88.applock88

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock

class RestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("AppLockPrefs", Context.MODE_PRIVATE)
        val isProtectionActive = prefs.getBoolean("is_protection_active", false)
        
        if (isProtectionActive) {
            val serviceIntent = Intent(context, LockService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }

    companion object {
        fun scheduleRestart(context: Context) {
            val prefs = context.getSharedPreferences("AppLockPrefs", Context.MODE_PRIVATE)
            val isProtectionActive = prefs.getBoolean("is_protection_active", false)
            if (!isProtectionActive) return

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, RestartReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val triggerTime = SystemClock.elapsedRealtime() + 5000
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pendingIntent)
        }
    }
}

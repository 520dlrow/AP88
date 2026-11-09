package com.mc88.applock88

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class AppLockAccessibilityService : AccessibilityService() {

    private var lastForegroundPackage = ""

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val prefs = getSharedPreferences("AppLockPrefs", MODE_PRIVATE)
        if (!prefs.getBoolean("use_accessibility", false)) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName == this.packageName || packageName == "com.android.systemui" || packageName == "android") return

        if (packageName == lastForegroundPackage) return

        lastForegroundPackage = packageName

        val lockedApps = prefs.getStringSet("locked_apps", emptySet()) ?: emptySet()

        if (lockedApps.contains(packageName)) {
            val unlockedApp = prefs.getString("unlocked_app", null)
            val unlockTime = prefs.getLong("unlock_time", 0)
            val timeoutMs = prefs.getLong("timeout_ms", 3000L)

            val isRecentlyUnlocked = (packageName == unlockedApp && System.currentTimeMillis() - unlockTime < timeoutMs)

            if (!isRecentlyUnlocked) {
                val intent = Intent(this, LockActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                intent.putExtra("locked_package", packageName)
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {}
}

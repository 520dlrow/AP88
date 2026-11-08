package com.mc88.applock88

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AdminReceiver : DeviceAdminReceiver() {
    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "Warning: Disabling this will make AppLock88 vulnerable to uninstallation."
    }

    override fun onDisabled(context: Context, intent: Intent) {
        Toast.makeText(context, "Anti-Uninstall Disabled", Toast.LENGTH_SHORT).show()
    }
}

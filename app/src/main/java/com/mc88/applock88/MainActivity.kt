package com.mc88.applock88

import android.app.AppOpsManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)

        findViewById<Button>(R.id.btnUsage).setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        findViewById<Button>(R.id.btnOverlay).setOnClickListener {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnSetPin).setOnClickListener {
            showSetPinDialog()
        }

        findViewById<Button>(R.id.btnSelectApps).setOnClickListener {
            startActivity(Intent(this, AppListActivity::class.java))
        }

        findViewById<Button>(R.id.btnStart).setOnClickListener {
            if (!hasUsagePermission() || !Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Please grant all permissions first", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val intent = Intent(this, LockService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            tvStatus.text = getString(R.string.status_running)
        }

        findViewById<Button>(R.id.btnStop).setOnClickListener {
            stopService(Intent(this, LockService::class.java))
            tvStatus.text = getString(R.string.status_stopped)
        }
    }

    private fun showSetPinDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.btn_set_pin))
        val input = EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        builder.setView(input)
        builder.setPositiveButton("Save") { _, _ ->
            val pin = input.text.toString()
            if (pin.length >= 4) {
                getSharedPreferences("AppLockPrefs", Context.MODE_PRIVATE).edit().putString("pin", pin).apply()
                Toast.makeText(this, "PIN Saved", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "PIN must be at least 4 digits", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun hasUsagePermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName)
        } else {
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }
}

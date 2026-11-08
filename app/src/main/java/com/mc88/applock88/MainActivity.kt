package com.mc88.applock88

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvCurrentPin: TextView
    private lateinit var spinnerTheme: Spinner
    private lateinit var spinnerTimeout: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("AppLockPrefs", Context.MODE_PRIVATE)
        val savedPin = prefs.getString("pin", "")
        
        if (!savedPin.isNullOrEmpty() && !intent.hasExtra("skip_lock")) {
            val intent = Intent(this, LockActivity::class.java)
            intent.putExtra("is_self_lock", true)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_main)
        ThemeManager.applyTheme(this)

        tvStatus = findViewById(R.id.tvStatus)
        tvCurrentPin = findViewById(R.id.tvCurrentPin)
        spinnerTheme = findViewById(R.id.spinnerTheme)
        spinnerTimeout = findViewById(R.id.spinnerTimeout)

        updatePinDisplay()

        val themeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ThemeManager.themes)
        spinnerTheme.adapter = themeAdapter
        spinnerTheme.setSelection(prefs.getInt("theme_index", 0))
        spinnerTheme.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                prefs.edit().putInt("theme_index", position).apply()
                ThemeManager.applyTheme(this@MainActivity)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val timeoutOptions = arrayOf("2 seconds", "3 seconds", "5 seconds", "10 seconds", "30 seconds", "1 minute")
        val timeoutValues = longArrayOf(2000L, 3000L, 5000L, 10000L, 30000L, 60000L)
        val timeoutAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, timeoutOptions)
        spinnerTimeout.adapter = timeoutAdapter
        
        val savedTimeout = prefs.getLong("timeout_ms", 3000L)
        spinnerTimeout.setSelection(timeoutValues.indexOf(savedTimeout).coerceAtLeast(0))
        
        spinnerTimeout.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                prefs.edit().putLong("timeout_ms", timeoutValues[position]).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        findViewById<Button>(R.id.btnUsage).setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        findViewById<Button>(R.id.btnOverlay).setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnSetPin).setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            showSetPinDialog()
        }

        findViewById<Button>(R.id.btnSelectApps).setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            startActivity(Intent(this, AppListActivity::class.java))
        }

        findViewById<Button>(R.id.btnStart).setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            if (!hasUsagePermission() || !Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Please grant all permissions first", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val intent = Intent(this, LockService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent) else startService(intent)
            tvStatus.text = getString(R.string.status_running)
        }

        findViewById<Button>(R.id.btnStop).setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            stopService(Intent(this, LockService::class.java))
            tvStatus.text = getString(R.string.status_stopped)
        }
    }

    private fun updatePinDisplay() {
        val savedPin = getSharedPreferences("AppLockPrefs", Context.MODE_PRIVATE).getString("pin", "")
        if (savedPin.isNullOrEmpty()) {
            tvCurrentPin.text = "Current PIN: None"
        } else {
            tvCurrentPin.text = "Current PIN: $savedPin"
        }
    }

    private fun showSetPinDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.btn_set_pin))
        val input = EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        input.hint = "Enter new PIN (4+ digits)"
        builder.setView(input)
        builder.setPositiveButton("Save") { _, _ ->
            val pin = input.text.toString().trim()
            if (pin.length >= 4) {
                getSharedPreferences("AppLockPrefs", Context.MODE_PRIVATE).edit().putString("pin", pin).apply()
                Toast.makeText(this, "PIN Saved", Toast.LENGTH_SHORT).show()
                updatePinDisplay()
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

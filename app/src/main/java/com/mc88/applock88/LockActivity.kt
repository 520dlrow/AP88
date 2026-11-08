package com.mc88.applock88

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LockActivity : AppCompatActivity() {

    private var enteredPin = ""
    private var isSelfLock = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
        }

        setContentView(R.layout.activity_lock)
        
        ThemeManager.applyTheme(this)

        isSelfLock = intent.getBooleanExtra("is_self_lock", false)
        val lockedPkg = intent.getStringExtra("locked_package") ?: ""
        val tvDisplay = findViewById<TextView>(R.id.tvPinDisplay)
        val tvTitle = findViewById<TextView>(R.id.tvLockTitle)

        if (isSelfLock) {
            tvTitle.text = "AppLock88 is Protected"
        }

        val buttons = mapOf(
            R.id.btn0 to "0", R.id.btn1 to "1", R.id.btn2 to "2", R.id.btn3 to "3",
            R.id.btn4 to "4", R.id.btn5 to "5", R.id.btn6 to "6", R.id.btn7 to "7",
            R.id.btn8 to "8", R.id.btn9 to "9"
        )

        for ((id, number) in buttons) {
            findViewById<Button>(id).setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                if (enteredPin.length < 6) {
                    enteredPin += number
                    tvDisplay.text = enteredPin
                }
            }
        }

        findViewById<Button>(R.id.btnClear).setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            enteredPin = ""
            tvDisplay.text = ""
        }

        findViewById<Button>(R.id.btnEnter).setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            val savedPin = getSharedPreferences("AppLockPrefs", MODE_PRIVATE).getString("pin", "")?.trim()

            if (enteredPin.isNotEmpty() && enteredPin == savedPin) {
                if (isSelfLock) {
                    finish()
                } else {
                    getSharedPreferences("AppLockPrefs", MODE_PRIVATE).edit()
                        .putString("unlocked_app", lockedPkg)
                        .putLong("unlock_time", System.currentTimeMillis())
                        .apply()
                    finish()
                }
            } else {
                Toast.makeText(this, "Wrong PIN", Toast.LENGTH_SHORT).show()
                enteredPin = ""
                tvDisplay.text = ""
            }
        }

        findViewById<Button>(R.id.btnClose).setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            val startMain = Intent(Intent.ACTION_MAIN)
            startMain.addCategory(Intent.CATEGORY_HOME)
            startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(startMain)
            finishAffinity()
        }
    }

    override fun onBackPressed() { }
}

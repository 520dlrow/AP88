package com.mc88.applock88

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

object ThemeManager {
    
    val themes = listOf(
        "Dark Blue", "Cyber Punk", "Rose Pink", "Pastel Purple", "Clean Light", "AMOLED Black"
    )

    fun applyTheme(activity: Activity) {
        val prefs = activity.getSharedPreferences("AppLockPrefs", Context.MODE_PRIVATE)
        val themeIndex = prefs.getInt("theme_index", 0)
        val colors = getThemeColors(themeIndex)
        
        activity.window.statusBarColor = colors[0]
        activity.window.navigationBarColor = colors[0]

        val rootView = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)
        if (rootView.childCount > 0) {
            val mainLayout = rootView.getChildAt(0)
            mainLayout.setBackgroundColor(colors[0])
        }

        applyColorsToView(rootView, colors[1], colors[2], colors[3], colors[4])
    }

    private fun applyColorsToView(view: View?, surface: Int, primary: Int, textMain: Int, textSub: Int) {
        if (view == null) return

        when (view) {
            is Button -> {
                if (view.id == R.id.btnEnter || view.id == R.id.btnStart || view.id == R.id.btnSetPin || view.id == R.id.btnSelectApps || view.id == R.id.btnBack) {
                    view.backgroundTintList = ColorStateList.valueOf(primary)
                    view.setTextColor(if (isColorLight(primary)) Color.BLACK else Color.WHITE)
                } else if (view.id == R.id.btnStop || view.id == R.id.btnUsage || view.id == R.id.btnOverlay || view.id == R.id.btnClose || view.id == R.id.btnClear) {
                    view.backgroundTintList = ColorStateList.valueOf(surface)
                    view.setTextColor(textMain)
                } else {
                    view.backgroundTintList = ColorStateList.valueOf(surface)
                    view.setTextColor(textMain)
                }
            }
            is TextView -> {
                if (view.id == R.id.tvPinDisplay || view.id == R.id.tvCurrentPin) {
                    view.setTextColor(primary)
                } else {
                    view.setTextColor(textMain)
                }
            }
            is ViewGroup -> {
                for (i in 0 until view.childCount) {
                    applyColorsToView(view.getChildAt(i), surface, primary, textMain, textSub)
                }
            }
        }
    }

    private fun isColorLight(color: Int): Boolean {
        val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness < 0.5
    }

    private fun getThemeColors(index: Int): IntArray {
        return when(index) {
            0 -> intArrayOf(0xFF0B132B.toInt(), 0xFF1C2541.toInt(), 0xFF5BC0BE.toInt(), 0xFFFFFFFF.toInt(), 0xFF8D99AE.toInt())
            1 -> intArrayOf(0xFF1A1A1A.toInt(), 0xFF2D2D2D.toInt(), 0xFFFF5722.toInt(), 0xFFFFFFFF.toInt(), 0xFFB0B0B0.toInt())
            2 -> intArrayOf(0xFFFFF0F5.toInt(), 0xFFFFC0CB.toInt(), 0xFFE91E63.toInt(), 0xFF333333.toInt(), 0xFF888888.toInt())
            3 -> intArrayOf(0xFFE0F7FA.toInt(), 0xFFB2EBF2.toInt(), 0xFF9C27B0.toInt(), 0xFF333333.toInt(), 0xFF888888.toInt())
            4 -> intArrayOf(0xFFFFFFFF.toInt(), 0xFFF5F5F5.toInt(), 0xFF2196F3.toInt(), 0xFF000000.toInt(), 0xFF757575.toInt())
            else -> intArrayOf(0xFF000000.toInt(), 0xFF121212.toInt(), 0xFFFFFFFF.toInt(), 0xFFFFFFFF.toInt(), 0xFFAAAAAA.toInt())
        }
    }
}

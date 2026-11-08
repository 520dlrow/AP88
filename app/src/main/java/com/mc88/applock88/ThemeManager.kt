package com.mc88.applock88

import android.app.Activity
import android.content.Context
import android.widget.Button

object ThemeManager {
    
    val themes = listOf(
        "Dark Blue", "Cyber Punk", "Rose Pink", "Pastel Purple", "Clean Light", "AMOLED Black"
    )

    fun applyTheme(activity: Activity) {
        val prefs = activity.getSharedPreferences("AppLockPrefs", Context.MODE_PRIVATE)
        val themeIndex = prefs.getInt("theme_index", 0)
        val root = activity.window.decorView.findViewById<android.view.View>(android.R.id.content)

        val colors = getThemeColors(themeIndex)
        
        activity.window.decorView.setBackgroundColor(colors[0])
        activity.window.statusBarColor = colors[0]
        activity.window.navigationBarColor = colors[0]

        root?.let {
            val bgLayout = it.findViewById<android.view.View>(R.id.tvPinDisplay)?.rootView as? android.widget.LinearLayout
            bgLayout?.setBackgroundColor(colors[0])
        }

        applyTextColors(it, colors[3], colors[4])
        applyButtonColors(it, colors[1], colors[2])
    }

    private fun applyTextColors(view: android.view.View?, main: Int, sub: Int) {
        if (view is android.widget.TextView) {
            if (view.id == R.id.tvLockTitle || view.id == R.id.tvName) view.setTextColor(main)
            if (view.id == R.id.tvPinDisplay) view.setTextColor(sub)
        }
        if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) applyTextColors(view.getChildAt(i), main, sub)
        }
    }

    private fun applyButtonColors(view: android.view.View?, surface: Int, primary: Int) {
        if (view is Button) {
            if (view.id == R.id.btnEnter) {
                view.setBackgroundColor(primary)
            } else if (view.id != R.id.btnClose) {
                view.setBackgroundColor(surface)
            }
        }
        if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) applyButtonColors(view.getChildAt(i), surface, primary)
        }
    }

    private fun getThemeColors(index: Int): IntArray {
        return when(index) {
            0 -> intArrayOf(0xFF0B132B.toInt(), 0xFF1C2541.toInt(), 0xFF5BC0BE.toInt(), 0xFFFFFFFF.toInt(), 0xFF8D99AE.toInt()) // Dark Blue
            1 -> intArrayOf(0xFF1A1A1A.toInt(), 0xFF2D2D2D.toInt(), 0xFFFF5722.toInt(), 0xFFFFFFFF.toInt(), 0xFFB0B0B0.toInt()) // Cyber Punk
            2 -> intArrayOf(0xFFFFF0F5.toInt(), 0xFFFFC0CB.toInt(), 0xFFE91E63.toInt(), 0xFF333333.toInt(), 0xFF888888.toInt()) // Rose
            3 -> intArrayOf(0xFFE0F7FA.toInt(), 0xFFB2EBF2.toInt(), 0xFF9C27B0.toInt(), 0xFF333333.toInt(), 0xFF888888.toInt()) // Pastel
            4 -> intArrayOf(0xFFFFFFFF.toInt(), 0xFFF5F5F5.toInt(), 0xFF2196F3.toInt(), 0xFF000000.toInt(), 0xFF757575.toInt()) // Light
            else -> intArrayOf(0xFF000000.toInt(), 0xFF121212.toInt(), 0xFFFFFFFF.toInt(), 0xFFFFFFFF.toInt(), 0xFFAAAAAA.toInt()) // AMOLED
        }
    }
}

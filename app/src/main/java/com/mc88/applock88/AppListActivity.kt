package com.mc88.applock88

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_list)

        ThemeManager.applyTheme(this)

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            finish()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != packageName }
            .distinctBy { it.activityInfo.packageName }
            .sortedBy { it.loadLabel(pm).toString() }

        recyclerView.adapter = AppAdapter(apps, pm)
    }

    inner class AppAdapter(private val apps: List<android.content.pm.ResolveInfo>, private val pm: PackageManager) :
        RecyclerView.Adapter<AppAdapter.ViewHolder>() {

        private val prefs = getSharedPreferences("AppLockPrefs", Context.MODE_PRIVATE)
        private val lockedApps = prefs.getStringSet("locked_apps", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val icon: ImageView = v.findViewById(R.id.imgIcon)
            val name: TextView = v.findViewById(R.id.tvName)
            val checkLock: CheckBox = v.findViewById(R.id.checkLock)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = apps[position]
            val pkg = app.activityInfo.packageName
            holder.name.text = app.loadLabel(pm)
            holder.icon.setImageDrawable(app.loadIcon(pm))

            holder.checkLock.setOnCheckedChangeListener(null)
            holder.checkLock.isChecked = lockedApps.contains(pkg)

            holder.checkLock.setOnCheckedChangeListener { _, isChecked ->
                holder.checkLock.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                if (isChecked) lockedApps.add(pkg) else lockedApps.remove(pkg)
                prefs.edit().putStringSet("locked_apps", lockedApps).apply()
            }
        }

        override fun getItemCount() = apps.size
    }
}

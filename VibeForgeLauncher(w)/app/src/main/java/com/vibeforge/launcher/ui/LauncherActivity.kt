package com.vibeforge.launcher.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vibeforge.launcher.R
import com.vibeforge.launcher.core.AppLockManager
import com.vibeforge.launcher.model.AppModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LauncherActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var appLockManager: AppLockManager
    private var appsList = mutableListOf<AppModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        appLockManager = AppLockManager(this)

        recyclerView = findViewById(R.id.appsRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 4)

        loadApps()
    }

    private fun loadApps() {
        lifecycleScope.launch(Dispatchers.IO) {
            val pm = packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null)
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)

            val resolvedApps = pm.queryIntentActivities(mainIntent, 0)
            val apps = resolvedApps.map { resolveInfo ->
                AppModel(
                    label = resolveInfo.loadLabel(pm).toString(),
                    packageName = resolveInfo.activityInfo.packageName,
                    icon = runCatching { resolveInfo.loadIcon(pm) }.getOrElse { pm.defaultActivityIcon },
                    isLocked = appLockManager.isLocked(resolveInfo.activityInfo.packageName)
                )
            }.sortedBy { it.label.lowercase() }

            withContext(Dispatchers.Main) {
                appsList.clear()
                appsList.addAll(apps)
                recyclerView.adapter = AppAdapter(appsList, this@LauncherActivity)
            }
        }
    }

    internal fun showContextMenu(app: AppModel) {
        val options = arrayOf(
            if (app.isLocked) "Unlock App" else "Lock App",
            "App Info",
            "Plugins Dashboard"
        )

        MaterialAlertDialogBuilder(this)
            .setTitle(app.label)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> toggleLock(app)
                    1 -> openAppInfo(app)
                    2 -> startActivity(Intent(this, PluginsActivity::class.java))
                }
            }
            .show()
    }

    private fun toggleLock(app: AppModel) {
        lifecycleScope.launch {
            val newState = !app.isLocked
            appLockManager.setLocked(app.packageName, newState)
            app.isLocked = newState
            recyclerView.adapter?.notifyDataSetChanged()
        }
    }

    private fun openAppInfo(app: AppModel) {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.parse("package:${app.packageName}")
        }
        startActivity(intent)
    }
}

class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val icon: ImageView = view.findViewById(R.id.appIcon)
    val label: TextView = view.findViewById(R.id.appLabel)
    val lockStatus: ImageView = view.findViewById(R.id.lockStatus)
}

class AppAdapter(
    private val apps: List<AppModel>,
    private val activity: LauncherActivity
) : RecyclerView.Adapter<AppViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        holder.label.text = app.label
        holder.icon.setImageDrawable(app.icon)
        holder.lockStatus.visibility = if (app.isLocked) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            if (app.isLocked) {
                val intent = Intent(activity, BiometricLockActivity::class.java)
                intent.putExtra("packageName", app.packageName)
                activity.startActivity(intent)
            } else {
                val intent = activity.packageManager
                    .getLaunchIntentForPackage(app.packageName)
                if (intent != null) activity.startActivity(intent)
            }
        }

        holder.itemView.setOnLongClickListener {
            activity.showContextMenu(app)
            true
        }
    }

    override fun getItemCount() = apps.size
}
